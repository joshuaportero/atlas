package dev.portero.atlas.party;

import dev.portero.atlas.event.EventBus;
import dev.portero.atlas.event.SessionCloseEvent;
import dev.portero.atlas.lang.Messages;
import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.player.ProfileManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PartyService {

    public static final int maxSize = 5;

    private final ProfileManager profiles;
    private final Map<UUID, Party> parties = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> invites = new ConcurrentHashMap<>();

    public PartyService(ProfileManager profiles, EventBus events) {
        this.profiles = profiles;
        events.subscribe(SessionCloseEvent.class, event -> this.leave(event.uniqueId(), false));
    }

    public Optional<Party> party(UUID player) {
        return Optional.ofNullable(this.parties.get(player));
    }

    public boolean create(AtlasPlayer player) {
        if (this.parties.containsKey(player.uniqueId())) {
            Messages.Party.ALREADY.send(player.handle());
            return false;
        }
        Party party = new Party(player.uniqueId());
        this.parties.put(player.uniqueId(), party);
        player.partyId(party.id());
        Messages.Party.CREATED.send(player.handle());
        return true;
    }

    public boolean invite(AtlasPlayer leader, Player target) {
        Party party = this.parties.get(leader.uniqueId());
        if (party == null) {
            Messages.Party.NOT_IN.send(leader.handle());
            return false;
        }
        if (!party.isLeader(leader.uniqueId())) {
            Messages.Party.NOT_LEADER.send(leader.handle());
            return false;
        }
        if (party.members().size() >= maxSize) {
            Messages.Party.FULL.send(leader.handle());
            return false;
        }
        this.invites.put(target.getUniqueId(), party.id());
        Messages.Party.INVITE_SENT.send(leader.handle(), target.getName());
        Messages.Party.INVITED.send(target, leader.name());
        return true;
    }

    public boolean accept(AtlasPlayer player) {
        UUID partyId = this.invites.remove(player.uniqueId());
        if (partyId == null) {
            Messages.Party.NO_INVITE.send(player.handle());
            return false;
        }
        if (this.parties.containsKey(player.uniqueId())) {
            Messages.Party.ALREADY.send(player.handle());
            return false;
        }
        Party party = this.findById(partyId).orElse(null);
        if (party == null || party.members().size() >= maxSize) {
            Messages.Party.FULL.send(player.handle());
            return false;
        }
        party.members().add(player.uniqueId());
        this.parties.put(player.uniqueId(), party);
        player.partyId(party.id());
        this.broadcast(party, member -> Messages.Party.JOINED.send(member, player.name()));
        return true;
    }

    public boolean deny(AtlasPlayer player) {
        if (this.invites.remove(player.uniqueId()) == null) {
            Messages.Party.NO_INVITE.send(player.handle());
            return false;
        }
        return true;
    }

    public boolean leave(UUID playerId, boolean message) {
        Party party = this.parties.remove(playerId);
        if (party == null) {
            return false;
        }
        party.members().remove(playerId);
        this.profiles.find(playerId).ifPresent(player -> player.partyId(null));
        if (party.members().isEmpty()) {
            return true;
        }
        if (party.isLeader(playerId)) {
            UUID next = party.members().iterator().next();
            party.leader(next);
        }
        if (message) {
            this.profiles.find(playerId).ifPresent(player -> Messages.Party.LEFT.send(player.handle()));
        }
        return true;
    }

    public boolean kick(AtlasPlayer leader, Player target) {
        Party party = this.parties.get(leader.uniqueId());
        if (party == null) {
            Messages.Party.NOT_IN.send(leader.handle());
            return false;
        }
        if (!party.isLeader(leader.uniqueId())) {
            Messages.Party.NOT_LEADER.send(leader.handle());
            return false;
        }
        if (!party.members().contains(target.getUniqueId())
                || target.getUniqueId().equals(leader.uniqueId())) {
            return false;
        }
        this.leave(target.getUniqueId(), false);
        Messages.Party.KICKED.send(leader.handle(), target.getName());
        Messages.Party.LEFT.send(target);
        return true;
    }

    public boolean disband(AtlasPlayer leader) {
        Party party = this.parties.get(leader.uniqueId());
        if (party == null) {
            Messages.Party.NOT_IN.send(leader.handle());
            return false;
        }
        if (!party.isLeader(leader.uniqueId())) {
            Messages.Party.NOT_LEADER.send(leader.handle());
            return false;
        }
        for (UUID member : Set.copyOf(party.members())) {
            this.parties.remove(member);
            this.profiles.find(member).ifPresent(player -> {
                player.partyId(null);
                Messages.Party.DISBANDED.send(player.handle());
            });
        }
        party.members().clear();
        return true;
    }

    public void chat(AtlasPlayer player, String text) {
        Party party = this.parties.get(player.uniqueId());
        if (party == null) {
            Messages.Party.NOT_IN.send(player.handle());
            return;
        }
        this.broadcast(party, member -> Messages.Party.CHAT.send(member, player.name() + ": " + text));
    }

    private Optional<Party> findById(UUID partyId) {
        return this.parties.values().stream()
                .filter(party -> party.id().equals(partyId))
                .findFirst();
    }

    private void broadcast(Party party, java.util.function.Consumer<Player> consumer) {
        for (UUID member : party.members()) {
            Player online = Bukkit.getPlayer(member);
            if (online != null) {
                consumer.accept(online);
            }
        }
    }
}
