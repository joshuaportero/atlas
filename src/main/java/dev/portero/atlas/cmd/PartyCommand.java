package dev.portero.atlas.cmd;

import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.api.MenuService;
import dev.portero.atlas.party.PartyService;
import dev.portero.atlas.player.ProfileManager;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.join.Join;
import org.bukkit.entity.Player;

@Command(name = "party")
public class PartyCommand {

    private final PartyService parties;
    private final ProfileManager profiles;
    private final MenuService menus;
    private final MenuFactory factory;

    public PartyCommand(PartyService parties, ProfileManager profiles,
                        MenuService menus, MenuFactory factory) {
        this.parties = parties;
        this.profiles = profiles;
        this.menus = menus;
        this.factory = factory;
    }

    @Execute
    public void menu(@Context Player player) {
        this.menus.openRoot(player, this.factory.partyMenu());
    }

    @Execute(name = "create")
    public void create(@Context Player player) {
        this.parties.create(this.profiles.require(player));
    }

    @Execute(name = "invite")
    public void invite(@Context Player player, @Arg Player target) {
        this.parties.invite(this.profiles.require(player), target);
    }

    @Execute(name = "accept")
    public void accept(@Context Player player) {
        this.parties.accept(this.profiles.require(player));
    }

    @Execute(name = "deny")
    public void deny(@Context Player player) {
        this.parties.deny(this.profiles.require(player));
    }

    @Execute(name = "leave")
    public void leave(@Context Player player) {
        this.parties.leave(player.getUniqueId(), true);
    }

    @Execute(name = "kick")
    public void kick(@Context Player player, @Arg Player target) {
        this.parties.kick(this.profiles.require(player), target);
    }

    @Execute(name = "disband")
    public void disband(@Context Player player) {
        this.parties.disband(this.profiles.require(player));
    }

    @Execute(name = "chat")
    public void chat(@Context Player player, @Join String message) {
        this.parties.chat(this.profiles.require(player), message);
    }
}
