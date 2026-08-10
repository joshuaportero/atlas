# Atlas Zombies — Plugin Design Document

**Status:** Draft v1
**Working title:** *Atlas Zombies* (a Call of Duty: Zombies–style survival minigame)
**Target platform:** Paper `26.2` (Minecraft "Chaos Cubed"), Java 25
**Codebase:** module of the `atlas` plugin (`dev.portero.atlas.zombies.*`), extractable to a standalone plugin later
**Last updated:** 2026-08-10

---

## 1. Overview

A round-based co-op survival minigame inspired by Call of Duty: Zombies. Players are
dropped into an arena, fight escalating waves of zombies, earn points for kills and
repairs, and spend those points to open up the map and gear up:

- **Guns** — ranged weapons built purely from vanilla items (no mods, no custom textures).
- **Doors** — buyable map progression gates that open new areas.
- **Reparable windows** — zombie entry points barricaded with planks; players rebuild them for points.
- **Wall-buys** — guns and ammo purchasable from marked blocks on walls.
- **Mystery box** — a random-weapon gamble that can relocate.
- **Drinks** — perk machines (Juggernog-style) granting persistent effects.
- **Zombie types** — multiple variants with distinct stats and behaviors.
- **Bosses** — a special boss wave every 10 rounds.
- **Events** — scripted mid-game scenarios (e.g., *protect the nexus*) driven by an extensible event system.

The plugin owns the **full map setup workflow**: an in-game setup wizard stores arena
definitions, and **WorldEdit** is used (soft-depend) for region selection, arena
snapshots, and map reset between games.

### Design pillars

1. **Vanilla-first** — everything works on a stock 26.2 client. No mods, no resource pack, no custom textures in v1.
2. **Feel** — guns must feel responsive (hitscan, recoil, sounds, ammo feedback) despite being vanilla items.
3. **Zero admin pain** — a map should go from "built" to "playable" in one in-game session.
4. **Systems, not scripts** — zombies, bosses, perks, and events are all data-driven and extensible.

---

## 2. Scope

### In scope (v1)

- Single-arena instances (one game per arena at a time), multi-arena support.
- Full game lifecycle: lobby → countdown → rounds → game over → reset.
- Points economy, guns, ammo, doors, windows, wall-buys, mystery box, drinks.
- Downed/revive/bleed-out flow, spectator on death.
- Zombie variants, boss rounds (every 10 rounds), event system with 2–3 built-in events.
- WorldEdit-assisted setup wizard, arena snapshot & restore.
- Scoreboard/action-bar HUD, sound design via vanilla sounds.

### Out of scope (v1)

- Custom models/textures, resource pack, `ITEM_MODEL` component usage (see §14).
- Cross-server/proxy (BungeeCord/Velocity) instancing.
- Matchmaking, parties, ranked play.
- Persistent stats/leaderboards (schema reserved, Postgres + HikariCP already on the classpath — phase 2).

---

## 3. Technology stack

| Concern | Choice | Notes |
| --- | --- | --- |
| Server API | Paper API `26.2` (`io.papermc.paper:paper-api:26.2.build.+`) | Already in `build.gradle.kts`; `apiVersion: "26.2"` |
| Language / JVM | Java 25 | Already configured via toolchain |
| Commands | LiteCommands 3.11.x | Already used; add a `zombies` command root |
| GUIs | triumph-gui 3.1.13 | Mystery box confirmation, perk menus, admin browsers |
| Scoreboard | scoreboard-library 2.8.0 | Sidebar HUD (round, points, zombies left) |
| Config | Bukkit YAML via existing `ConfigManager`/`ConfigType` | Add `ConfigType` entries per file |
| Map tooling | **WorldEdit 7.3+ (soft-depend, `compileOnly`)** | FAWE works transparently through the WE API |
| Build | Shadow 9.5.1, plugin-yml via `de.eldoria.plugin-yml.bukkit` | No new shaded deps for this module |

`plugin.yml` additions: `softdepend: [WorldEdit, FastAsyncWorldEdit]`. If WorldEdit is
absent, the plugin still loads — snapshot/restore features are disabled and the setup
wizard warns the admin (see §8.6).

