package dev.portero.atlas.combat;

import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.data.property.Visibility;
import de.oliver.fancyholograms.api.hologram.Hologram;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public final class DamageHologramService {

    private static final int lifetimeTicks = 16;
    private static final Color transparent = Color.fromARGB(0);

    private final Plugin plugin;

    public DamageHologramService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void show(AtlasPostDamageEvent event) {
        DamageContext context = event.context();
        Player viewer = this.viewer(context);
        if (viewer == null || !viewer.isOnline() || context.currentDamage() <= 0.0) {
            return;
        }

        Location origin = this.origin(context.victim());
        String name = "atlas-dmg-" + UUID.randomUUID().toString().replace("-", "");
        TextHologramData data = new TextHologramData(name, origin);
        data.setPersistent(false);
        data.setText(List.of(this.line(context)));
        data.setBackground(transparent);
        data.setTextShadow(true);
        data.setSeeThrough(true);
        data.setBillboard(Display.Billboard.CENTER);
        data.setVisibility(Visibility.MANUAL);
        data.setVisibilityDistance(48);
        data.setInterpolationDuration(2);
        data.setBrightness(new Display.Brightness(15, 15));
        data.setScale(this.scale(context.critical()));

        HologramManager manager = FancyHologramsPlugin.get().getHologramManager();
        Hologram hologram = manager.create(data);
        manager.addHologram(hologram);
        hologram.forceShowHologram(viewer);

        this.animate(context.victim(), hologram, origin.clone(), manager);
    }

    private void animate(Entity victim, Hologram hologram, Location location,
                         HologramManager manager) {
        AtomicInteger tick = new AtomicInteger();
        Runnable cleanup = () -> {
            Visibility.ManualVisibility.remove(hologram);
            manager.removeHologram(hologram);
        };
        Runnable step = () -> {
            int current = tick.getAndIncrement();
            if (current >= lifetimeTicks || !hologram.getData().getLocation().isWorldLoaded()) {
                cleanup.run();
                return;
            }

            double rise = 0.11 * (1.0 - (current / (double) lifetimeTicks));
            location.add(0.0, rise, 0.0);
            hologram.getData().setLocation(location);
            hologram.forceUpdate();
            hologram.refreshForViewers();
        };

        if (victim.isValid()) {
            victim.getScheduler().runAtFixedRate(this.plugin, task -> {
                if (tick.get() >= lifetimeTicks) {
                    task.cancel();
                }
                step.run();
            }, cleanup, 1L, 1L);
            return;
        }

        this.plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(this.plugin, task -> {
            if (tick.get() >= lifetimeTicks) {
                task.cancel();
            }
            step.run();
        }, 1L, 1L);
    }

    private Player viewer(DamageContext context) {
        if (context.attackerPlayer() != null) {
            return context.attackerPlayer().handle();
        }
        if (context.victimPlayer() != null) {
            return context.victimPlayer().handle();
        }
        return null;
    }

    private Location origin(Entity victim) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double height = victim instanceof LivingEntity living ? living.getHeight() : 1.8;
        return victim.getLocation().add(
                random.nextDouble(-0.45, 0.45),
                height + random.nextDouble(0.15, 0.45),
                random.nextDouble(-0.45, 0.45));
    }

    private String line(DamageContext context) {
        long amount = Math.round(context.currentDamage());
        String number = Long.toString(amount);
        if (context.critical()) {
            return "<bold>" + this.color(context.type(), true) + number + "!</bold>";
        }
        return this.color(context.type(), false) + number;
    }

    private String color(DamageType type, boolean critical) {
        return switch (type) {
            case MAGIC -> critical ? "<#E070FF>" : "<#C44CFF>";
            case TRUE -> critical ? "<#FFFFFF>" : "<#E8E8E8>";
            case PHYSICAL -> critical ? "<#FFE14D>" : "<#F0C040>";
            default -> "<#F0C040>";
        };
    }

    private Vector3f scale(boolean critical) {
        float size = critical ? 1.85f : 1.15f;
        return new Vector3f(size, size, size);
    }
}
