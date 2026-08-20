package dev.portero.atlas.data;

import dev.portero.atlas.database.DatabaseType;

public enum SqlDialect {
    SQLITE,
    POSTGRES,
    MYSQL;

    public static SqlDialect from(DatabaseType type) {
        return switch (type) {
            case SQLITE -> SQLITE;
            case POSTGRES -> POSTGRES;
            case MYSQL -> MYSQL;
            default -> throw new IllegalStateException("Unsupported database type: " + type);
        };
    }

    public String upsertProfile() {
        if (this == MYSQL) {
            return """
                    INSERT INTO atlas_profile (unique_id, name, created_at, updated_at)
                    VALUES (?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        name = VALUES(name),
                        updated_at = VALUES(updated_at)
                    """;
        }
        return """
                INSERT INTO atlas_profile (unique_id, name, created_at, updated_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(unique_id) DO UPDATE SET
                    name = excluded.name,
                    updated_at = excluded.updated_at
                """;
    }

    public String upsertComponent() {
        if (this == MYSQL) {
            return """
                    INSERT INTO atlas_profile_component (unique_id, component_key, payload)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        payload = VALUES(payload)
                    """;
        }
        return """
                INSERT INTO atlas_profile_component (unique_id, component_key, payload)
                VALUES (?, ?, ?)
                ON CONFLICT(unique_id, component_key) DO UPDATE SET
                    payload = excluded.payload
                """;
    }
}
