package dev.portero.atlas.data.profile;

import dev.portero.atlas.data.SqlExecutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SqlProfileRepository implements ProfileRepository {

    private final SqlExecutor executor;
    private final ProfileComponentRegistry components;

    public SqlProfileRepository(SqlExecutor executor, ProfileComponentRegistry components) {
        this.executor = executor;
        this.components = components;
    }

    @Override
    public CompletableFuture<Optional<Profile>> find(UUID uniqueId) {
        return this.executor.query(connection -> this.load(connection, uniqueId));
    }

    @Override
    public CompletableFuture<Profile> findOrCreate(UUID uniqueId, String name) {
        return this.executor.transaction(connection -> this.loadOrCreate(connection, uniqueId, name));
    }

    @Override
    public CompletableFuture<Void> save(Profile profile) {
        return this.executor.transaction(connection -> {
            this.persist(connection, profile);
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> saveAll(Collection<Profile> profiles) {
        return this.executor.transaction(connection -> {
            for (Profile profile : profiles) {
                this.persist(connection, profile);
            }
            return null;
        });
    }

    @Override
    public Optional<Profile> findNow(UUID uniqueId) {
        return this.executor.queryNow(connection -> this.load(connection, uniqueId));
    }

    @Override
    public Profile findOrCreateNow(UUID uniqueId, String name) {
        return this.executor.transactionNow(connection -> this.loadOrCreate(connection, uniqueId, name));
    }

    @Override
    public void saveNow(Profile profile) {
        this.executor.transactionNow(connection -> {
            this.persist(connection, profile);
            return null;
        });
    }

    private Profile loadOrCreate(Connection connection, UUID uniqueId, String name)
            throws SQLException {
        Optional<Profile> existing = this.load(connection, uniqueId);
        if (existing.isPresent()) {
            Profile profile = existing.get();
            profile.name(name);
            return profile;
        }

        Profile created = Profile.create(uniqueId, name);
        this.persist(connection, created);
        created.clearDirty();
        return created;
    }

    private Optional<Profile> load(Connection connection, UUID uniqueId) throws SQLException {
        Profile profile = null;
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT name, created_at, updated_at FROM atlas_profile WHERE unique_id = ?")) {
            statement.setString(1, uniqueId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    profile = new Profile(
                            uniqueId,
                            resultSet.getString("name"),
                            resultSet.getLong("created_at"),
                            resultSet.getLong("updated_at"));
                }
            }
        }

        if (profile == null) {
            return Optional.empty();
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT component_key, payload FROM atlas_profile_component WHERE unique_id = ?")) {
            statement.setString(1, uniqueId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    profile.attach(this.components.create(
                            resultSet.getString("component_key"),
                            resultSet.getString("payload")), false);
                }
            }
        }

        return Optional.of(profile);
    }

    private void persist(Connection connection, Profile profile) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO atlas_profile (unique_id, name, created_at, updated_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(unique_id) DO UPDATE SET
                    name = excluded.name,
                    updated_at = excluded.updated_at
                """)) {
            statement.setString(1, profile.uniqueId().toString());
            statement.setString(2, profile.name());
            statement.setLong(3, profile.createdAt());
            statement.setLong(4, profile.updatedAt());
            statement.executeUpdate();
        }

        Collection<ProfileComponent> attached = profile.components();
        if (attached.isEmpty()) {
            try (PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM atlas_profile_component WHERE unique_id = ?")) {
                delete.setString(1, profile.uniqueId().toString());
                delete.executeUpdate();
            }
            return;
        }

        try (PreparedStatement upsert = connection.prepareStatement("""
                INSERT INTO atlas_profile_component (unique_id, component_key, payload)
                VALUES (?, ?, ?)
                ON CONFLICT(unique_id, component_key) DO UPDATE SET
                    payload = excluded.payload
                """)) {
            for (ProfileComponent component : attached) {
                upsert.setString(1, profile.uniqueId().toString());
                upsert.setString(2, component.key());
                upsert.setString(3, component.serialize());
                upsert.addBatch();
            }
            upsert.executeBatch();
        }

        StringJoiner placeholders = new StringJoiner(", ");
        for (int index = 0; index < attached.size(); index++) {
            placeholders.add("?");
        }

        try (PreparedStatement delete = connection.prepareStatement(
                "DELETE FROM atlas_profile_component WHERE unique_id = ? AND component_key NOT IN ("
                        + placeholders + ")")) {
            delete.setString(1, profile.uniqueId().toString());
            int parameter = 2;
            for (ProfileComponent component : attached) {
                delete.setString(parameter++, component.key());
            }
            delete.executeUpdate();
        }
    }
}
