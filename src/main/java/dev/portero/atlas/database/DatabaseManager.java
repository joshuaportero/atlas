package dev.portero.atlas.database;

import com.google.common.base.Stopwatch;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class DatabaseManager {

    private final Logger logger;
    private final FileConfiguration config;

    private HikariDataSource dataSource;

    public DatabaseManager(Logger logger, FileConfiguration config) {
        this.logger = logger;
        this.config = config;
    }

    public void connect() throws SQLException {
        Stopwatch stopwatch = Stopwatch.createStarted();

        this.dataSource = new HikariDataSource();

        this.dataSource.addDataSourceProperty("cachePrepStmts", true);
        this.dataSource.addDataSourceProperty("prepStmtCacheSize", 250);
        this.dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        this.dataSource.addDataSourceProperty("useServerPrepStmts", true);

        this.dataSource.setMaximumPoolSize(5);

        this.dataSource.setUsername(config.getString("database.username"));
        this.dataSource.setPassword(config.getString("database.password"));

        this.dataSource.setDriverClassName("org.postgresql.Driver");

        String host = config.getString("database.host");
        String port = config.getString("database.port");
        boolean useSsl = config.getBoolean("database.use-ssl");

        String url = String.format("jdbc:postgresql://%s:%s/?ssl=%b", host, port, useSsl);

        this.dataSource.setJdbcUrl(url);

        logger.info("Connecting to the database...");

        this.dataSource.getConnection();

        logger.info("Connected to the database in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms.");
    }

    public void shutdown() {
        try {
            this.dataSource.close();
        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Failed to close the database connection!", e);
        }
    }
}