---

## 4. Architecture

### 4.1 Package layout

```
dev.portero.atlas.zombies
├── ZombiesModule.java            // bootstrap: wires managers, registers listeners/commands
├── arena
│   ├── Arena.java                // immutable map definition (spawn points, doors, windows, ...)
│   ├── ArenaRepository.java      // loads/saves arenas/<id>.yml (+ .schem)
│   ├── setup
│   │   ├── SetupSession.java     // per-admin setup wizard state machine
│   │   ├── SetupStep.java        // LOBBY_SPAWN → PLAYER_SPAWNS → ZOMBIE_SPAWNS → WINDOWS → ...
│   │   └── ArenaValidator.java   // completeness checklist before an arena goes live
│   └── worldedit
│       ├── WorldEditHook.java    // capability detection (WE present? FAWE present?)
│       ├── SelectionService.java // read player selections from WE sessions
│       ├── SnapshotService.java  // clipboard capture of arena region
│       └── RestoreService.java   // async/chunked paste on game reset
├── game
│   ├── GameManager.java          // active sessions, join/leave, lookup by player/world
│   ├── GameSession.java          // one running game; owns all per-game state
│   ├── GameState.java            // LOBBY, STARTING, IN_ROUND, INTERMISSION, BOSS, EVENT, GAME_OVER, RESETTING
│   ├── GameTicker.java           // single per-session repeating task driving time-based logic
│   ├── PlayerSession.java        // per-player in-game state: points, loadout, perks, downs
│   ├── WindowState.java          // per-game barricade state (pure logic, world writes in GameSession)
│   └── RoundDirector.java        // round math: zombie count, health scaling, spawn pacing
├── combat
│   ├── gun
│   │   ├── GunDefinition.java    // config-backed: damage, fire rate, mag, reserve, sounds
│   │   ├── GunRegistry.java
│   │   ├── GunService.java       // fire/reload/ammo; hitscan via World#rayTrace
│   │   └── RecoilService.java
│   ├── ZombieType.java           // config-backed variant definition
│   ├── ZombieRegistry.java
│   ├── ZombieService.java        // spawning, gear, attribute scaling
│   └── ai
│       └── BossSkillScheduler.java   // (M5) boss skill loops
│                                             // note: barricade tearing is ticker-driven in GameSession
│                                             // (proximity-based), not a MobGoals goal — see §7.5
├── economy
│   ├── PointsService.java        // award/spend, anti-farming rules
│   └── purchase
│       ├── Purchasable.java      // door, wall-buy, box roll, drink — uniform "interact to buy"
│       └── PurchaseListener.java
├── interactable                  // (M3+) WallBuy.java, MysteryBox.java, DrinkMachine.java
│                                 // doors/windows live as DoorDef/WindowDef in arena + runtime state in GameSession
├── perk
│   ├── Perk.java  ├── PerkRegistry.java  └── PerkService.java
├── boss
│   ├── BossDefinition.java  ├── BossRegistry.java  └── BossService.java
├── event
│   ├── GameEvent.java          // interface: onStart/onTick/onEnd, win/lose conditions
│   ├── EventRegistry.java
│   ├── EventScheduler.java
│   └── impl
│       ├── NexusDefenseEvent.java  ├── PowerOutageEvent.java  └── DoublePointsEvent.java
├── hud
│   ├── HudService.java         // sidebar + action bar ammo + boss bars
└── listener/                   // Bukkit listeners, thin — delegate to services
```

### 4.2 Core model

- **`Arena`** = static, shareable map definition. Never mutated by gameplay.
- **`GameSession`** = one live instance of an arena. Holds players, round number, alive
  zombies, runtime state of doors/windows, active event/boss, and the tick task.
- **`PlayerSession`** = per-player run state: points, current gun(s) + ammo, owned perks,
  downed/alive/spectating status.

This separation keeps arenas cheap to reload and games fully disposable: ending a game
discards the session and restores the world from the snapshot.

### 4.3 Game state machine

