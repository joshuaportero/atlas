package dev.portero.atlas.level;

import dev.portero.atlas.lang.Messages;
import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.stat.StatManager;

public final class LevelService {

    private final StatManager stats;
    private final int maxLevel;
    private final long baseXp;
    private final int pointsPerLevel;

    public LevelService(StatManager stats, int maxLevel, long baseXp, int pointsPerLevel) {
        this.stats = stats;
        this.maxLevel = maxLevel;
        this.baseXp = baseXp;
        this.pointsPerLevel = pointsPerLevel;
    }

    public LevelComponent component(AtlasPlayer player) {
        LevelComponent component = player.profile().component(LevelComponent.class)
                .orElseGet(LevelComponent::new);
        player.profile().attach(component, false);
        return component;
    }

    public int level(AtlasPlayer player) {
        return this.component(player).level();
    }

    public long xp(AtlasPlayer player) {
        return this.component(player).xp();
    }

    public long xpForNext(int level) {
        return this.baseXp * (long) level;
    }

    public int addXp(AtlasPlayer player, long amount) {
        if (amount <= 0) {
            return 0;
        }
        LevelComponent component = this.component(player);
        component.xp(component.xp() + amount);
        int gained = 0;
        while (component.level() < this.maxLevel
                && component.xp() >= this.xpForNext(component.level())) {
            component.xp(component.xp() - this.xpForNext(component.level()));
            component.level(component.level() + 1);
            this.stats.addPoints(player, this.pointsPerLevel);
            gained++;
        }
        if (component.level() >= this.maxLevel) {
            component.xp(0);
        }
        player.profile().attach(component);
        if (gained > 0) {
            Messages.Level.UP.send(player.handle(), component.level(), this.pointsPerLevel * gained);
        }
        return gained;
    }
}
