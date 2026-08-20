package dev.portero.atlas.data;

import dev.portero.atlas.scheduler.AtlasScheduler;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public final class SqlExecutor {

    private final DataSource dataSource;
    private final AtlasScheduler scheduler;

    public SqlExecutor(DataSource dataSource, AtlasScheduler scheduler) {
        this.dataSource = dataSource;
        this.scheduler = scheduler;
    }

    public <T> T queryNow(SqlFunction<T> function) {
        try (Connection connection = this.dataSource.getConnection()) {
            return function.apply(connection);
        } catch (SQLException exception) {
            throw new IllegalStateException("SQL query failed", exception);
        }
    }

    public void runNow(SqlConsumer consumer) {
        this.queryNow(connection -> {
            consumer.accept(connection);
            return null;
        });
    }

    public <T> T transactionNow(SqlFunction<T> function) {
        try (Connection connection = this.dataSource.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                T result = function.apply(connection);
                connection.commit();
                return result;
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("SQL transaction failed", exception);
        }
    }

    public <T> CompletableFuture<T> query(SqlFunction<T> function) {
        return this.scheduler.async(() -> this.queryNow(function));
    }

    public CompletableFuture<Void> run(SqlConsumer consumer) {
        return this.scheduler.async(() -> this.runNow(consumer));
    }

    public <T> CompletableFuture<T> transaction(SqlFunction<T> function) {
        return this.scheduler.async(() -> this.transactionNow(function));
    }

}
