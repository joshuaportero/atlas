package dev.portero.atlas.cmd;

import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.api.MenuService;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command(name = "atlas")
public class AtlasCommand {

    private final MenuService menus;
    private final MenuFactory factory;

    public AtlasCommand(MenuService menus, MenuFactory factory) {
        this.menus = menus;
        this.factory = factory;
    }

    @Execute
    @Permission("atlas.menu")
    public void execute(@Context Player player) {
        this.menus.openRoot(player, this.factory.playerHub());
    }

    @Execute(name = "admin")
    @Permission("atlas.admin")
    public void admin(@Context Player player) {
        this.menus.openRoot(player, this.factory.adminHub());
    }

    @Execute(name = "stats")
    @Permission("atlas.menu")
    public void stats(@Context Player player) {
        this.menus.openRoot(player, this.factory.statMenu());
    }

    @Execute(name = "skills")
    @Permission("atlas.menu")
    public void skills(@Context Player player) {
        this.menus.openRoot(player, this.factory.skillMenu());
    }

    @Execute(name = "quests")
    @Permission("atlas.menu")
    public void quests(@Context Player player) {
        this.menus.openRoot(player, this.factory.questMenu());
    }

    @Execute(name = "settings")
    @Permission("atlas.menu")
    public void settings(@Context Player player) {
        this.menus.openRoot(player, this.factory.settingsMenu());
    }

    @Execute(name = "profile")
    @Permission("atlas.menu")
    public void profile(@Context Player player) {
        this.menus.openRoot(player, this.factory.profileMenu());
    }

    @Execute(name = "help")
    public void help(@Context CommandSender sender) {
        sender.sendMessage("/atlas - player menu");
        sender.sendMessage("/atlas admin - management menu");
        sender.sendMessage("/party /quest /skill /level /koth /settings");
    }
}
