package dev.portero.atlas.manager;

import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
public class ChatManager {
    private int slowModeSeconds;

    private boolean disabled;

    private Map<String, Instant> messageCooldowns = new HashMap<>();
}
