package dev.portero.atlas.cmd;

import dev.portero.atlas.util.MessageUtil;
import dev.portero.atlas.zombies.ZombiesModule;
import dev.portero.atlas.zombies.arena.Arena;
import dev.portero.atlas.zombies.arena.BlockPos;
import dev.portero.atlas.zombies.arena.Cuboid;
import dev.portero.atlas.zombies.arena.DoorDef;
import dev.portero.atlas.zombies.arena.WallBuyDef;
import dev.portero.atlas.zombies.arena.WindowDef;
import dev.portero.atlas.zombies.arena.setup.SetupSession;
import dev.portero.atlas.zombies.game.GameManager;
import dev.portero.atlas.zombies.worldedit.WorldEditService;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Optional;

/**
 * Root command of the zombies minigame ({@code /za}).
 */
@Command(name = "za")
public class ZombiesCommand {

    private static final String PREFIX = "&8[&cZA&8] ";

    private final ZombiesModule module;

    public ZombiesCommand(ZombiesModule module) {
        this.module = module;
    }

    @Execute
    public void help(@Context CommandSender sender) {
        sender.sendMessage(MessageUtil.format("&8&m----------&r &cAtlas Zombies &8&m----------"));
        sender.sendMessage(MessageUtil.format("&e/za join <arena> &8- &7join a game"));
        sender.sendMessage(MessageUtil.format("&e/za leave &8- &7leave your game"));
        sender.sendMessage(MessageUtil.format("&e/za list &8- &7list arenas"));
        if (sender.hasPermission("atlas.zombies.admin.setup")) {
            sender.sendMessage(MessageUtil.format("&e/za setup <arena> &8- &7create/edit an arena"));
            sender.sendMessage(MessageUtil.format("&e/za start|stop <arena> &8- &7manage games"));
            sender.sendMessage(MessageUtil.format("&e/za reload &8- &7reload configs and arenas"));
        }
    }

    @Execute(name = "join")
    @Permission("atlas.zombies.play")
    public void join(@Context Player player, @Arg String arenaId) {
        Optional<Arena> arena = this.module.getArenaRepository().find(arenaId);
        if (arena.isEmpty()) {
            this.send(player, "&cUnknown arena '&e" + arenaId + "&c'");
            return;
        }
        if (Bukkit.getWorld(arena.get().getWorldName()) == null) {
            this.send(player, "&cArena world is not loaded");
            return;
        }
        if (this.module.getWorldEditService() == null
                || !this.module.getArenaRepository().schematicFile(arena.get().getId()).exists()) {
            this.send(player, "&cArena is not ready (missing WorldEdit snapshot)");
            return;
        }

        GameManager.JoinResult result = this.module.getGameManager().join(player, arena.get());
        switch (result) {
            case ALREADY_IN_GAME -> this.send(player, "&cYou are already in a game");
            case NOT_JOINABLE -> this.send(player, "&cThat game is full or already running");
            case ARENA_RESETTING -> this.send(player, "&cArena is resetting, try again in a moment");
            default -> {
            }
        }
    }

    @Execute(name = "leave")
    @Permission("atlas.zombies.play")
    public void leave(@Context Player player) {
        if (this.module.getGameManager().sessionOf(player.getUniqueId()).isEmpty()) {
            this.send(player, "&cYou are not in a game");
            return;
        }
        this.module.getGameManager().leave(player);
    }

    @Execute(name = "list")
    @Permission("atlas.zombies.play")
    public void list(@Context CommandSender sender) {
        sender.sendMessage(MessageUtil.format(PREFIX + "&7Arenas:"));
        for (Arena arena : this.module.getArenaRepository().arenas()) {
            boolean running = this.module.getGameManager().sessionOfArena(arena.getId()).isPresent();
            sender.sendMessage(MessageUtil.format("&8- &e" + arena.getId() + " &8(&7" + arena.getDisplayName()
                    + "&8) " + (running ? "&arunning" : "&7idle")));
        }
    }

    @Execute(name = "start")
    @Permission("atlas.zombies.admin.start")
    public void start(@Context CommandSender sender, @Arg String arenaId) {
        Optional<Arena> arena = this.module.getArenaRepository().find(arenaId);
        if (arena.isEmpty()) {
            this.send(sender, "&cUnknown arena '&e" + arenaId + "&c'");
            return;
        }
        if (this.module.getGameManager().forceStart(arena.get())) {
            this.send(sender, "&aCountdown started");
        } else {
            this.send(sender, "&cNo joinable lobby for that arena (empty or resetting)");
        }
    }

