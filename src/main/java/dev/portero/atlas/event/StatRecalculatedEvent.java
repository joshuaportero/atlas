package dev.portero.atlas.event;

import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.stat.StatSnapshot;

public final class StatRecalculatedEvent implements AtlasEvent {

    private final AtlasPlayer player;
    private final StatSnapshot snapshot;

    public StatRecalculatedEvent(AtlasPlayer player, StatSnapshot snapshot) {
        this.player = player;
        this.snapshot = snapshot;
    }

    public AtlasPlayer player() {
        return this.player;
    }

    public StatSnapshot snapshot() {
        return this.snapshot;
    }
}
