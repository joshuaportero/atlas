package dev.portero.atlas.skill;

import dev.portero.atlas.pipeline.PipelineStage;
import dev.portero.atlas.pipeline.PlayerReadyContext;
import dev.portero.atlas.player.ProfileManager;

public final class LoadSkillsStage implements PipelineStage<PlayerReadyContext> {

    private final ProfileManager profiles;
    private final SkillService skills;

    public LoadSkillsStage(ProfileManager profiles, SkillService skills) {
        this.profiles = profiles;
        this.skills = skills;
    }

    @Override
    public String id() {
        return "skill.load";
    }

    @Override
    public int priority() {
        return 15;
    }

    @Override
    public void process(PlayerReadyContext context) {
        this.skills.applyEquipped(this.profiles.require(context.player()));
    }
}
