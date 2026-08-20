package dev.portero.atlas.event;

import dev.portero.atlas.worldevent.WorldEventDefinition;

public final class WorldEventStartedEvent implements AtlasEvent {

    private final WorldEventDefinition definition;

    public WorldEventStartedEvent(WorldEventDefinition definition) {
        this.definition = definition;
    }

    public WorldEventDefinition definition() {
        return this.definition;
    }
}
