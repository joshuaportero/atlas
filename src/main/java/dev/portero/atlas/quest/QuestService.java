package dev.portero.atlas.quest;

import dev.portero.atlas.lang.Messages;
import dev.portero.atlas.level.LevelService;
import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.player.QuestStateComponent;
import dev.portero.atlas.stat.StatManager;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class QuestService {

    private final Map<String, QuestDefinition> quests = new ConcurrentHashMap<>();
    private final LevelService levels;
    private final StatManager stats;

    public QuestService(LevelService levels, StatManager stats) {
        this.levels = levels;
        this.stats = stats;
    }

    public void register(QuestDefinition quest) {
        this.quests.put(quest.id(), quest);
    }

    public Collection<QuestDefinition> values() {
        return this.quests.values();
    }

    public Optional<QuestDefinition> find(String id) {
        return Optional.ofNullable(this.quests.get(id));
    }

    public QuestStateComponent component(AtlasPlayer player) {
        QuestStateComponent component = player.profile().component(QuestStateComponent.class)
                .orElseGet(QuestStateComponent::new);
        player.profile().attach(component, false);
        player.questStates(component.states());
        return component;
    }

    public QuestProgress progress(AtlasPlayer player, String questId) {
        return QuestProgress.parse(this.component(player).states().get(questId));
    }

    public boolean start(AtlasPlayer player, String questId) {
        QuestDefinition quest = this.quests.get(questId);
        if (quest == null) {
            return false;
        }
        QuestProgress current = this.progress(player, questId);
        if (current.active() || current.complete()) {
            return false;
        }
        this.write(player, questId, new QuestProgress("active", 0));
        Messages.Quest.STARTED.send(player.handle(), quest.name());
        return true;
    }

    public boolean abandon(AtlasPlayer player, String questId) {
        QuestProgress current = this.progress(player, questId);
        if (!current.active()) {
            return false;
        }
        this.component(player).states().remove(questId);
        player.removeQuestState(questId);
        player.profile().markDirty();
        Messages.Quest.ABANDONED.send(player.handle(), questId);
        return true;
    }

    public void increment(AtlasPlayer player, String type, String target, int amount) {
        for (QuestDefinition quest : this.quests.values()) {
            QuestObjective objective = quest.objective();
            if (!objective.type().equals(type) || !objective.target().equals(target)) {
                continue;
            }
            QuestProgress current = this.progress(player, quest.id());
            if (!current.active()) {
                continue;
            }
            int next = Math.min(objective.amount(), current.amount() + amount);
            if (next >= objective.amount()) {
                this.write(player, quest.id(), new QuestProgress("complete", objective.amount()));
                this.levels.addXp(player, quest.xpReward());
                if (quest.pointReward() > 0) {
                    this.stats.addPoints(player, quest.pointReward());
                }
                Messages.Quest.COMPLETED.send(player.handle(), quest.name(), quest.xpReward());
            } else {
                this.write(player, quest.id(), new QuestProgress("active", next));
            }
        }
    }

    private void write(AtlasPlayer player, String questId, QuestProgress progress) {
        QuestStateComponent component = this.component(player);
        component.states().put(questId, progress.serialize());
        player.questState(questId, progress.serialize());
        player.profile().attach(component);
    }
}
