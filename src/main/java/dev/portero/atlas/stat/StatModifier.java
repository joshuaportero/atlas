package dev.portero.atlas.stat;

public final class StatModifier {

    private final String source;
    private final StatType stat;
    private final ModifierOperation operation;
    private final double amount;

    public StatModifier(String source, StatType stat, ModifierOperation operation, double amount) {
        this.source = source;
        this.stat = stat;
        this.operation = operation;
        this.amount = amount;
    }

    public String source() {
        return this.source;
    }

    public StatType stat() {
        return this.stat;
    }

    public ModifierOperation operation() {
        return this.operation;
    }

    public double amount() {
        return this.amount;
    }
}
