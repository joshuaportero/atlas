package dev.portero.atlas.zombies.perk;

/**
 * Well-known perk ids. Hook points reference these constants; the display data lives in
 * {@code zombies/perks.yml}.
 */
public final class Perks {

    /** Double health. */
    public static final String JUGGERNOG = "juggernog";
    /** Half reload time. */
    public static final String SPEED_COLA = "speed_cola";
    /** Faster revives; solo self-revive once. */
    public static final String QUICK_REVIVE = "quick_revive";
    /** Faster fire rate. */
    public static final String DOUBLE_TAP = "double_tap";
    /** Movement speed. */
    public static final String STAMIN_UP = "stamin_up";
    /** Tighter spread, better headshots. */
    public static final String DEADSHOT = "deadshot";

    private Perks() {
    }
}
