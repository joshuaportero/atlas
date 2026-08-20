package dev.portero.atlas.menu.player;

import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.Menus;
import dev.portero.atlas.menu.api.AtlasMenu;
import dev.portero.atlas.menu.api.ItemFactory;
import dev.portero.atlas.menu.api.MenuItem;
import dev.portero.atlas.menu.api.MenuView;
import dev.portero.atlas.party.Party;
import dev.portero.atlas.player.AtlasPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class PartyMenu extends AtlasMenu {

    private final MenuFactory menus;

    public PartyMenu(MenuFactory menus) {
        this.menus = menus;
    }

    @Override
    public Component title(Player player) {
        return Menus.title("&8Party");
    }

    @Override
    public int rows() {
        return 4;
    }

    @Override
    public void populate(MenuView view) {
        AtlasPlayer atlas = this.menus.atlas(view.player());
        Party party = this.menus.parties().party(atlas.uniqueId()).orElse(null);
        view.border(MenuItem.of(ItemFactory.pane()));
        if (party == null) {
            view.set(13, MenuItem.of(ItemFactory.of(Material.LIME_BANNER, "&aCreate Party",
                            "&eClick to create a party"))
                    .onClick(context -> {
                        this.menus.parties().create(atlas);
                        context.refresh();
                    }));
        } else {
            int slot = 10;
            for (UUID member : party.members()) {
                Player online = Bukkit.getPlayer(member);
                String name = online != null ? online.getName() : member.toString().substring(0, 8);
                String role = party.isLeader(member) ? "&6Leader" : "&7Member";
                view.set(slot, MenuItem.of(ItemFactory.of(Material.PLAYER_HEAD, "&e" + name, role)));
                slot++;
            }
            view.set(21, MenuItem.of(ItemFactory.of(Material.DARK_OAK_DOOR, "&cLeave Party",
                            "&eClick to leave"))
                    .onClick(context -> {
                        this.menus.parties().leave(atlas.uniqueId(), true);
                        context.refresh();
                    }));
            if (party.isLeader(atlas.uniqueId())) {
                view.set(23, MenuItem.of(ItemFactory.of(Material.BARRIER, "&4Disband Party",
                                "&eClick to disband"))
                        .onClick(context -> {
                            this.menus.parties().disband(atlas);
                            context.refresh();
                        }));
            }
        }
        Menus.footer(view);
    }
}