```
LOBBY ──(min players / force start)──▶ STARTING ──(countdown)──▶ INTERMISSION
   ▲                                                                  │
   │                                                          (timer expires)
   │                                                                  ▼
RESETTING ◀── GAME_OVER ◀── (all players dead/bled out) ◀── IN_ROUND ◀──┘
   │                                                          │  ▲
   └──(world restored, session closed)──▶ LOBBY        (all zombies killed)
                                                              │
                              BOSS round (round % 10 == 0) ───┤
                              EVENT window (scheduler) ───────┘
```

Rules:

- Rounds `10, 20, 30, …` are **boss rounds** (boss + light trash waves instead of a normal wave).
- The **EventScheduler** may attach an event to configurable round windows (e.g., a defense
  event on rounds 5–7). Events and bosses compose: an event can modify a boss round (see §7.9/§7.10).
- `RESETTING` blocks joins until `RestoreService` signals completion.

### 4.4 Ticking

One `BukkitRunnable` per `GameSession` at 5–10 Hz for coarse logic (timers, spawn pacing,
HUD refresh); precision work (gun fire, hit detection) stays event-driven. No per-zombie
Bukkit tasks — zombie behavior lives in Paper's `MobGoals`/pathfinder, driven by the entity
AI thread, not the scheduler.

---

## 5. Vanilla-item gameplay strategy (no mods, no textures)

Since v1 ships zero client-side assets, all "custom" feel comes from:

- **Item identity** — vanilla `ItemStack` + custom name/lore (Adventure components) +
  `PersistentDataContainer` tags (`atlas:gun_id`, `atlas:ammo_mag`, `atlas:ammo_reserve`).
- **Durability bar as ammo gauge** — `Damageable` meta maps magazine state onto the item's
  durability bar; instantly readable without a resource pack.
- **Sound design** — vanilla `Sound` effects pitch-shifted per gun (e.g., `ENTITY_GENERIC_EXPLODE`
  for shotguns, `ENTITY_ARROW_SHOOT`/`BLOCK_ANVIL_LAND` layering for rifles, `BLOCK_IRON_DOOR_*` for reloads).
- **Particles** — `Particle` trails for tracers, muzzle flash at the eye location.
- **Camera kick** — small rotation nudge (`Player#rotate`/`teleport` with yaw/pitch delta) for recoil.
- **HUD** — action bar for ammo (`|██████----| 24/120`), sidebar for round/points, `BossBar` for bosses and event timers.

> The item-to-gun mapping table (§7.2) chooses vanilla items that *read* correctly
> (crossbow, hoes as rifles, blaze rod as sniper, …) so nothing looks broken without textures.

---

## 6. Map setup & WorldEdit integration

The plugin owns setup end-to-end. A map maker builds the map in-game or in WE, then runs
the wizard. No manual file editing required (but files stay human-readable).

### 6.1 Arena definition

```yaml
# plugins/Atlas/zombies/arenas/nacht.yml
id: nacht
displayName: "Nacht der Untoten"
world: zombies_nacht
region: { min: [0, 60, 0], max: [80, 100, 80] }      # WE cuboid, snapshot boundary
lobbySpawn: [40.5, 64, 12.5, 0, 0]
playerSpawns: [[40.5, 64, 40.5, 0, 0], ...]           # up to maxPlayers
zombieSpawns:                                          # tagged so windows can prefer "their" spawn
  - { pos: [4.5, 64, 20.5], window: window_a }
  - { pos: [76.5, 64, 20.5], window: window_b }
windows:
  window_a:
    min: [8, 64, 19]                                     # plank layer only
    max: [10, 66, 19]
    spawn: { x: 4.5, y: 64, z: 20.5, yaw: 0, pitch: 0 }  # exterior; this window's zombies appear here
    material: OAK_PLANKS                                 # captured from the region at setup time
doors:
  door_stairs:
    min: [30, 64, 30]
    max: [32, 67, 30]
    price: 750
    opensTo: upstairs                                   # informational grouping
wallBuys:
  - { gun: rifle_m1, pos: [12, 65, 25], facing: NORTH, price: 600, ammoPrice: 300 }
mysteryBox:
  price: 950
  locations: [[50.5, 64, 50.5], [60.5, 71, 20.5]]      # first = initial, rest = move targets
drinks:
  juggernog: { pos: [70.5, 64, 60.5], facing: SOUTH }
  speed_cola: { pos: [22.5, 64, 55.5], facing: EAST }
powerSwitch: [65.5, 64, 10.5]                          # optional: gates perks/box until flipped
settings:
  minPlayers: 1
  maxPlayers: 4
  startingPoints: 500
```

