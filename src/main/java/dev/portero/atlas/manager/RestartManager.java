package dev.portero.atlas.manager;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitTask;

@Getter
@Setter
public class RestartManager {
    private BukkitTask task;
}
