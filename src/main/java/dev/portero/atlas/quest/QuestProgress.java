package dev.portero.atlas.quest;

public final class QuestProgress {

    private final String status;
    private final int amount;

    public QuestProgress(String status, int amount) {
        this.status = status;
        this.amount = amount;
    }

    public static QuestProgress parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return new QuestProgress("none", 0);
        }
        int separator = raw.indexOf(':');
        if (separator <= 0) {
            return new QuestProgress(raw, 0);
        }
        try {
            return new QuestProgress(raw.substring(0, separator),
                    Integer.parseInt(raw.substring(separator + 1)));
        } catch (NumberFormatException ignored) {
            return new QuestProgress(raw.substring(0, separator), 0);
        }
    }

    public String status() {
        return this.status;
    }

    public int amount() {
        return this.amount;
    }

    public boolean active() {
        return "active".equals(this.status);
    }

    public boolean complete() {
        return "complete".equals(this.status);
    }

    public String serialize() {
        return this.status + ":" + this.amount;
    }
}