### 6.2 Setup wizard flow

`/za setup <id>` puts an admin into a `SetupSession` (state machine, one per admin):

1. **Region** — uses the admin's current WorldEdit selection (`SelectionService` reads the
   WE `LocalSession` cuboid). No wand of our own to maintain.
2. **Lobby spawn / player spawns** — stand and confirm.
3. **Zombie spawns** — stand and confirm (fallback spawns, used when no windows are defined).
4. **Windows** — WE-select the plank blocks, stand where the window's zombies should spawn
   (outside), then `/za set addwindow <id>`; the barricade material is captured from the region.
5. **Doors** — WE-select the door blocks, then `/za set adddoor <id> <price>`.
6. **Wall-buys** — look at the wall block, `/za set addwallbuy <gun> <price> <ammoPrice>`.
7. **Mystery box** — `/za set boxprice <price>`, then per location: look at the box block,
   `/za set addboxlocation` (first location is the initial one).
8. **Drinks** — look at the machine block, `/za set adddrink <perkId>`.
9. **Power switch** (optional) — look at the block, `/za set powerswitch`.
8. **Validate** — `ArenaValidator` prints a checklist (✔ region, ✔ ≥1 spawn, ✘ no doors…);
   missing required items block activation.
9. **Save** — writes `arenas/<id>.yml` **and** captures the region snapshot via
   `SnapshotService` to `arenas/<id>.schem` (Sponge schematic v3 through WE's
   `ClipboardFormats`).

Every step is `/za set ...`-addressable individually for later edits, and `/za setup` can
be re-entered any time.

### 6.3 Snapshot & restore strategy

- **Capture:** `ForwardExtentCopy` of the region into a `BlockArrayClipboard`, written once
  at save time (not per game) — disk read per reset, no per-capture cost.
- **Restore on game end / abort:** the region is split into chunk columns and pasted back a
  few chunks per tick (configurable budget, `restoreChunksPerTick`) through per-batch WE
  `EditSession`s to avoid lag spikes; joins stay blocked in `RESETTING` until done. FAWE is
  detected and logged; routing the paste through FAWE's native async queue is a later
  optimization (the budgeted paste is safe on both).
- **Delta tracking (optional optimization, later):** since gameplay only ever mutates
  *known* blocks (door regions, window planks, box position), a listener-based block-change
  log could restore only diffs. Design keeps restore behind `RestoreService` so this can
  swap in without touching game logic. **v1 always full-pastes: simple and bulletproof.**
- Zombies/projectiles/dropped items inside the region are purged as part of reset; the
  arena world should be dedicated (no survival gameplay in it).

### 6.4 WorldEdit failure modes

| Situation | Behavior |
| --- | --- |
| WE absent at startup | Module loads; `/za setup` and arena activation disabled with a clear log/message. |
| WE absent but arenas already configured | Arenas can still *run* (doors/windows are plain block edits); reset falls back to delta restore of known regions only, with a loud warning. Snapshot paste unavailable. |
| Schematic file missing/corrupt | Arena fails validation, refuses to activate. |
| FAWE vs WE | Same API; FAWE is auto-detected for the async path. No hard dependency on either flavor. |

---

## 7. Gameplay systems

### 7.1 Rounds & the RoundDirector

Per round `n`, the director computes:

- **Zombie count:** `base + round-1 * perRound + perPlayer * (players-1)` (configurable curve).
- **Health multiplier:** smooth curve `1 + (n-1)*0.15`, jumping to exponential past round 20
  (CoD-style); applied via `GENERIC_MAX_HEALTH` attribute, never by re-gearing.
- **Speed tiers:** slow → walker → sprinter mix, percentage shifting with rounds.
- **Spawn pacing:** trickle spawns (interval shrinks per round), cap on concurrently alive
  zombies; round ends when spawned == total && alive == 0.

