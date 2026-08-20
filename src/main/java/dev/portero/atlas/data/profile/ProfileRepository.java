package dev.portero.atlas.data.profile;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ProfileRepository {

    CompletableFuture<Optional<Profile>> find(UUID uniqueId);

    CompletableFuture<Profile> findOrCreate(UUID uniqueId, String name);

    CompletableFuture<Void> save(Profile profile);

    CompletableFuture<Void> saveAll(Collection<Profile> profiles);

    Optional<Profile> findNow(UUID uniqueId);

    Profile findOrCreateNow(UUID uniqueId, String name);

    void saveNow(Profile profile);
}
