package dev.portero.atlas.cmd;

import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.api.MenuService;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import org.bukkit.entity.Player;

@Command(name = "settings")
public class SettingsCommand {

    private final MenuService menus;
    private final MenuFactory factory;

    public SettingsCommand(MenuService menus, MenuFactory factory) {
        this.menus = menus;
        this.factory = factory;
    }

    @Execute
    public void open(@Context Player player) {
        this.menus.openRoot(player, this.factory.settingsMenu());
    }
}
