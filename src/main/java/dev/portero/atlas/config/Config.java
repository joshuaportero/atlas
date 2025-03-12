package dev.portero.atlas.config;

import lombok.Getter;

@Getter
public enum Config {
    DATA("data.yml"),
    SCOREBOARD("scoreboard.yml");

    private final String fileName;

    Config(String fileName) {
        this.fileName = fileName;
    }
}
