package dev.portero.atlas.worldevent;

import dev.portero.atlas.bootstrap.AtlasModule;
import dev.portero.atlas.bootstrap.ModuleContext;
import dev.portero.atlas.event.EventBus;
import dev.portero.atlas.pipeline.PipelineRegistry;
import dev.portero.atlas.pipeline.PlayerReadyContext;
import dev.portero.atlas.player.ProfileManager;
import dev.portero.atlas.scheduler.AtlasScheduler;
import dev.portero.atlas.stat.ModifierOperation;
import dev.portero.atlas.stat.StatManager;
import dev.portero.atlas.stat.StatModifier;
import dev.portero.atlas.stat.StatRegistry;
import org.bukkit.Material;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class WorldEventModule implements AtlasModule {

    @Override
    public String id() {
        return "world-events";
    }

    @Override
    public void load(ModuleContext context) {
        WorldEventService events = new WorldEventService(
                context.service(ProfileManager.class),
                context.service(StatManager.class),
                context.service(AtlasScheduler.class),
                context.service(EventBus.class));

        StatRegistry stats = context.service(StatRegistry.class);
        long fiveMinutes = TimeUnit.MINUTES.toMillis(5);
        events.register(new WorldEventDefinition("blood_moon", "Blood Moon",
                "All opted-in players gain a large strength bonus.", Material.REDSTONE,
                fiveMinutes,
                List.of(new StatModifier("event", stats.require("strength"), ModifierOperation.PERCENT, 0.25))));
        events.register(new WorldEventDefinition("iron_veil", "Iron Veil",
                "All opted-in players gain bonus defense.", Material.IRON_BLOCK,
                fiveMinutes,
                List.of(new StatModifier("event", stats.require("defense"), ModifierOperation.FLAT, 15))));
        events.register(new WorldEventDefinition("arcane_surge", "Arcane Surge",
                "All opted-in players gain magic power and mana.", Material.NETHER_STAR,
                fiveMinutes,
                List.of(
                        new StatModifier("event", stats.require("magic_power"), ModifierOperation.FLAT, 20),
                        new StatModifier("event", stats.require("max_mana"), ModifierOperation.FLAT, 40))));
        events.register(new WorldEventDefinition("koth", "King of the Hill",
                "Capture the hill and hold it to score points.", Material.GOLDEN_HELMET,
                fiveMinutes, List.of()));

        context.services().register(WorldEventService.class, events);
        context.service(PipelineRegistry.class)
                .require("player.ready", PlayerReadyContext.class)
                .add(new ApplyWorldEventsStage(context.service(ProfileManager.class), events));
    }
}