    @Execute(name = "stop")
    @Permission("atlas.zombies.admin.stop")
    public void stop(@Context CommandSender sender, @Arg String arenaId) {
        if (this.module.getGameManager().stop(arenaId.toLowerCase())) {
            this.send(sender, "&aGame stopped, arena is resetting");
        } else {
            this.send(sender, "&cNo game running in '&e" + arenaId + "&c'");
        }
    }

    @Execute(name = "setup")
    @Permission("atlas.zombies.admin.setup")
    public void setup(@Context Player player, @Arg String arenaId) {
        SetupSession session = this.module.getSetupManager()
                .start(player, arenaId, this.module.getArenaRepository());
        this.send(player, "&aSetup session started for '&e" + session.getArenaId() + "&a'");
        this.sendSetupHints(player);
    }

    @Execute(name = "set region")
    @Permission("atlas.zombies.admin.setup")
    public void setRegion(@Context Player player) {
        WorldEditService worldEdit = this.module.getWorldEditService();
        if (worldEdit == null) {
            this.send(player, "&cWorldEdit is required to define regions");
            return;
        }
        this.module.getSetupManager().sessionOf(player.getUniqueId()).ifPresentOrElse(session -> {
            Optional<Cuboid> selection = worldEdit.selectionOf(player);
            if (selection.isEmpty()) {
                this.send(player, "&cMake a cuboid WorldEdit selection first (//pos1, //pos2)");
                return;
            }
            if (!selection.get().world().equals(player.getWorld().getName())) {
                this.send(player, "&cSelection must be in the world you are standing in");
                return;
            }
            session.setRegion(selection.get());
            this.send(player, "&aRegion set");
        }, () -> this.send(player, "&cNo setup session — run /za setup <arena> first"));
    }

    @Execute(name = "set lobbyspawn")
    @Permission("atlas.zombies.admin.setup")
    public void setLobbySpawn(@Context Player player) {
        this.module.getSetupManager().sessionOf(player.getUniqueId()).ifPresentOrElse(session -> {
            session.setLobbySpawn(player.getLocation());
            this.send(player, "&aLobby spawn set");
        }, () -> this.send(player, "&cNo setup session — run /za setup <arena> first"));
    }

    @Execute(name = "set addplayerspawn")
    @Permission("atlas.zombies.admin.setup")
    public void addPlayerSpawn(@Context Player player) {
        this.module.getSetupManager().sessionOf(player.getUniqueId()).ifPresentOrElse(session -> {
            session.getPlayerSpawns().add(player.getLocation());
            this.send(player, "&aPlayer spawn #" + session.getPlayerSpawns().size() + " added");
        }, () -> this.send(player, "&cNo setup session — run /za setup <arena> first"));
    }

    @Execute(name = "set addzombiespawn")
    @Permission("atlas.zombies.admin.setup")
    public void addZombieSpawn(@Context Player player) {
        this.module.getSetupManager().sessionOf(player.getUniqueId()).ifPresentOrElse(session -> {
            session.getZombieSpawns().add(player.getLocation());
            this.send(player, "&aZombie spawn #" + session.getZombieSpawns().size() + " added");
        }, () -> this.send(player, "&cNo setup session — run /za setup <arena> first"));
    }

    @Execute(name = "set addwindow")
    @Permission("atlas.zombies.admin.setup")
    public void addWindow(@Context Player player, @Arg String id) {
        WorldEditService worldEdit = this.module.getWorldEditService();
        if (worldEdit == null) {
            this.send(player, "&cWorldEdit is required to define regions");
            return;
        }
        this.module.getSetupManager().sessionOf(player.getUniqueId()).ifPresentOrElse(session -> {
            Optional<Cuboid> selection = worldEdit.selectionOf(player);
            if (selection.isEmpty() || !selection.get().world().equals(player.getWorld().getName())) {
                this.send(player, "&cMake a cuboid WorldEdit selection of the window planks first");
                return;
            }
            Material material = this.firstSolidMaterial(player.getWorld(), selection.get());
            if (material == null) {
                this.send(player, "&cFill the selected region with the barricade blocks first");
                return;
            }
            WindowDef window = new WindowDef(id.toLowerCase(), selection.get(), player.getLocation(), material);
            session.getWindows().put(window.id(), window);
            this.send(player, "&aWindow '&e" + window.id() + "&a' added &8(&7zombie spawn = your position&8)");
        }, () -> this.send(player, "&cNo setup session — run /za setup <arena> first"));
    }

