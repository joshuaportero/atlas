package dev.portero.atlas.cmd;

import dev.portero.atlas.lang.Messages;
import dev.portero.atlas.level.LevelService;
import dev.portero.atlas.player.ProfileManager;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import org.bukkit.entity.Player;

@Command(name = "level")
public class LevelCommand {

    private final LevelService levels;
    private final ProfileManager profiles;

    public LevelCommand(LevelService levels, ProfileManager profiles) {
        this.levels = levels;
        this.profiles = profiles;
    }

    @Execute
    public void info(@Context Player player) {
        var atlas = this.profiles.require(player);
        int level = this.levels.level(atlas);
        Messages.Level.INFO.send(player, level);
        Messages.Level.XP.send(player, this.levels.xp(atlas), this.levels.xpForNext(level));
    }
}
