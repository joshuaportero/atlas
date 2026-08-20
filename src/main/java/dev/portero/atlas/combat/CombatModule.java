package dev.portero.atlas.combat;

import dev.portero.atlas.bootstrap.AtlasModule;
import dev.portero.atlas.bootstrap.ModuleContext;
import dev.portero.atlas.config.ConfigManager;
import dev.portero.atlas.config.ConfigType;
import dev.portero.atlas.event.EventBus;
import dev.portero.atlas.pipeline.PipelineRegistry;
import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.player.ProfileManager;
import dev.portero.atlas.player.SettingsComponent;
import dev.portero.atlas.stat.StatManager;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

@Slf4j
public final class CombatModule implements AtlasModule {

    @Override
    public String id() {
        return "combat";
    }

    @Override
    public void load(ModuleContext context) {
        CombatManager manager = new CombatManager(
                context.service(ProfileManager.class),
                context.service(StatManager.class),
                context.service(PipelineRegistry.class),
                context.service(EventBus.class));
        context.services().register(CombatManager.class, manager);
    }

    @Override
    public void enable(ModuleContext context) {
        YamlConfiguration config = context.service(ConfigManager.class).getConfig(ConfigType.DEFAULT);
        CombatManager combat = context.service(CombatManager.class);
        combat.enabled(config == null || config.getBoolean("combat.enabled", true));

        Plugin plugin = context.plugin();
        plugin.getServer().getPluginManager().registerEvents(new CombatListener(combat), plugin);

        if (plugin.getServer().getPluginManager().getPlugin("FancyHolograms") == null) {
            log.info("FancyHolograms not found; floating combat numbers are disabled");
            return;
        }

        DamageHologramService holograms = new DamageHologramService(plugin);
        context.service(EventBus.class).subscribe(AtlasPostDamageEvent.class, event -> {
            AtlasPlayer attacker = event.context().attackerPlayer();
            if (attacker != null) {
                boolean feedback = attacker.profile().component(SettingsComponent.class)
                        .map(SettingsComponent::combatFeedback)
                        .orElse(true);
                if (!feedback) {
                    return;
                }
            }
            holograms.show(event);
        });
    }
}
