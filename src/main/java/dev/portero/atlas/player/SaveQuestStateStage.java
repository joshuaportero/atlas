package dev.portero.atlas.player;

import dev.portero.atlas.pipeline.PipelineStage;
import dev.portero.atlas.pipeline.PlayerCloseContext;

public final class SaveQuestStateStage implements PipelineStage<PlayerCloseContext> {

    private final ProfileManager profiles;

    public SaveQuestStateStage(ProfileManager profiles) {
        this.profiles = profiles;
    }

    @Override
    public String id() {
        return "player.quest.save";
    }

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public void process(PlayerCloseContext context) {
        this.profiles.find(context.uniqueId()).ifPresent(player -> {
            QuestStateComponent component = context.profile()
                    .component(QuestStateComponent.class)
                    .orElseGet(QuestStateComponent::new);
            component.states().clear();
            component.states().putAll(player.questStates());
            context.profile().attach(component);
        });
    }
}
