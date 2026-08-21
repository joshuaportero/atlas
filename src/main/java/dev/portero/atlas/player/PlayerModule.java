package dev.portero.atlas.player;

import dev.portero.atlas.bootstrap.AtlasModule;
import dev.portero.atlas.bootstrap.ModuleContext;
import dev.portero.atlas.data.profile.ProfileComponentRegistry;
import dev.portero.atlas.pipeline.PipelineRegistry;
import dev.portero.atlas.pipeline.PlayerCloseContext;
import dev.portero.atlas.pipeline.PlayerReadyContext;

public final class PlayerModule implements AtlasModule {

    @Override
    public String id() {
        return "player";
    }

    @Override
    public void load(ModuleContext context) {
        ProfileManager profiles = new ProfileManager();
        context.services().register(ProfileManager.class, profiles);
        context.service(ProfileComponentRegistry.class)
                .register(SettingsComponent.key, SettingsComponent::new);

        PipelineRegistry pipelines = context.service(PipelineRegistry.class);
        pipelines.require("player.ready", PlayerReadyContext.class)
                .add(new AttachPlayerStage(profiles));
        pipelines.require("player.close", PlayerCloseContext.class)
                .add(new DetachPlayerStage(profiles));
    }
}
