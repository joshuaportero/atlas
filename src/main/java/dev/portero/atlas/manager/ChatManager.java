package dev.portero.atlas.manager;

import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
public class ChatManager {
    private int slowModeSeconds;
    private boolean isChatDisabled;
    private Map<String, Instant> playerMessageTimestamps = new HashMap<>();
}
