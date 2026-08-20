package dev.portero.atlas.quest;

public final class QuestObjective {

    private final String type;
    private final String target;
    private final int amount;

    public QuestObjective(String type, String target, int amount) {
        this.type = type;
        this.target = target;
        this.amount = amount;
    }

    public static QuestObjective blocks(String target, int amount) {
        return new QuestObjective("block", target, amount);
    }

    public static QuestObjective entities(String target, int amount) {
        return new QuestObjective("entity", target, amount);
    }

    public String type() {
        return this.type;
    }

    public String target() {
        return this.target;
    }

    public int amount() {
        return this.amount;
    }
}
