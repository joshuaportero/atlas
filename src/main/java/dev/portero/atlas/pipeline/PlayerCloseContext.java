package dev.portero.atlas.pipeline;

import dev.portero.atlas.data.profile.Profile;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class PlayerCloseContext extends PipelineContext {

    private final UUID uniqueId;
    private final Profile profile;
    private final Player player;

    public PlayerCloseContext(UUID uniqueId, Profile profile, Player player) {
        this.uniqueId = uniqueId;
        this.profile = profile;
        this.player = player;
    }

    public UUID uniqueId() {
        return this.uniqueId;
    }

    public Profile profile() {
        return this.profile;
    }

    public Player player() {
        return this.player;
    }
}
