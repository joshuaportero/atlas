package dev.portero.atlas.player;

import dev.portero.atlas.pipeline.PipelineStage;
import dev.portero.atlas.pipeline.PlayerCloseContext;

public final class DetachPlayerStage implements PipelineStage<PlayerCloseContext> {

    private final ProfileManager profiles;

    public DetachPlayerStage(ProfileManager profiles) {
        this.profiles = profiles;
    }

    @Override
    public String id() {
        return "player.detach";
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public void process(PlayerCloseContext context) {
        this.profiles.detach(context.uniqueId());
    }
}
