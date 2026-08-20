package dev.portero.atlas.menu.api;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public abstract class AtlasMenu {

    public abstract Component title(Player player);

    public abstract int rows();

    public abstract void populate(MenuView view);

    public boolean cancelClicks() {
        return true;
    }
}
