package dev.portero.atlas.resource;

import dev.portero.atlas.pipeline.PipelineStage;
import dev.portero.atlas.pipeline.PlayerCloseContext;
import dev.portero.atlas.player.ProfileManager;

public final class SaveResourcesStage implements PipelineStage<PlayerCloseContext> {

    private final ProfileManager profiles;
    private final ResourceManager resources;

    public SaveResourcesStage(ProfileManager profiles, ResourceManager resources) {
        this.profiles = profiles;
        this.resources = resources;
    }

    @Override
    public String id() {
        return "resource.save";
    }

    @Override
    public int priority() {
        return 20;
    }

    @Override
    public void process(PlayerCloseContext context) {
        this.profiles.find(context.uniqueId()).ifPresent(this.resources::persist);
    }
}
