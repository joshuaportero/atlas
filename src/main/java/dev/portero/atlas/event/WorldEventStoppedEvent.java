package dev.portero.atlas.event;

import dev.portero.atlas.worldevent.WorldEventDefinition;

public final class WorldEventStoppedEvent implements AtlasEvent {

    private final WorldEventDefinition definition;

    public WorldEventStoppedEvent(WorldEventDefinition definition) {
        this.definition = definition;
    }

    public WorldEventDefinition definition() {
        return this.definition;
    }
}