Intermission between rounds: configurable seconds, repair-your-windows window, round
number shown with the classic sting sound.

### 7.2 Guns

Hitscan-first design: firing raycasts (`World#rayTraceBlocks` + `rayTraceEntities`) from
the eye along the look vector with per-gun spread — instant feedback, no projectile travel
desync, cheap on the server. (Explosive/"launcher" guns spawn a real projectile instead.)

- **Fire:** right-click (`PlayerInteractEvent`), click-per-shot semi-auto with a per-gun
  fire interval (v1 decision — see §15.4). Interactables win over firing: right-clicking a
  door/mystery box/wall-buy uses it; anything else fires the held gun. Guns with
  `aimDownSights: true` (Kar98k) keep the vanilla spyglass scope on plain right-click and
  fire on sneak + right-click.
- **Ammo:** mag + reserve in the item's PDC; the durability bar mirrors the magazine
  (damageable materials only), the lore line always shows `mag / reserve`.
  `PlayerItemDamageEvent` is cancelled for guns — the bar is an ammo gauge, not wear.
- **Reload:** `F` (swap-hand, cancelled) or auto on empty trigger pull; per-gun reload time
  with sound cues; cancelled on slot switch/drop/quit. Reload-perk (Speed Cola) multiplies
  speed later via a duration multiplier hook.
- **Recoil & spread:** per-gun camera kick (pitch); per-shot cone spread.

Starter roster (all vanilla items):

| Gun | Vanilla item | Class | Notes |
| --- | --- | --- | --- |
| M1911 | `WOODEN_SHOVEL`-style sidearm → **`IRON_HOE`** | Pistol | starter, weak, fast |
| M1 Garand | **`CROSSBOW`** (never actually loaded) | Semi rifle | "ping" sound on empty |
| Thompson | **`IRON_SHOVEL`** | SMG | high fire rate |
| Trench Gun | **`BLAZE_ROD`** | Shotgun | multi-ray pellet spread |
| Kar98k | **`SPYGLASS`** | Sniper | high dmg, slow cycle, scope = spyglass zoom! |
| Ray Gun (box-only) | **`AMETHYST_SHARD`** | Wonder | explosive projectile, self-damage |
| Molotov (equipment) | **`SPLASH_POTION`** (fire) | Lethal | area denial, nice vs barricade crowds |

All stats (damage, headshot multiplier, RPM, mag, reserve, reload, price, box weight)
live in `guns.yml` — adding a gun is config-only.

### 7.3 Points economy

- Kill: by zombie type (60–130); headshot bonus; melee bonus (risk/reward).
- Hit markers: small points per hit so bullet-hoses still pay.
- Window repair: per plank.
- Event objectives: lump rewards (nexus saved, etc.).
- Downed penalty: lose a percentage / perks on bleed-out.
- `PointsService` is the single mutation point (auditable, event-hookable, anti-double-dip).

### 7.4 Doors

Interacting with any block in a door region prompts the purchase (action bar + click to
confirm). On buy: the region's blocks are set to air row-by-row (bottom-up, 2 ticks/row)
with a stone-slide sound — reads as the door "opening" and costs nothing client-side.
Doors are per-game runtime state; the snapshot restore re-seals them. Optional
`requiresPower` flag for map flow control.

### 7.5 Reparable windows

- Window = WE-defined region of plank blocks + its own exterior zombie spawn
  (`WindowDef`); plank material captured at setup so repairs match the map.
- Each spawned zombie is assigned a random window and appears at its exterior spawn.
  Movement stays **vanilla** (zombies crowd the barricade while pathing to players);
  the session ticker tears **one plank per window per interval** for zombies physically
  pressing against it (`breakIntervalTicks`, reach ~2.6 blocks). This proved more robust
  than a custom `MobGoals` pathing goal, which fights vanilla target acquisition.
- Players **sneak** next to a damaged window to rebuild it plank-by-plank
  (`repairCooldownMillis` per plank, `points.repair` each). Repair pauses when the
  player stands up, moves out of `repairReach`, or a zombie is within
  `zombieBlockReach` of the window — kill the zombie at the window first, then repair.
