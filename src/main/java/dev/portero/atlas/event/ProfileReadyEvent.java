package dev.portero.atlas.event;

import dev.portero.atlas.data.profile.Profile;
import dev.portero.atlas.data.session.Session;
import org.bukkit.entity.Player;

public final class ProfileReadyEvent implements AtlasEvent {

    private final Player player;
    private final Profile profile;
    private final Session session;

    public ProfileReadyEvent(Player player, Profile profile, Session session) {
        this.player = player;
        this.profile = profile;
        this.session = session;
    }

    public Player player() {
        return this.player;
    }

    public Profile profile() {
        return this.profile;
    }

    public Session session() {
        return this.session;
    }
}
