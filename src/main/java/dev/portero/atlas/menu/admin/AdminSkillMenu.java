package dev.portero.atlas.menu.admin;

import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.Menus;
import dev.portero.atlas.menu.api.AtlasMenu;
import dev.portero.atlas.menu.api.ItemFactory;
import dev.portero.atlas.menu.api.MenuItem;
import dev.portero.atlas.menu.api.MenuView;
import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.skill.SkillComponent;
import dev.portero.atlas.skill.SkillDefinition;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class AdminSkillMenu extends AtlasMenu {

    private final MenuFactory menus;
    private final Player target;

    public AdminSkillMenu(MenuFactory menus, Player target) {
        this.menus = menus;
        this.target = target;
    }

    @Override
    public Component title(Player player) {
        return Menus.title("&4Skills: " + this.target.getName());
    }

    @Override
    public int rows() {
        return 4;
    }

    @Override
    public void populate(MenuView view) {
        if (!this.target.isOnline()) {
            Menus.footer(view);
            return;
        }
        AtlasPlayer atlas = this.menus.atlas(this.target);
        SkillComponent component = this.menus.skills().component(atlas);
        view.border(MenuItem.of(ItemFactory.pane()));
        int slot = 10;
        for (SkillDefinition skill : this.menus.skills().registry().values()) {
            boolean unlocked = component.unlocked().contains(skill.id());
            view.set(slot, MenuItem.of(ItemFactory.of(skill.icon(), "&e" + skill.name(),
                            unlocked ? "&aUnlocked" : "&cLocked",
                            "&eLeft-click to unlock",
                            "&cRight-click to revoke"))
                    .onClick(context -> {
                        if (context.right()) {
                            this.menus.skills().revoke(atlas, skill.id());
                        } else {
                            this.menus.skills().unlock(atlas, skill.id());
                        }
                        context.refresh();
                    }));
            slot += 2;
        }
        Menus.footer(view);
    }
}
