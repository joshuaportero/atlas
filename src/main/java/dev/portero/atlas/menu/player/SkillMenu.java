package dev.portero.atlas.menu.player;

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

public final class SkillMenu extends AtlasMenu {

    private final MenuFactory menus;

    public SkillMenu(MenuFactory menus) {
        this.menus = menus;
    }

    @Override
    public Component title(Player player) {
        return Menus.title("&8Skills");
    }

    @Override
    public int rows() {
        return 4;
    }

    @Override
    public void populate(MenuView view) {
        AtlasPlayer atlas = this.menus.atlas(view.player());
        SkillComponent component = this.menus.skills().component(atlas);
        view.border(MenuItem.of(ItemFactory.pane()));
        int slot = 10;
        for (SkillDefinition skill : this.menus.skills().registry().values()) {
            boolean unlocked = component.unlocked().contains(skill.id());
            boolean equipped = component.equipped().contains(skill.id());
            String status = !unlocked ? "&cLocked" : equipped ? "&aEquipped" : "&7Unlocked";
            view.set(slot, MenuItem.of(ItemFactory.of(skill.icon(), "&e" + skill.name(),
                            "&7" + skill.description(),
                            status,
                            unlocked ? "&eClick to toggle equip" : "&7Ask an admin to unlock"))
                    .onClick(context -> {
                        if (!unlocked) {
                            return;
                        }
                        if (equipped) {
                            this.menus.skills().unequip(atlas, skill.id());
                        } else {
                            this.menus.skills().equip(atlas, skill.id());
                        }
                        context.refresh();
                    }));
            slot += 2;
        }
        Menus.footer(view);
    }
}
