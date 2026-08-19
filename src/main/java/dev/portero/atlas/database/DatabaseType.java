package dev.portero.atlas.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DatabaseType {
    SQLITE("org.sqlite.JDBC"),
    POSTGRES("org.postgresql.Driver");

    private final String driverClassName;

    public static DatabaseType fromConfig(String value) {
        if (value == null || value.isBlank()) {
            return SQLITE;
        }

        return switch (value.toLowerCase()) {
            case "sqlite" -> SQLITE;
            case "postgres", "postgresql" -> POSTGRES;
            default -> throw new IllegalArgumentException("Unknown database type: " + value);
        };
    }
}
