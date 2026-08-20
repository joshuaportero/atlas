package dev.portero.atlas.cmd;

import dev.portero.atlas.lang.Messages;
import dev.portero.atlas.menu.MenuFactory;
import dev.portero.atlas.menu.api.MenuService;
import dev.portero.atlas.player.ProfileManager;
import dev.portero.atlas.quest.QuestDefinition;
import dev.portero.atlas.quest.QuestProgress;
import dev.portero.atlas.quest.QuestService;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import org.bukkit.entity.Player;

@Command(name = "quest", aliases = {"quests"})
public class QuestCommand {

    private final QuestService quests;
    private final ProfileManager profiles;
    private final MenuService menus;
    private final MenuFactory factory;

    public QuestCommand(QuestService quests, ProfileManager profiles,
                        MenuService menus, MenuFactory factory) {
        this.quests = quests;
        this.profiles = profiles;
        this.menus = menus;
        this.factory = factory;
    }

    @Execute
    public void menu(@Context Player player) {
        this.menus.openRoot(player, this.factory.questMenu());
    }

    @Execute(name = "list")
    public void list(@Context Player player) {
        this.quests.values().forEach(quest -> player.sendMessage(
                quest.id() + " - " + quest.name() + " (" + quest.description() + ")"));
    }

    @Execute(name = "accept")
    public void accept(@Context Player player, @Arg String questId) {
        if (this.quests.find(questId).isEmpty()) {
            Messages.Quest.UNKNOWN.send(player);
            return;
        }
        if (!this.quests.start(this.profiles.require(player), questId)) {
            Messages.Quest.ALREADY.send(player);
        }
    }

    @Execute(name = "abandon")
    public void abandon(@Context Player player, @Arg String questId) {
        if (!this.quests.abandon(this.profiles.require(player), questId)) {
            Messages.Quest.NOT_ACTIVE.send(player);
        }
    }

    @Execute(name = "info")
    public void info(@Context Player player, @Arg String questId) {
        QuestDefinition quest = this.quests.find(questId).orElse(null);
        if (quest == null) {
            Messages.Quest.UNKNOWN.send(player);
            return;
        }
        QuestProgress progress = this.quests.progress(this.profiles.require(player), questId);
        player.sendMessage(quest.name() + ": " + progress.amount() + "/"
                + quest.objective().amount() + " (" + progress.status() + ")");
    }
}
