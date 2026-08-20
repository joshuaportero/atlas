package dev.portero.atlas.config;

import lombok.Getter;

@Getter
public enum ConfigType {
    SCOREBOARD("scoreboard.yml"),
    DATA("data.yml"),
    DEFAULT("config.yml");

    private final String fileName;

    ConfigType(String fileName) {
        this.fileName = fileName;
    }
}
