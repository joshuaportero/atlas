package dev.portero.atlas.worldevent;

import dev.portero.atlas.pipeline.PipelineStage;
import dev.portero.atlas.pipeline.PlayerReadyContext;
import dev.portero.atlas.player.ProfileManager;

public final class ApplyWorldEventsStage implements PipelineStage<PlayerReadyContext> {

    private final ProfileManager profiles;
    private final WorldEventService events;

    public ApplyWorldEventsStage(ProfileManager profiles, WorldEventService events) {
        this.profiles = profiles;
        this.events = events;
    }

    @Override
    public String id() {
        return "event.apply";
    }

    @Override
    public int priority() {
        return 16;
    }

    @Override
    public void process(PlayerReadyContext context) {
        this.events.applyActive(this.profiles.require(context.player()));
    }
}
