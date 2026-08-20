package dev.portero.atlas.player;

import dev.portero.atlas.pipeline.PipelineStage;
import dev.portero.atlas.pipeline.PlayerReadyContext;

public final class AttachPlayerStage implements PipelineStage<PlayerReadyContext> {

    private final ProfileManager profiles;

    public AttachPlayerStage(ProfileManager profiles) {
        this.profiles = profiles;
    }

    @Override
    public String id() {
        return "player.attach";
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public void process(PlayerReadyContext context) {
        this.profiles.attach(context.player(), context.session());
    }
}
