package dev.portero.atlas.stat;

import dev.portero.atlas.pipeline.PipelineStage;
import dev.portero.atlas.pipeline.PlayerReadyContext;
import dev.portero.atlas.player.ProfileManager;

public final class LoadStatsStage implements PipelineStage<PlayerReadyContext> {

    private final ProfileManager profiles;
    private final StatManager stats;

    public LoadStatsStage(ProfileManager profiles, StatManager stats) {
        this.profiles = profiles;
        this.stats = stats;
    }

    @Override
    public String id() {
        return "stat.load";
    }

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public void process(PlayerReadyContext context) {
        StatComponent component = context.profile()
                .component(StatComponent.class)
                .orElseGet(StatComponent::new);
        context.profile().attach(component, false);
        this.stats.recalculate(this.profiles.require(context.player()));
    }
}
