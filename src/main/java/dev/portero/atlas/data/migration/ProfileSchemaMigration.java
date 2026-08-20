package dev.portero.atlas.data.migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class ProfileSchemaMigration implements Migration {

    @Override
    public int version() {
        return 1;
    }

    @Override
    public void apply(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS atlas_profile (
                        unique_id VARCHAR(36) PRIMARY KEY,
                        name VARCHAR(32) NOT NULL,
                        created_at BIGINT NOT NULL,
                        updated_at BIGINT NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS atlas_profile_component (
                        unique_id VARCHAR(36) NOT NULL,
                        component_key VARCHAR(64) NOT NULL,
                        payload TEXT NOT NULL,
                        PRIMARY KEY (unique_id, component_key),
                        FOREIGN KEY (unique_id) REFERENCES atlas_profile(unique_id) ON DELETE CASCADE
                    )
                    """);
        }
    }
}
