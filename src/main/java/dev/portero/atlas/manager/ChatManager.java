package dev.portero.atlas.manager;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatManager {

    @Setter
    @Getter
    private int slowModeDuration;

    @Setter
    @Getter
    private boolean chatDisabled;

    private final Map<UUID, Instant> userCooldowns;

    public ChatManager() {
        this.slowModeDuration = 0;
        this.chatDisabled = false;
        this.userCooldowns = new HashMap<>();
    }

    public void addUserToCooldown(UUID userId) {
        this.userCooldowns.put(userId, Instant.now());
    }

    public void refreshUserCooldown(UUID userId) {
        this.userCooldowns.put(userId, Instant.now());
    }

    public boolean isUserOnCooldown(UUID userId) {
        return this.userCooldowns.containsKey(userId);
    }

    public int getRemainingCooldown(UUID userId) {
        return (int) (this.slowModeDuration - (System.currentTimeMillis() - this.userCooldowns.get(userId).toEpochMilli()) / 1000L);
    }
}
