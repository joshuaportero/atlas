package dev.portero.atlas.scoreboard;

import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException;
import org.bukkit.plugin.java.JavaPlugin;

public class ScoreboardManager {

    private final JavaPlugin plugin;
    private ScoreboardLibrary scoreboardLibrary;

    public ScoreboardManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            this.scoreboardLibrary = ScoreboardLibrary.loadScoreboardLibrary(plugin);
        } catch (NoPacketAdapterAvailableException e) {
            throw new RuntimeException("No scoreboard packet adapter available!", e);
        }
    }

    public void shutdown() {
        if (this.scoreboardLibrary != null) {
            this.scoreboardLibrary.close();
        }
    }
}
