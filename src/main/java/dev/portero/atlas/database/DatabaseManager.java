package dev.portero.atlas.database;

import com.google.common.base.Stopwatch;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DatabaseManager {

    private final FileConfiguration config;

    private HikariDataSource dataSource;

    public DatabaseManager(FileConfiguration config) {
        this.config = config;
    }

    public void connect() throws SQLException {
        final Stopwatch stopwatch = Stopwatch.createStarted();

        this.dataSource = new HikariDataSource();

        this.dataSource.addDataSourceProperty("cachePrepStmts", true);
        this.dataSource.addDataSourceProperty("prepStmtCacheSize", 250);
        this.dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        this.dataSource.addDataSourceProperty("useServerPrepStmts", true);

        this.dataSource.setMaximumPoolSize(5);

        this.dataSource.setUsername(this.config.getString("database.username"));
        this.dataSource.setPassword(this.config.getString("database.password"));

        this.dataSource.setDriverClassName("org.postgresql.Driver");

        String host = this.config.getString("database.host");
        String port = this.config.getString("database.port");
        boolean useSsl = this.config.getBoolean("database.use-ssl");

        String url = String.format("jdbc:postgresql://%s:%s/?ssl=%b", host, port, useSsl);

        this.dataSource.setJdbcUrl(url);

        log.info("Connecting to the database...");

        this.dataSource.getConnection();

        log.info("Connected to the database in {}ms.", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    public void shutdown() {
        try {
            this.dataSource.close();
        } catch (Exception e) {
            log.error("Failed to close the database connection!", e);
        }
    }
}