- Fully open windows let zombies path inside; the window set per room creates the classic
  "which do I watch" pressure.
- In-game block breaking/placing inside the arena region is cancelled (arena protection).

### 7.6 Wall-buys

Defined by block position + facing (`/za set addwallbuy <gun> <price> <ammoPrice>` while
looking at the wall block). The gun renders as an **invisible, glowing, fixed `ItemFrame`**
on the wall face — cheap, protected (`setFixed` + invulnerable + cancelled damage/hanging
events), zero client mods. Click = buy the gun; when already owned, click = full reserve
refill at `ammoPrice`. Frames spawn on session start and are removed (plus region-purged)
on game end.

### 7.7 Mystery box

- A decorated block (e.g., `DECORATED_POT`/ender chest) at one of N configured locations.
- Pay → lid animation (sound + particles) → an `ItemDisplay` above the box cycles through
  gun items for ~4s, decelerating → final weapon hovers until taken or times out.
- **Firework/"teddy" outcome (config %):** box explodes in fireworks and **relocates** to
  another configured location — the CoD box-move, trivial with multiple configured locations.
- Weighted table in `guns.yml` (`boxWeight`), wonder weapons low-weight.

### 7.8 Drinks (perks)

Machines are interactable blocks (brewing stand look). Each perk is a modest vanilla
potion effect / attribute tweak while owned, lost on bleed-out:

| Drink | Vanilla effect | Price |
| --- | --- | --- |
| Juggernog | +2 downs worth of health (`HEALTH_BOOST` + resistance tweak) | 2500 |
| Speed Cola | 2× reload speed | 3000 |
| Quick Revive | self-revive once (solo) / 2× revive speed (co-op) | 500/1500 |
| Double Tap | +fire rate (shorter fire cooldown) | 2000 |
| Stamin-Up | `SPEED` I + jump QoL | 2000 |
| Deadshot | tighter spread + headshot bonus up | 1500 |

Max perk slots configurable (default 4, CoD-style). When the arena defines a
**power switch** (`/za set powerswitch`), drink machines and the mystery box are gated
behind it; without one, power is always on. Perk effects: Juggernog sets
`MAX_HEALTH` base to 40, Stamin-Up is an infinite `SPEED` effect, Speed Cola / Double Tap /
Deadshot are multipliers queried by `GunService` (reload ×0.5, fire interval ×0.75,
spread ×0.7, headshot ×1.25), Quick Revive speeds revives ×0.5 and enables solo self-revive.

**Power-up drops (implemented):** 3% per kill (`powerups.dropChance`) — Max Ammo,
Insta-Kill, Double Points (timed, `durationSeconds`), Nuke. Drops are glowing
`ItemDisplay`s picked up by walking through them (`pickupReach`, despawn after
`despawnSeconds`).

### 7.9 Bosses (every 10th round)

- Boss rounds replace the normal wave: boss + trickle trash.
- Boss = scaled vanilla mob with attribute overrides + a **skill loop** run by
  `BossSkillScheduler` (telegraphed: particles/sound wind-up → effect):
  - *Ground slam* — AoE damage + knockback ring.
  - *Summon* — calls a small zombie pack.
  - *Charge* — pathfinder sprint at a targeted player.
  - *Enrage* — below 25% HP: speed/damage up.
- `BossBar` shows HP; big point payout split among participants; guaranteed power-up drop.
- Bosses are config-defined (`bosses.yml`: base mob, scale, skills + cooldowns), so new
  bosses are data. First boss: **"The Brute"** (giant-scaled zombie, slam + summon).

### 7.10 Event system

Events are first-class, composable scenario modules — the same hook set the rest of the
game already exposes.

```java
public interface GameEvent {
    String id();
    void onStart(GameSession session);
    void onTick(GameSession session);          // driven by GameTicker
    void onEnd(GameSession session, EventResult result);
    boolean isFinished(GameSession session);   // win/lose/timeout evaluated per tick
}
```

- `EventScheduler` picks events per arena config: eligible round windows, probability,
  cooldown between events, and whether they may overlap boss rounds.
