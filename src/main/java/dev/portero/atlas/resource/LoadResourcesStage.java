package dev.portero.atlas.resource;

import dev.portero.atlas.pipeline.PipelineStage;
import dev.portero.atlas.pipeline.PlayerReadyContext;
import dev.portero.atlas.player.ProfileManager;

public final class LoadResourcesStage implements PipelineStage<PlayerReadyContext> {

    private final ProfileManager profiles;
    private final ResourceManager resources;

    public LoadResourcesStage(ProfileManager profiles, ResourceManager resources) {
        this.profiles = profiles;
        this.resources = resources;
    }

    @Override
    public String id() {
        return "resource.load";
    }

    @Override
    public int priority() {
        return 20;
    }

    @Override
    public void process(PlayerReadyContext context) {
        ResourceComponent component = context.profile()
                .component(ResourceComponent.class)
                .orElseGet(ResourceComponent::new);
        context.profile().attach(component, false);
        this.resources.load(this.profiles.require(context.player()), component);
    }
}
