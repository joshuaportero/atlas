package dev.portero.atlas.cmd;

import dev.portero.atlas.lang.Messages;
import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.api.MenuService;
import dev.portero.atlas.player.ProfileManager;
import dev.portero.atlas.skill.SkillService;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import org.bukkit.entity.Player;

@Command(name = "skill", aliases = {"skills"})
public class SkillCommand {

    private final SkillService skills;
    private final ProfileManager profiles;
    private final MenuService menus;
    private final MenuFactory factory;

    public SkillCommand(SkillService skills, ProfileManager profiles,
                        MenuService menus, MenuFactory factory) {
        this.skills = skills;
        this.profiles = profiles;
        this.menus = menus;
        this.factory = factory;
    }

    @Execute
    public void menu(@Context Player player) {
        this.menus.openRoot(player, this.factory.skillMenu());
    }

    @Execute(name = "equip")
    public void equip(@Context Player player, @Arg String skillId) {
        if (this.skills.equip(this.profiles.require(player), skillId)) {
            Messages.Skill.EQUIPPED.send(player, skillId);
            return;
        }
        Messages.Skill.FAILED.send(player);
    }

    @Execute(name = "unequip")
    public void unequip(@Context Player player, @Arg String skillId) {
        if (this.skills.unequip(this.profiles.require(player), skillId)) {
            Messages.Skill.UNEQUIPPED.send(player, skillId);
            return;
        }
        Messages.Skill.FAILED.send(player);
    }
}