- Events get hooks other systems already publish (round start/end, zombie killed, points
  awarded) so they can modify rules temporarily.

Built-in launch events:

| Event | Description | Fail state |
| --- | --- | --- |
| **Nexus Defense** | A nexus block (beacon-style, HP-tracked) spawns in a room; zombies periodically retarget it for N rounds/waves. Players split defense vs. kiting. | Nexus HP → 0 (points penalty / perk loss, not instant game over — configurable) |
| **Power Outage** | Machines, box and door purchases dark for 90s; zombie spawns surge. | — (survival only) |
| **Double Points Frenzy** | All points ×2, spawn rate up. | — |

### 7.11 Zombie types

Defined in `zombies.yml` (base attributes, gear, spawn weight, round floor, points):

| Type | Vanilla base | Twist |
| --- | --- | --- |
| Shambler | `ZOMBIE` | default; speed tiers by round |
| Sprinter | `ZOMBIE` (baby=false, speed attr) | fast, lower HP |
| Brute | `ZOMBIE` scaled 1.3× | high HP, knockback resist, slow |
| Crawler | `ZOMBIE` forced `SWIMMING` pose | low profile, spawns after explosions |
| Spitter | `WITCH` (custom goals) | ranged "acid" (harming splash) — pressure at windows |
| Bomber | `CREEPER` re-skinned via name/glow | walks to players/windows and detonates; points only if killed before fuse |

All spawned with `PersistentDataContainer` marks (`atlas:zombie_type`, `atlas:game_id`) so
cleanup, points attribution, and event logic are exact. Custom AI installed via Paper
`MobGoals` (target nearest *in-game* player, barricade breaking, nexus targeting during
events) — vanilla spawning rules/light level irrelevant since we spawn explicitly.

### 7.12 Player flow: downed, revive, spectate (implemented)

- Fatal damage is intercepted (`EntityDamageEvent`, final damage ≥ health → cancelled) and
  puts the player into **downed**: 1 HP, crawl pose + heavy slowness, 30s bleed-out timer.
  Zombies lose interest in downed/dead players (target events cancelled); downed players
  take no further damage but can still shoot.
- **Revive:** a teammate sneaks next to the downed player (`downed.reviveSeconds`,
  `downed.reviveReach`); progress shows on both action bars and resets when the reviver
  stops. Quick Revive halves the time. Revive keeps perks.
- **Bleed-out:** real death → spectator; perks are stripped; the player **respawns at the
  next round start** (co-op). Solo bleed-out / everyone downed simultaneously → `GAME_OVER`.
- **Solo Quick Revive:** going down alone with the perk triggers a self-revive after
  `downed.selfReviveSeconds` and consumes the perk (solo price 500 via `soloPrice`).

---

## 8. Configuration files

| File | Contents |
| --- | --- |
| `zombies/config.yml` | global: tick rates, restore chunk budget, defaults (points, revive times) |
| `zombies/guns.yml` | gun definitions + box weights |
| `zombies/zombies.yml` | zombie types + round scaling curve params |
| `zombies/perks.yml` | drink definitions |
| `zombies/bosses.yml` | boss definitions + skill tuning |
| `zombies/events.yml` | event definitions, windows, probabilities, nexus HP |
| `zombies/arenas/<id>.yml` | per-arena definition (§6.1) |
| `zombies/arenas/<id>.schem` | region snapshot (WorldEdit Sponge schematic) |

All loaded through the existing `ConfigManager`/`ConfigType` pattern; per-domain record
classes (e.g., `GunDefinition`) parse + validate on load and fail loudly with file/line context.

---

## 9. Commands & permissions (LiteCommands)

```
/za join <arena>                 atlas.zombies.play
/za leave                        atlas.zombies.play
/za start [arena]                atlas.zombies.admin.start
/za setup <arena>                atlas.zombies.admin.setup      (wizard entry)
/za set <...>                    atlas.zombies.admin.setup      (individual steps)
/za save / /za cancel            (inside setup session)
/za reload                       atlas.zombies.admin.reload
/za debug spawns|regions [arena] atlas.zombies.admin.debug      (particle outlines)
```

