package dev.portero.atlas.database;

import com.google.common.base.Stopwatch;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DatabaseManager {

    private final Plugin plugin;
    private final FileConfiguration config;
    private DatabaseType type;
    private HikariDataSource dataSource;

    public DatabaseManager(Plugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void connect() throws SQLException {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        this.type = DatabaseType.fromConfig(this.config.getString("database.type"));

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("Atlas");
        hikariConfig.setDriverClassName(this.type.getDriverClassName());

        switch (this.type) {
            case SQLITE -> this.configureSqlite(hikariConfig);
            case POSTGRES -> this.configurePostgres(hikariConfig);
            case MYSQL -> this.configureMysql(hikariConfig);
            default -> throw new IllegalStateException("Unexpected database type: " + this.type);
        }

        this.dataSource = new HikariDataSource(hikariConfig);

        log.info("Connecting to {}...", this.type.name().toLowerCase());

        try (var ignored = this.dataSource.getConnection()) {
            log.info("Connected to {} in {}ms.", this.type.name().toLowerCase(),
                    stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    private void configureSqlite(HikariConfig hikariConfig) {
        String fileName = this.config.getString("database.file", "atlas.db");
        File databaseFile = new File(this.plugin.getDataFolder(), fileName);

        hikariConfig.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        hikariConfig.setMaximumPoolSize(1);
        hikariConfig.setConnectionInitSql("PRAGMA journal_mode=WAL; PRAGMA foreign_keys=ON;");
        hikariConfig.setConnectionTestQuery("SELECT 1");
    }

    private void configurePostgres(HikariConfig hikariConfig) {
        String host = this.config.getString("database.host", "localhost");
        String port = this.config.getString("database.port", "5432");
        String name = this.config.getString("database.name", "atlas");
        boolean useSsl = this.config.getBoolean("database.use-ssl");

        hikariConfig.setJdbcUrl(String.format("jdbc:postgresql://%s:%s/%s?ssl=%b", host, port, name, useSsl));
        hikariConfig.setUsername(this.config.getString("database.username"));
        hikariConfig.setPassword(this.config.getString("database.password"));
        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.addDataSourceProperty("cachePrepStmts", true);
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", 250);
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        hikariConfig.addDataSourceProperty("useServerPrepStmts", true);
    }

    private void configureMysql(HikariConfig hikariConfig) {
        String host = this.config.getString("database.host", "localhost");
        String port = this.config.getString("database.port", "3306");
        String name = this.config.getString("database.name", "atlas");
        boolean useSsl = this.config.getBoolean("database.use-ssl");

        hikariConfig.setJdbcUrl(String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=%b&allowPublicKeyRetrieval=true",
                host, port, name, useSsl));
        hikariConfig.setUsername(this.config.getString("database.username"));
        hikariConfig.setPassword(this.config.getString("database.password"));
        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.addDataSourceProperty("cachePrepStmts", true);
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", 250);
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        hikariConfig.addDataSourceProperty("useServerPrepStmts", true);
    }

    public DatabaseType getType() {
        if (this.type == null) {
            throw new IllegalStateException("Database is not connected");
        }
        return this.type;
    }

    public DataSource getDataSource() {
        if (this.dataSource == null || this.dataSource.isClosed()) {
            throw new IllegalStateException("Database is not connected");
        }
        return this.dataSource;
    }

    public void shutdown() {
        if (this.dataSource == null || this.dataSource.isClosed()) {
            return;
        }

        this.dataSource.close();
    }
}