    @Execute(name = "set adddoor")
    @Permission("atlas.zombies.admin.setup")
    public void addDoor(@Context Player player, @Arg String id, @Arg int price) {
        WorldEditService worldEdit = this.module.getWorldEditService();
        if (worldEdit == null) {
            this.send(player, "&cWorldEdit is required to define regions");
            return;
        }
        this.module.getSetupManager().sessionOf(player.getUniqueId()).ifPresentOrElse(session -> {
            Optional<Cuboid> selection = worldEdit.selectionOf(player);
            if (selection.isEmpty() || !selection.get().world().equals(player.getWorld().getName())) {
                this.send(player, "&cMake a cuboid WorldEdit selection of the door blocks first");
                return;
            }
            if (price < 0) {
                this.send(player, "&cPrice must be 0 or higher");
                return;
            }
            DoorDef door = new DoorDef(id.toLowerCase(), selection.get(), price);
            session.getDoors().put(door.id(), door);
            this.send(player, "&aDoor '&e" + door.id() + "&a' added for &e" + price + " points");
        }, () -> this.send(player, "&cNo setup session — run /za setup <arena> first"));
    }

    @Execute(name = "set addwallbuy")
    @Permission("atlas.zombies.admin.setup")
    public void addWallBuy(@Context Player player, @Arg String gunId, @Arg int price, @Arg int ammoPrice) {
        this.module.getSetupManager().sessionOf(player.getUniqueId()).ifPresentOrElse(session -> {
            if (this.module.getGunRegistry().find(gunId).isEmpty()) {
                this.send(player, "&cUnknown gun '&e" + gunId + "&c' (see zombies/guns.yml)");
                return;
            }
            Block target = player.getTargetBlockExact(5);
            if (target == null || target.getType().isAir()) {
                this.send(player, "&cLook at the wall block to place the wall-buy on");
                return;
            }
            BlockPos pos = new BlockPos(target.getX(), target.getY(), target.getZ());
            BlockFace facing = this.lookFace(player.getLocation().getYaw()).getOppositeFace();
            String id = gunId.toLowerCase() + "_" + (session.getWallBuys().size() + 1);
            session.getWallBuys().put(id, new WallBuyDef(id, gunId.toLowerCase(), pos, facing, price, ammoPrice));
            this.send(player, "&aWall-buy '&e" + id + "&a' added for gun '&e" + gunId.toLowerCase() + "&a'");
        }, () -> this.send(player, "&cNo setup session — run /za setup <arena> first"));
    }

    @Execute(name = "set boxprice")
    @Permission("atlas.zombies.admin.setup")
    public void setBoxPrice(@Context Player player, @Arg int price) {
        this.module.getSetupManager().sessionOf(player.getUniqueId()).ifPresentOrElse(session -> {
            if (price < 0) {
                this.send(player, "&cPrice must be 0 or higher");
                return;
            }
            session.setBoxPrice(price);
            this.send(player, "&aMystery box price set to &e" + price);
        }, () -> this.send(player, "&cNo setup session — run /za setup <arena> first"));
    }

    @Execute(name = "set addboxlocation")
    @Permission("atlas.zombies.admin.setup")
    public void addBoxLocation(@Context Player player) {
        this.module.getSetupManager().sessionOf(player.getUniqueId()).ifPresentOrElse(session -> {
            Block target = player.getTargetBlockExact(5);
            Location location = target != null && !target.getType().isAir()
                    ? target.getLocation()
                    : player.getLocation().getBlock().getLocation();
            session.getBoxLocations().add(location);
            this.send(player, "&aBox location #" + session.getBoxLocations().size()
                    + " added &8(&7the block itself is the box; first location is the initial one&8)");
        }, () -> this.send(player, "&cNo setup session — run /za setup <arena> first"));
    }

    @Execute(name = "set adddrink")
    @Permission("atlas.zombies.admin.setup")
    public void addDrink(@Context Player player, @Arg String perkId) {
        this.module.getSetupManager().sessionOf(player.getUniqueId()).ifPresentOrElse(session -> {
            if (this.module.getPerkRegistry().find(perkId.toLowerCase()).isEmpty()) {
                this.send(player, "&cUnknown perk '&e" + perkId + "&c' (see zombies/perks.yml)");
                return;
            }
            Block target = player.getTargetBlockExact(5);
            if (target == null || target.getType().isAir()) {
                this.send(player, "&cLook at the drink machine block");
                return;
            }
            session.getDrinks().put(perkId.toLowerCase(),
                    new BlockPos(target.getX(), target.getY(), target.getZ()));
            this.send(player, "&aDrink machine '&e" + perkId.toLowerCase() + "&a' placed");
        }, () -> this.send(player, "&cNo setup session — run /za setup <arena> first"));
    }

