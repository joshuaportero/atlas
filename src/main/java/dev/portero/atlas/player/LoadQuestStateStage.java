package dev.portero.atlas.player;

import dev.portero.atlas.pipeline.PipelineStage;
import dev.portero.atlas.pipeline.PlayerReadyContext;

public final class LoadQuestStateStage implements PipelineStage<PlayerReadyContext> {

    private final ProfileManager profiles;

    public LoadQuestStateStage(ProfileManager profiles) {
        this.profiles = profiles;
    }

    @Override
    public String id() {
        return "player.quest.load";
    }

    @Override
    public int priority() {
        return 5;
    }

    @Override
    public void process(PlayerReadyContext context) {
        AtlasPlayer player = this.profiles.require(context.player());
        QuestStateComponent component = context.profile()
                .component(QuestStateComponent.class)
                .orElseGet(QuestStateComponent::new);
        player.questStates(component.states());
        context.profile().attach(component, false);
    }
}
