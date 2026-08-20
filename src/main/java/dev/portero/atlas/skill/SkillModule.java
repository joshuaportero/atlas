package dev.portero.atlas.skill;

import dev.portero.atlas.bootstrap.AtlasModule;
import dev.portero.atlas.bootstrap.ModuleContext;
import dev.portero.atlas.data.profile.ProfileComponentRegistry;
import dev.portero.atlas.pipeline.PipelineRegistry;
import dev.portero.atlas.pipeline.PlayerReadyContext;
import dev.portero.atlas.player.ProfileManager;
import dev.portero.atlas.stat.ModifierOperation;
import dev.portero.atlas.stat.StatManager;
import dev.portero.atlas.stat.StatModifier;
import dev.portero.atlas.stat.StatRegistry;
import org.bukkit.Material;

import java.util.List;

public final class SkillModule implements AtlasModule {

    @Override
    public String id() {
        return "skills";
    }

    @Override
    public void load(ModuleContext context) {
        context.service(ProfileComponentRegistry.class).register(SkillComponent.key, SkillComponent::new);

        SkillRegistry registry = new SkillRegistry();
        StatRegistry stats = context.service(StatRegistry.class);
        registry.register(new SkillDefinition("berserker", "Berserker",
                "Gain raw strength while equipped.", Material.IRON_SWORD,
                List.of(new StatModifier("skill", stats.require("strength"), ModifierOperation.FLAT, 8))));
        registry.register(new SkillDefinition("guardian", "Guardian",
                "Bolster defense while equipped.", Material.SHIELD,
                List.of(new StatModifier("skill", stats.require("defense"), ModifierOperation.FLAT, 8))));
        registry.register(new SkillDefinition("sage", "Sage",
                "Increase magic power and max mana.", Material.ENCHANTED_BOOK,
                List.of(
                        new StatModifier("skill", stats.require("magic_power"), ModifierOperation.FLAT, 10),
                        new StatModifier("skill", stats.require("max_mana"), ModifierOperation.FLAT, 20))));
        registry.register(new SkillDefinition("athlete", "Athlete",
                "Increase stamina capacity.", Material.LEATHER_BOOTS,
                List.of(new StatModifier("skill", stats.require("max_stamina"), ModifierOperation.FLAT, 25))));

        SkillService skills = new SkillService(registry, context.service(StatManager.class));
        context.services().register(SkillRegistry.class, registry);
        context.services().register(SkillService.class, skills);

        context.service(PipelineRegistry.class)
                .require("player.ready", PlayerReadyContext.class)
                .add(new LoadSkillsStage(context.service(ProfileManager.class), skills));
    }
}