    @Execute(name = "set powerswitch")
    @Permission("atlas.zombies.admin.setup")
    public void setPowerSwitch(@Context Player player) {
        this.module.getSetupManager().sessionOf(player.getUniqueId()).ifPresentOrElse(session -> {
            Block target = player.getTargetBlockExact(5);
            if (target == null || target.getType().isAir()) {
                this.send(player, "&cLook at the power switch block");
                return;
            }
            session.setPowerSwitch(new BlockPos(target.getX(), target.getY(), target.getZ()));
            this.send(player, "&aPower switch placed &8(&7drinks and the box need power&8)");
        }, () -> this.send(player, "&cNo setup session — run /za setup <arena> first"));
    }

    @Execute(name = "give")
    @Permission("atlas.zombies.admin.give")
    public void give(@Context Player player, @Arg String gunId) {
        this.module.getGunRegistry().find(gunId.toLowerCase()).ifPresentOrElse(gun -> {
            this.module.getGunService().giveGun(player, gun);
            this.send(player, "&aGave " + gun.displayName());
        }, () -> this.send(player, "&cUnknown gun '&e" + gunId + "&c'"));
    }

    @Execute(name = "save")
    @Permission("atlas.zombies.admin.setup")
    public void save(@Context Player player) {
        Optional<SetupSession> session = this.module.getSetupManager().sessionOf(player.getUniqueId());
        if (session.isEmpty()) {
            this.send(player, "&cNo setup session — run /za setup <arena> first");
            return;
        }
        if (!session.get().isComplete()) {
            this.send(player, "&cArena is incomplete:");
            session.get().missing().forEach(missing -> this.send(player, "&8- &e" + missing));
            return;
        }

        Arena arena = session.get().build(session.get().getRegion().world());
        this.module.getArenaRepository().save(arena);

        WorldEditService worldEdit = this.module.getWorldEditService();
        if (worldEdit != null) {
            File schematic = this.module.getArenaRepository().schematicFile(arena.getId());
            boolean written = worldEdit.saveSnapshot(arena, schematic);
            this.send(player, written ? "&aArena saved with world snapshot"
                    : "&cArena saved but the snapshot failed — check console");
        } else {
            this.send(player, "&eArena saved, but WorldEdit is missing so no snapshot was captured");
        }
        this.module.getSetupManager().end(player.getUniqueId());
    }

    @Execute(name = "cancel")
    @Permission("atlas.zombies.admin.setup")
    public void cancel(@Context Player player) {
        this.module.getSetupManager().end(player.getUniqueId());
        this.send(player, "&7Setup session discarded");
    }

    @Execute(name = "reload")
    @Permission("atlas.zombies.admin.reload")
    public void reload(@Context CommandSender sender) {
        this.module.reload();
        this.send(sender, "&aZombies module reloaded");
    }

    private void sendSetupHints(Player player) {
        this.send(player, "&7Steps: &e//pos1//pos2 &8→ &e/za set region &8→ &e/za set lobbyspawn");
        this.send(player, "&8→ &e/za set addplayerspawn &8→ &e/za set addzombiespawn &8→ &e/za save");
        this.send(player, "&7Optional: &e/za set addwindow <id> &8+ &e/za set adddoor <id> <price>");
        this.send(player, "&8+ &e/za set addwallbuy <gun> <price> <ammoPrice> &8+ &e/za set boxprice <price>");
        this.send(player, "&8+ &e/za set addboxlocation &7(look at the box block)");
    }

    private BlockFace lookFace(float yaw) {
        float normalized = ((yaw % 360.0f) + 360.0f) % 360.0f;
        int index = Math.round(normalized / 90.0f) % 4;
        return switch (index) {
            case 0 -> BlockFace.SOUTH;
            case 1 -> BlockFace.WEST;
            case 2 -> BlockFace.NORTH;
            default -> BlockFace.EAST;
        };
    }

    private Material firstSolidMaterial(World world, Cuboid cuboid) {
        for (int x = cuboid.minX(); x <= cuboid.maxX(); x++) {
            for (int y = cuboid.minY(); y <= cuboid.maxY(); y++) {
                for (int z = cuboid.minZ(); z <= cuboid.maxZ(); z++) {
                    Material type = world.getBlockAt(x, y, z).getType();
                    if (!type.isAir()) {
                        return type;
                    }
                }
            }
        }
        return null;
    }

    private void send(CommandSender sender, String message) {
        sender.sendMessage(MessageUtil.format(PREFIX + message));
    }
}
