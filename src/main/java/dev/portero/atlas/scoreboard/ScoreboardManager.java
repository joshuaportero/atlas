package dev.portero.atlas.scoreboard;

import lombok.RequiredArgsConstructor;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class ScoreboardManager {

    private final Plugin plugin;
    private ScoreboardLibrary scoreboardLibrary;

    public void initialize() {
        try {
            this.scoreboardLibrary = ScoreboardLibrary.loadScoreboardLibrary(this.plugin);
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
