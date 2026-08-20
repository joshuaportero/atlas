package dev.portero.atlas.quest;

import org.bukkit.Material;

public final class QuestDefinition {

    private final String id;
    private final String name;
    private final String description;
    private final Material icon;
    private final QuestObjective objective;
    private final long xpReward;
    private final int pointReward;

    public QuestDefinition(String id, String name, String description, Material icon,
                           QuestObjective objective, long xpReward, int pointReward) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.objective = objective;
        this.xpReward = xpReward;
        this.pointReward = pointReward;
    }

    public String id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public String description() {
        return this.description;
    }

    public Material icon() {
        return this.icon;
    }

    public QuestObjective objective() {
        return this.objective;
    }

    public long xpReward() {
        return this.xpReward;
    }

    public int pointReward() {
        return this.pointReward;
    }
}
