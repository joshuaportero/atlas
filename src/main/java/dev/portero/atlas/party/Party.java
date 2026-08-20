package dev.portero.atlas.party;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Party {

    private final UUID id;
    private volatile UUID leader;
    private final Set<UUID> members = ConcurrentHashMap.newKeySet();

    public Party(UUID leader) {
        this.id = UUID.randomUUID();
        this.leader = leader;
        this.members.add(leader);
    }

    public UUID id() {
        return this.id;
    }

    public UUID leader() {
        return this.leader;
    }

    public void leader(UUID leader) {
        this.leader = leader;
    }

    public Set<UUID> members() {
        return this.members;
    }

    public boolean isLeader(UUID player) {
        return this.leader.equals(player);
    }
}
