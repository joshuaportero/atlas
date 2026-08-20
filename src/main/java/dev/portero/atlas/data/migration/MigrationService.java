package dev.portero.atlas.data.migration;

import dev.portero.atlas.data.SqlExecutor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;

@Slf4j
public final class MigrationService {

    private final SqlExecutor executor;
    private final List<Migration> migrations;

    public MigrationService(SqlExecutor executor, List<Migration> migrations) {
        this.executor = executor;
        this.migrations = List.copyOf(migrations);
    }

    public void migrate() {
        this.executor.transactionNow(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS atlas_schema_history (
                            version INTEGER PRIMARY KEY,
                            applied_at BIGINT NOT NULL
                        )
                        """);
            }

            int current = this.currentVersion(connection);

            List<Migration> pending = this.migrations.stream()
                    .sorted(Comparator.comparingInt(Migration::version))
                    .filter(migration -> migration.version() > current)
                    .toList();

            for (Migration migration : pending) {
                log.info("Applying schema migration {}", migration.version());
                migration.apply(connection);
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO atlas_schema_history (version, applied_at) VALUES (?, ?)")) {
                    insert.setInt(1, migration.version());
                    insert.setLong(2, System.currentTimeMillis());
                    insert.executeUpdate();
                }
            }
            return null;
        });
    }

    private int currentVersion(Connection connection) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT MAX(version) FROM atlas_schema_history")) {
            try (ResultSet resultSet = query.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }
}
