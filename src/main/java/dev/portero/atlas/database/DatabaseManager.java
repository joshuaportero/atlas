package dev.portero.atlas.database;

import com.google.common.base.Stopwatch;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DatabaseManager {

    private final File dataFolder;

    private HikariDataSource dataSource;

    public DatabaseManager(File dataFolder) {

        this.dataFolder = dataFolder;
    }

    public void connect() throws SQLException {
        final Stopwatch stopwatch = Stopwatch.createStarted();

        this.dataSource = new HikariDataSource();

        this.dataSource.setDriverClassName("org.sqlite.JDBC");

        File databaseFile = new File(this.dataFolder, "database.db");

        String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();

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