Argument resolvers for `Arena`, `GunDefinition`, `BossDefinition` plug into the existing
LiteCommands setup; invalid-usage/permission output reuses `CustomInvalidUsageHandler` and
`MissingPermissionHandler`.

---

## 10. Persistence (phase 2)

`HikariCP` + Postgres are already dependencies. v1 computes a game-summary at match end and
logs it; phase 2 persists: rounds survived, kills, headshots, downs/revives, points earned,
per-player per-arena best round. Schema is designed up front (append-only `match`,
`match_player` tables) so no migration pain later.

---

## 11. Performance notes

- One tick task per game; zero per-entity schedulers; AI on Paper's goal system.
- Hitscan raytrace is O(few blocks) — cheaper than projectile entity tracking.
- ItemDisplay entities (wall-buys, box) are few and static; no armor-stand spam.
- Restore budgeted per tick (plain WE) or async (FAWE); `RESETTING` gates re-entry.
- Zombie cap alive (config) keeps entity counts bounded; garbage-collect dead-game
  entities by `atlas:game_id` PDC tag.
- Dedicated arena worlds recommended; region purge on reset removes drops/orbs/arrows.

---

## 12. Testing

- **Unit (JUnit):** round-scaling math, points rules, gun stat parsing, arena validation,
  event win/lose predicates. Pure logic classes take no Bukkit types (or use MockBukkit).
- **Integration:** `run-paper` (`./gradlew runServer`, already configured) with WorldEdit in
  `run/plugins`; a scripted test arena under `run/` for smoke tests.
- **Manual checklist** per release: full game loop, reset integrity (diff world vs schem),
  box relocation, boss round, nexus event.

---

## 13. Roadmap

| Milestone | Deliverable |
| --- | --- |
| **M1 — Skeleton** ✅ | Arena model + repository, setup wizard (region/spawns), WE snapshot & restore, game lifecycle, basic rounds of vanilla zombies, points, join/leave. |
| **M2 — The map fights back** ✅ | Windows + barricade tearing, doors (confirm-to-buy), arena block protection. |
| **M3 — Gunplay** ✅ | GunService v1 (hitscan/ammo/reload/recoil), starter gun roster, wall-buys, mystery box. |
| **M4 — Co-op depth** ✅ | Drinks/perks, downed/revive/bleed-out, round respawn, power switch, power-up drops (Max Ammo, Insta-Kill, Double Points, Nuke). |
| **M5 — Bosses** | Boss framework + "The Brute", boss rounds every 10. |
| **M6 — Events** | Event framework + Nexus Defense, Power Outage, Double Points. |
| **M7 — Polish** | HUD/sound pass, stats persistence (Postgres), leaderboards, balance pass. |

M1–M3 is the vertical slice: one arena, playable start-to-finish.

---

## 14. Future: custom textures & models (post-v1)

Deliberately deferred but designed for:

- All item identity flows through `GunRegistry`/`PerkRegistry` → swapping a vanilla icon for
  a `ITEM_MODEL`-component custom model is a config change, not a code change.
- 26.x pack formats (resource `88.0`) support per-item models cleanly; we'd ship an optional
  companion resource pack and key entries in `guns.yml` (`model: atlas:m1_garand`).
- Zombie visual variants likewise hang off `ZombieType` (armor/items today, models later).
- Server-side pack distribution via `Player#sendResourcePack` (Paper) when the time comes.

---

## 15. Open questions

1. **Lives model:** strict CoD bleed-out-per-round, or limited team respawn tokens? (Default: strict, configurable.)
2. **Multiple concurrent games in one world?** v1 assumes dedicated world per arena (simplest restore); region-keyed multi-arena worlds are possible later since all state is region-scoped.
3. **Nexus fail consequence:** penalty vs. instant loss — ship both, default penalty.
4. **Auto-fire input:** resolved for v1 — click-per-shot semi-auto with per-gun
   `fireIntervalMillis` (hold-right-click detection is unreliable server-side). If playtests
   demand full-auto, candidates are swing-packet-driven fire or a hold-detect heuristic.
5. **Pack-a-Punch equivalent?** Upgrade machine is thematically expected — candidate for M7+ using the same purchase interface.
