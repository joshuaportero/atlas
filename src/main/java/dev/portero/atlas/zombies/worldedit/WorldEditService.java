package dev.portero.atlas.zombies.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import dev.portero.atlas.zombies.arena.Arena;
import dev.portero.atlas.zombies.arena.Cuboid;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * All direct WorldEdit API usage of the zombies module lives here. This class must only be
 * loaded when WorldEdit (or FastAsyncWorldEdit) is installed; {@code ZombiesModule} guards
 * instantiation accordingly.
 */
@Slf4j
public class WorldEditService {

    private final Plugin plugin;
    private final int chunksPerTick;

    public WorldEditService(Plugin plugin, int chunksPerTick) {
        this.plugin = plugin;
        this.chunksPerTick = Math.max(1, chunksPerTick);
    }

    /**
     * Reads the player's current WorldEdit selection.
     *
     * @param player player with a selection
     * @return the selection as cuboid, or empty when incomplete or not a cuboid
     */
    public Optional<Cuboid> selectionOf(Player player) {
        com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
        try {
            Region selection = WorldEdit.getInstance().getSessionManager().get(wePlayer)
                    .getSelection(wePlayer.getWorld());
            if (!(selection instanceof CuboidRegion) || selection.getWorld() == null) {
                return Optional.empty();
            }
            BlockVector3 min = selection.getMinimumPoint();
            BlockVector3 max = selection.getMaximumPoint();
            return Optional.of(new Cuboid(selection.getWorld().getName(),
                    min.x(), min.y(), min.z(),
                    max.x(), max.y(), max.z()));
        } catch (IncompleteRegionException error) {
            return Optional.empty();
        }
    }

    /**
     * Captures the arena region into a Sponge v3 schematic file.
     *
     * @param arena arena whose region is captured
     * @param file target file
     * @return true when the snapshot was written
     */
    public boolean saveSnapshot(Arena arena, File file) {
        org.bukkit.World bukkitWorld = Bukkit.getWorld(arena.getWorldName());
        if (bukkitWorld == null) {
            log.error("Cannot snapshot arena {}: world {} is not loaded", arena.getId(), arena.getWorldName());
            return false;
        }

        World weWorld = BukkitAdapter.adapt(bukkitWorld);
        CuboidRegion region = this.toRegion(weWorld, arena.getRegion());
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        ForwardExtentCopy copy = new ForwardExtentCopy(weWorld, region, clipboard, region.getMinimumPoint());
        try {
            Operations.complete(copy);
        } catch (WorldEditException error) {
            log.error("Failed to copy region for arena {}: {}", arena.getId(), error.getMessage());
            return false;
        }

        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            log.error("Cannot create snapshot folder for arena {}", arena.getId());
            return false;
        }
        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
            return true;
        } catch (IOException error) {
            log.error("Failed to write snapshot for arena {}: {}", arena.getId(), error.getMessage());
            return false;
        }
    }

    /**
     * Restores the arena region from its snapshot, paste-budgeted over multiple ticks
     * (chunk columns per tick) to avoid lag spikes. The callback runs on the main thread
     * once every block has been pasted.
     *
     * @param arena arena to restore
     * @param file snapshot file
     * @param onComplete callback after the last block is placed
     */
    public void restore(Arena arena, File file, Runnable onComplete) {
        org.bukkit.World bukkitWorld = Bukkit.getWorld(arena.getWorldName());
        if (bukkitWorld == null) {
            log.error("Cannot restore arena {}: world {} is not loaded", arena.getId(), arena.getWorldName());
            onComplete.run();
            return;
        }

        Clipboard clipboard;
        try (ClipboardReader reader = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getReader(new FileInputStream(file))) {
            clipboard = reader.read();
        } catch (IOException error) {
            log.error("Failed to read snapshot for arena {}: {}", arena.getId(), error.getMessage());
            onComplete.run();
            return;
        }

        World weWorld = BukkitAdapter.adapt(bukkitWorld);
        Cuboid region = arena.getRegion();
        Queue<int[]> columns = new ConcurrentLinkedQueue<>(region.chunkColumns());

        new BukkitRunnable() {
            @Override
            public void run() {
                int budget = WorldEditService.this.chunksPerTick;
                while (budget-- > 0 && !columns.isEmpty()) {
                    int[] column = columns.poll();
                    if (column != null) {
                        WorldEditService.this.pasteColumn(weWorld, clipboard, region, column[0], column[1]);
                    }
                }
                if (columns.isEmpty()) {
                    this.cancel();
                    onComplete.run();
                }
            }
        }.runTaskTimer(this.plugin, 1L, 1L);
    }

    private void pasteColumn(World weWorld, Clipboard clipboard, Cuboid region, int chunkX, int chunkZ) {
        int startX = Math.max(region.minX(), chunkX << 4);
        int endX = Math.min(region.maxX(), (chunkX << 4) + 15);
        int startZ = Math.max(region.minZ(), chunkZ << 4);
        int endZ = Math.min(region.maxZ(), (chunkZ << 4) + 15);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
            for (int x = startX; x <= endX; x++) {
                for (int z = startZ; z <= endZ; z++) {
                    for (int y = region.minY(); y <= region.maxY(); y++) {
                        BlockVector3 pos = BlockVector3.at(x, y, z);
                        editSession.setBlock(pos, clipboard.getFullBlock(pos));
                    }
                }
            }
        } catch (com.sk89q.worldedit.MaxChangedBlocksException error) {
            log.warn("Block change limit hit while restoring; remaining blocks paste on the next tick");
        }
    }

    private CuboidRegion toRegion(World weWorld, Cuboid cuboid) {
        return new CuboidRegion(weWorld,
                BlockVector3.at(cuboid.minX(), cuboid.minY(), cuboid.minZ()),
                BlockVector3.at(cuboid.maxX(), cuboid.maxY(), cuboid.maxZ()));
    }
}
