package dev.portero.atlas.mechanic;

import lombok.Getter;

@Getter
public enum MechanicType {
    EXPLOSION_FIREWORK("explosion_firework"),
    EXPLOSION("explosion"),
    CORPSE("corpse");

    private final String path;

    MechanicType(String path) {
        this.path = "mechanics." + path + ".enabled";
    }
}
