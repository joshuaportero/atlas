# Atlas Design Document

Atlas is the core RPG plugin for combat, skills, MMO progression, parties, chat, and world events.

This document is the development contract. Every system is modeled on a premium plugin, then implemented inside Atlas so the server does not depend on a pile of overlapping jars.

**Explicit non-goals**

- No clan system
- No guild system
- Parties only

---

## 1. Product

Atlas is a Paper 26.2 RPG core. Players join, load a profile, fight with custom stats, level skills by playing Minecraft, group up in parties, talk through Vault-backed chat, and compete in world events such as King of the Hill.

Version: `0.0.1-DEV`

Stack already in the repo:

- Modular bootstrap (`AtlasModule`, `ServiceRegistry`)
- Join/quit pipelines (`player.ready`, `player.close`)
- Component profiles persisted through HikariCP (SQLite / MySQL / PostgreSQL)
- Internal `EventBus` plus Bukkit events
- LiteCommands, Scoreboard Library, PlaceholderAPI, FancyHolograms

---

## 2. Premium mapping

Every Atlas system copies the *feature set and UX* of a known premium/production plugin. Atlas owns the code. External plugins are only APIs or optional hooks.

| Atlas system | Reference | Why this one |
|---|---|---|
| Skills | [AuraSkills](https://www.spigotmc.org/resources/auraskills.81069/) | XP sources, passives, mana abilities, rewards, jobs, menus, leaderboards |
| Stats / traits | AuraSkills stats | Strength, Health, Toughness, Luck, Wisdom, Regen, Crit, Speed |
| Combat | [MythicLib](https://www.spigotmc.org/resources/mythiclib.90306/) + AuraSkills Fighting | Damage types, crit, mitigation, skill casts, indicators |
| Resources | AuraSkills mana + MMOCore resources | Mana / stamina / energy, regen, consume on abilities |
| Account level | [MMOCore](https://www.spigotmc.org/resources/mmocore.90536/) player level | Power level, XP curve, stat points |
| Party | [Parties](https://modrinth.com/plugin/parties) | Groups only. No clans, no guilds, no fixed factions |
| Chat | Vault Chat API + [VaultChatFormatter](https://www.spigotmc.org/resources/vaultchatformatter.49016/) / LPC | Prefix/suffix from LuckPerms via Vault. Atlas formats chat |
| Economy | Vault Economy | Skill rewards, jobs, event payouts. Atlas is not a bank |
| Permissions | Vault Permissions + LuckPerms | Rank checks, command costs later |
| World events | Blood Moon / seasonal event plugins | Timed global modifiers with opt-in |
| KOTH | [KoTH (SubSide)](https://www.spigotmc.org/resources/koth.15024/) | Hill, capture, contest, score, rewards |
| HUD | AuraSkills action/boss bars | Health, mana, XP gain |
| Scoreboard | [FeatherBoard](https://www.spigotmc.org/resources/featherboard.2691/) | Animated boards, PAPI, per-world |
| Menus | AuraSkills menus + [DeluxeMenus](https://www.spigotmc.org/resources/deluxemenus.11734/) | YAML layouts, click actions |
| Holograms | FancyHolograms | Floating damage (already hooked) |
| Placeholders | PlaceholderAPI | `%atlas_*%` for every system |
| Leaderboards | AuraSkills leaderboards | Per-skill, power, KOTH |
| Loot / jobs | AuraSkills loot + jobs | Extra drops, Vault money while skilling |
| Config / messages | AuraSkills + CMI | MiniMessage, per-language files, reload |

**Hard dependencies (future):** Vault

**Soft dependencies (current + future):** PlaceholderAPI, FancyHolograms, LuckPerms (through Vault)

---

## 3. Architecture rules

These rules do not change across stages.

1. One `AtlasModule` per system. Load registers services. Enable binds listeners and tasks. Disable flushes.
2. Player data lives on `Profile` components. Session state lives on `AtlasPlayer`.
3. Join work goes through `player.ready`. Quit work goes through `player.close`.
4. Combat and stats go through pipelines, not ad-hoc listeners.
5. Cross-module talk uses `EventBus` or registered services. No module reaches into another module's internals.
6. Player-facing text is MiniMessage. No new legacy `&` strings.
7. Definitions (skills, events, menus, boards) are YAML. Code is the engine.
8. Commands are LiteCommands. Menus are the primary player UX.
9. Persistence is async. The main thread never talks JDBC.
10. Secrets never land in git or logs.

---

## 4. Current snapshot (Stage 0)

What already exists. Everything below this section is future work unless marked done.

| System | State |
|---|---|
| Bootstrap / modules | Done |
| Database + profile components + autosave | Done |
| Player attach/detach + settings | Done |
| Stats (10 types, modifiers, spend points) | Foundation |
| Resources (mana, stamina, energy + regen) | Foundation |
| Account level (linear XP, points per level) | Foundation |
| Skills | 4 hardcoded loadouts, 3 equip slots, stat modifiers only |
| Combat pipeline (physical/magic/true, crit, defense, absorption) | Foundation |
| Damage holograms via FancyHolograms | Done (optional) |
| Party create/invite/accept/deny/leave/kick/disband/chat | Foundation |
| World events (blood moon, iron veil, arcane surge, koth) | Foundation, hardcoded |
| KOTH hill + contest + score + XP reward | Foundation |
| Custom inventory menus (player + admin) | Foundation, Java-built |
| PlaceholderAPI (`stat_`, resources, level, xp) | Thin |
| Scoreboard library loaded | No boards |
| Chat | Missing |
| Vault | Missing |
| HUD / action bar | Missing |
| Leaderboards | Missing |
| Skill XP sources / abilities / loot / jobs | Missing |
| YAML content files | Missing (`skills.yml` etc.) |

---

## 5. Global phases

Work is shipped in phases. A system may sit in several phases.

| Phase | Name | Goal |
|---|---|---|
| **0** | Foundation | Current repo. Do not regress it. |
| **1** | Platform | Vault, MiniMessage lang, YAML content loader, HUD, scoreboard, chat |
| **2** | Skills loop | AuraSkills-style skills, sources, passives, mana abilities, rewards |
| **3** | Group play | Parties parity, friendly fire, party XP, party HUD |
| **4** | Events | Config events, real KOTH, schedules, rewards |
| **5** | Economy loop | Vault money, jobs, loot tables, leaderboards |
| **6** | Polish | Reload, backups, API, admin tools, docs |

Phase 1 is the next coding target. Skills content without chat/HUD/config files is unusable.

---

## 6. Systems

Each system has:

- Reference plugin
- Current state
- Target design
- Ordered stages (S0 = now, later stages are TODO)

---

### 6.1 Platform — Vault, config, language

**Reference:** Vault, AuraSkills config layout, CMI messages

**Current:** `config.yml` + stub `data.yml`. `ConfigType` also lists `scoreboard.yml` with no resource. Messages are hardcoded legacy strings in `Messages.java`. No Vault.

**Target**

- Hard-depend Vault.
- LuckPerms provides Chat + Permissions through Vault.
- An economy plugin (EssentialsX, CMI, or similar) provides Economy through Vault. Atlas does not store balances.
- Split configs:

```
plugins/Atlas/
  config.yml
  skills.yml
  stats.yml
  abilities.yml
  sources.yml
  combat.yml
  party.yml
  chat.yml
  events.yml
  koth.yml
  hud.yml
  scoreboard.yml
  menus/
  rewards/
  loot/
  lang/en_us.yml
```

- `/atlas reload` hot-reloads definitions. Profiles and DB stay up.

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Done | `config.yml` database, stats defaults, resources, combat toggle, leveling, koth radius |
| S1 | TODO | Add Vault to `build.gradle.kts` and `softDepend`/`depend`. Fail soft if Chat/Economy missing, hard if Vault missing |
| S2 | TODO | MiniMessage `lang/en_us.yml`. Replace `Messages.java` constants with keyed lookups |
| S3 | TODO | YAML definition loader per module. Move hardcoded skills/events out of Java |
| S4 | TODO | `/atlas reload` with per-module reload hooks |
| S5 | TODO | Config version + auto-migrate old keys (AuraSkills-style) |

---

### 6.2 Data and profiles

**Reference:** AuraSkills SQL, Parties multi-DB

**Current:** HikariCP, SQLite/MySQL/PostgreSQL, `Profile` + JSON-ish component payloads, migrations, autosave, join pipeline.

**Target:** Keep the component model. Add backups, schema per new system, optional Redis later (out of scope until multi-server).

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Done | Profiles, sessions, autosave, flush on quit/stop |
| S1 | TODO | Components for skill XP, party extras, chat settings, cooldowns that must persist |
| S2 | TODO | Scheduled file backups of SQLite / SQL dumps (AuraSkills backups) |
| S3 | TODO | Admin `/atlas backup` and `/atlas restore` (offline, confirm) |
| S4 | TODO | Index UUID + name lookups for offline admin menus |

---

### 6.3 Player

**Reference:** MMOCore player data, AuraSkills user

**Current:** `AtlasPlayer` holds stats snapshot, modifiers, resources, cooldowns, party id, absorption, settings (combat feedback, regen, event opt-in).

**Target:** Same object. Add chat channel, HUD flags, ignored players, selected job, action-bar toggles.

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Done | Attach on ready, detach on close, settings component |
| S1 | TODO | Settings: scoreboard, hud, chat mentions, party notifications |
| S2 | TODO | Persistent cooldowns for mana abilities (survive reconnect) |
| S3 | TODO | `/profile` shows level, power, equipped abilities, party, job |

---

### 6.4 Stats

**Reference:** [AuraSkills stats](https://wiki.aurelium.dev/auraskills/stats/)

AuraSkills splits **stats** (player-facing levels) from **traits** (the actual effect). Atlas already has a modifier pipeline (`FLAT` / `PERCENT`). Keep it. Add the missing AuraSkills stats and treat current combat numbers as traits.

**Keep (already in Atlas)**

- `strength`, `defense`, `crit_chance`, `crit_damage`
- `attack_damage`, `magic_power`
- `max_health`, `max_mana`, `max_stamina`, `max_energy`

**Add**

| Stat | Traits |
|---|---|
| Health | max HP (exists), heart scaling |
| Regeneration | HP regen, mana regen bonus |
| Luck | extra-drop chance per gathering skill |
| Wisdom | XP bonus, max mana bonus |
| Toughness | maps onto `defense` formula |
| Speed | walk speed |
| Magic | `magic_power` as a first-class stat |

Modifiers stay sourced (`skill:fighting`, `event:blood_moon`, `item:sword`, `ability:berserk`). Recalc pipeline stays.

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Done | Registry, base + modifiers, clamp, health attribute, spend points |
| S1 | TODO | `stats.yml`. Enable/disable per stat. Display names |
| S2 | TODO | Trait layer: stat level * modifier = trait value (AuraSkills) |
| S3 | TODO | Apply speed, regen, XP bonus, luck to gameplay |
| S4 | TODO | Item/armor held modifiers (AuraSkills stat modifiers) |
| S5 | TODO | Health heart-scaling option for high HP |

---

### 6.5 Resources

**Reference:** AuraSkills mana, MMOCore mana/stamina

**Current:** mana, stamina, energy. Tick regen. Clamp on stat recalc. Persist component. Player can disable regen.

**Target:** Same three pools. Mana fuels mana abilities. Stamina fuels sprint/combat extras. Energy fuels gathering abilities if needed. Action bar shows them.

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Done | Three pools, regen, consume/restore API |
| S1 | TODO | Spend mana from abilities. Fail + message if empty |
| S2 | TODO | Out-of-combat vs in-combat regen rates |
| S3 | TODO | HUD binding (see 6.14) |
| S4 | TODO | Consumables / potions restore a named pool |

---

### 6.6 Account level

**Reference:** MMOCore player level, AuraSkills power (average of skill levels)

Atlas already has a separate account level (`LevelService`, max 50, `baseXp * level`, 2 stat points per level). Keep it as **power / account level**. Skill levels are independent (AuraSkills).

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Done | Linear curve, points, `/level` |
| S1 | TODO | Configurable equation (AuraSkills XP requirements style) |
| S2 | TODO | XP sources: combat kills, event wins, skill-level-ups (small drip) |
| S3 | TODO | Party XP share (see 6.9) |
| S4 | TODO | Power = weighted average of skill levels, shown on profile |
| S5 | TODO | Prestige / extra max level (optional, last) |

---

### 6.7 Skills

**Reference:** [AuraSkills](https://wiki.aurelium.dev/auraskills)

This is the largest system. Current Atlas "skills" (Berserker, Guardian, Sage, Athlete) are **combat loadouts**: 3 equip slots that apply stat modifiers. That is closer to MMOCore bound abilities than AuraSkills.

**Split**

| Concept | Role | Source |
|---|---|---|
| **Skills** | Farming, Mining, Fighting, … Level by doing the activity | AuraSkills |
| **Abilities** | Passives unlocked every N skill levels | AuraSkills abilities |
| **Mana abilities** | Active, mana cost, cooldown, 3 equip slots | AuraSkills mana abilities + current loadouts |
| **Sources** | What grants XP (break wheat, kill mob, fish, …) | AuraSkills sources |
| **Rewards** | On skill level-up: stats, commands, Vault money, items | AuraSkills rewards |
| **Jobs** | Opt-in skill that pays Vault money while gaining XP | AuraSkills jobs |

**Default skills (11, AuraSkills)**

Farming, Foraging, Mining, Fishing, Excavation, Archery, Defense, Fighting, Agility, Enchanting, Alchemy.

Each skill:

- Own XP and level (max configurable, default 100)
- XP requirement equation
- Primary + secondary stat rewards
- Up to 5 passives
- Optional 1 mana ability
- YAML sources and rewards

**Mana ability slots**

Keep `SkillService.maxEquipped = 3`. Move Berserker / Guardian / Sage / Athlete into `abilities.yml` as mana abilities (cost, cooldown, duration, modifiers). Unlock by reaching a skill level (e.g. Fighting 10 → Berserker). Equip in the skill menu.

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Done | 4 hardcoded loadouts, unlock-all, 3 slots, apply modifiers |
| S1 | TODO | `SkillDefinition` grows: max level, xp, sources list, ability ids. Persist `Map<skillId, xp/level>` |
| S2 | TODO | `skills.yml` + `sources.yml`. Listener grants XP for vanilla actions (break, kill, fish, brew, enchant, move) |
| S3 | TODO | Level-up hook → rewards (stats, messages, sounds) |
| S4 | TODO | Passive abilities (`abilities.yml`): chance/value per ability level |
| S5 | TODO | Mana abilities: activate (right-click / swap-hand / command), mana, cooldown, duration |
| S6 | TODO | Migrate Berserker/Guardian/Sage/Athlete to mana abilities |
| S7 | TODO | XP multipliers (permissions, items, events, party) |
| S8 | TODO | Item/block skill requirements |
| S9 | TODO | Jobs (select 1–N skills, Vault payout per XP) |
| S10 | TODO | Skill loot tables (fishing, excavation, mining extras) |
| S11 | TODO | `/skills` menu parity with AuraSkills (overview, progression, abilities) |
| S12 | TODO | Skill leaderboards |

Do not start S4 until S2 XP actually lands in-game.

---

### 6.8 Combat

**Reference:** MythicLib damage pipeline, AuraSkills Fighting/Defense/Archery

**Current pipeline** (`combat.damage`):

1. Base damage
2. Stat scale (strength or magic_power + attack_damage)
3. Crit
4. Mitigation (`defense / (defense + 100)`, magic at 50%)
5. Absorption
6. Bukkit `AtlasDamageCalculateEvent` / `AtlasPostDamageEvent`
7. Optional FancyHolograms number

Types: `PHYSICAL`, `MAGIC`, `TRUE`.

**Target**

- Same pipeline. Add stages, do not rewrite.
- Friendly fire check (party) as an early cancel stage.
- Skill XP on kill/damage for Fighting, Archery, Defense.
- Bleed / first-strike / stun as Fighting passives (from 6.7 S4).
- Projectile damage uses Archery stats.
- I-frames / combat-tag (optional, combat.yml).
- Damage holograms: crit color, heal color, magic color (AuraSkills + DecentHolograms behavior, we already use FancyHolograms).

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Done | Pipeline, listener, holograms, combat toggle, player combat-feedback setting |
| S1 | TODO | `combat.yml` (formulas, hologram style, combat tag) |
| S2 | TODO | Party friendly-fire stage |
| S3 | TODO | Grant Fighting/Archery/Defense XP from hits/kills |
| S4 | TODO | Environmental / fall / magic-ability damage types through the same pipeline |
| S5 | TODO | Combat tag: PvP flag, logout penalty later if wanted |
| S6 | TODO | Knockback / stun hooks for abilities |

---

### 6.9 Party

**Reference:** [Parties](https://modrinth.com/plugin/parties) (AlessioDP)

**Not in scope:** clans, guilds, fixed/permanent factions, party-as-nation.

**Current:** max 5, create, invite, accept, deny, leave, kick, disband, `/party chat`, leader auto-promote, leave on session close. In-memory only. KOTH treats same-party players as one holder.

**Target (Parties features we will take)**

Must have:

- Ranks: leader, moderator, member (configurable names + permissions)
- Party chat with toggle (`/p` chat mode)
- Friendly fire toggle (global default + per-party)
- Invite expiry
- List / members / info
- Join/leave broadcast
- Configurable max size
- Placeholders

Should have:

- Home + sethome
- Teleport-to-leader (`/party tp`) with cooldown
- Description, MOTD on join
- Color (chat + nametag if TAB later)
- Password / open-join
- Ask-to-join
- Member nicknames
- Offline members until timeout (not instant disband)

Won't have:

- Party EXP/level as a clan replacement
- Permanent "fixed" parties (that is a clan)
- Dynmap homes, Redis, Bungee sync (later if network)

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Done | CRUD + chat + KOTH same-party |
| S1 | TODO | `party.yml`. Max size, invite TTL, FF default |
| S2 | TODO | Ranks + permission nodes (`invite`, `kick`, `home`, `chat-color`) |
| S3 | TODO | Invite expiry, `/party list`, `/party info`, MOTD |
| S4 | TODO | Friendly fire hooked into combat pipeline |
| S5 | TODO | Chat toggle + Vault-formatted party channel (see 6.10) |
| S6 | TODO | Home / sethome / tp with cooldown + warmup |
| S7 | TODO | Open/close, password, ask-to-join |
| S8 | TODO | Share skill XP and account XP (config %) |
| S9 | TODO | Persist party if leader offline for N minutes; then disband or promote |

---

### 6.10 Chat

**Reference:** Vault Chat API, VaultChatFormatter, LPC / Strings for extra UX

Vault does **not** format chat. LuckPerms stores prefix/suffix/group. Vault Chat exposes them. Atlas formats the message.

**Current:** none. Party chat is a private broadcast with a hardcoded `&d[Party]` prefix.

**Target**

```
<prefix><name><suffix> <separator> <message>
```

- MiniMessage format in `chat.yml`
- Prefix/suffix/group from `Chat` Vault service
- PlaceholderAPI in format
- Channels: `global`, `party`, `local` (radius), `staff`
- `/chat <channel>`, `/msg`, `/r`
- Party channel uses the same formatter with a `[Party]` tag
- Mentions (`@name`) with sound, toggle in settings
- Chat cooldown, anti-repeat (light, not a full ChatControl)
- Ignore list
- Staff spy for `/msg` and party (permission)

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Missing | — |
| S1 | TODO | Vault Chat hook. AsyncChatEvent formatter. `chat.yml` format string |
| S2 | TODO | PAPI in format. MiniMessage + hex |
| S3 | TODO | Channels: global + party (replace `PartyService.chat`) |
| S4 | TODO | Local radius channel, staff channel |
| S5 | TODO | `/msg`, `/r`, social spy |
| S6 | TODO | Mentions, ignore, cooldown |
| S7 | TODO | Per-group formats via Vault group name |

If Vault Chat is missing, chat still works with a bare name format and a warning in console.

---

### 6.11 Economy

**Reference:** Vault Economy (EssentialsX / CMI as provider)

Atlas never stores money. Skills jobs, rewards, KOTH, and commands may deposit/withdraw through Vault.

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Missing | — |
| S1 | TODO | Hook Economy. `EconomyService` wrapper. No-op if missing |
| S2 | TODO | Skill level-up money reward (rewards YAML) |
| S3 | TODO | Jobs payout |
| S4 | TODO | KOTH / world-event Vault reward |
| S5 | TODO | Optional command costs (Parties-style `/party create` fee) |

---

### 6.12 World events

**Reference:** Blood-moon / world-event plugins, Atlas current design

**Current:** Hardcoded Blood Moon, Iron Veil, Arcane Surge, KOTH. Duration, stat modifiers, opt-in setting, start/stop events.

**Target:** `events.yml` definitions. Scheduler (interval, cron-like, or manual). Announce. Boss bar while active. Per-event rewards on end. World/region filter later.

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Done | 4 hardcoded events, modifiers, opt-in, start/stop bus events |
| S1 | TODO | Move definitions to `events.yml` |
| S2 | TODO | Announce titles + chat. Boss bar while running |
| S3 | TODO | Scheduler (every N minutes, only if players online) |
| S4 | TODO | End rewards (XP, Vault, items) for opted-in players |
| S5 | TODO | Extra event types: double-XP, mob-surge (spawn hook, later) |
| S6 | TODO | Per-world enable list |

---

### 6.13 King of the Hill

**Reference:** KoTH by SubSide

**Current:** One hill location, radius 8, 1s tick, contest if mixed parties, score/second, winner account XP, wired to world-event id `koth`.

**Target:** Multiple arenas. Capture time vs score-time. Min players. Broadcast interval. Reward table. Zone particles / hologram at hill. Countdown. Admin set pos1/pos2 or center+radius.

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Done | Single hill, contest, king broadcast, XP reward |
| S1 | TODO | `koth.yml`. Multiple named hills |
| S2 | TODO | Capture-time win condition (hold N seconds) in addition to score |
| S3 | TODO | Reward table: XP + Vault + items + commands |
| S4 | TODO | Hill hologram + particles (FancyHolograms) |
| S5 | TODO | Scheduled rotation + `/koth list` |
| S6 | TODO | Scoreboard/HUD during fight (king, time, contest) |

---

### 6.14 HUD

**Reference:** AuraSkills action bar + boss bar

**Current:** none.

**Target**

- Action bar: `HP  |  Mana  |  Stamina` (configurable order)
- Boss bar: skill XP gain (show on XP, fade), world event, KOTH
- Toggles in settings
- Do not fight vanilla XP bar; account/skill XP is ours

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Missing | — |
| S1 | TODO | Action bar task from resource + health stats |
| S2 | TODO | XP gain boss bar (per-skill, AuraSkills behavior) |
| S3 | TODO | Event / KOTH boss bar |
| S4 | TODO | `hud.yml` formats, update interval, disable worlds |

---

### 6.15 Scoreboard

**Reference:** FeatherBoard

**Current:** Scoreboard Library initialized. `ConfigType.SCOREBOARD` exists. No `scoreboard.yml`, no sidebar.

**Target:** One sidebar. Title + lines from YAML. PlaceholderAPI + Atlas placeholders. Optional animation frames. Per-world or per-event layouts later.

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Library only | — |
| S1 | TODO | Ship `scoreboard.yml`. Create sidebar on join, update on interval |
| S2 | TODO | Lines: level, xp, mana, party, event, koth king |
| S3 | TODO | Toggle in settings. Permission `atlas.scoreboard` |
| S4 | TODO | Extra layouts (event, koth, spawn) switched by world or flag |
| S5 | TODO | Title animation frames (FeatherBoard-lite) |

---

### 6.16 Menus

**Reference:** AuraSkills menus, DeluxeMenus

**Current:** Java menus for hub, profile, stats, resources, skills, events, party, settings, cooldowns, combat, plus admin mirrors. Not YAML.

**Target:** Keep the Java menu API. Move *layout* (slots, items, lore) to `menus/*.yml`. Click actions stay in code or become declared (`equip-skill`, `spend-point`, `open:stats`).

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Done | Working Java menus |
| S1 | TODO | Lore uses MiniMessage + PAPI |
| S2 | TODO | Skills menu shows XP/level/abilities, not just loadouts |
| S3 | TODO | Party menu: members, ranks, invite |
| S4 | TODO | YAML layouts for hub + skills (highest traffic) |
| S5 | TODO | Remaining menus to YAML |

---

### 6.17 Placeholders

**Reference:** AuraSkills placeholders, Parties placeholders

**Current:** `%atlas_stat_<id>%`, `%atlas_<resource>%`, `%atlas_<resource>_max%`, `%atlas_level%`, `%atlas_xp%`, `%atlas_name%`

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Done | Thin set |
| S1 | TODO | `skill_<id>_level`, `skill_<id>_xp`, `skill_<id>_xp_required`, `power` |
| S2 | TODO | `party_name`, `party_leader`, `party_size`, `party_rank` |
| S3 | TODO | `event_name`, `event_remaining`, `koth_king`, `koth_score` |
| S4 | TODO | `mana`, `stamina`, `energy` already; add percents and bars |
| S5 | TODO | Chat prefix/suffix passthrough if needed |

---

### 6.18 Holograms

**Reference:** FancyHolograms, AuraSkills damage indicators

**Current:** Post-damage hologram if FancyHolograms present and combat feedback on.

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Done | Damage numbers |
| S1 | TODO | Crit / magic / heal colors |
| S2 | TODO | KOTH hill marker |
| S3 | TODO | Optional XP holograms on skill gain |

---

### 6.19 Leaderboards

**Reference:** AuraSkills leaderboards

**Current:** none.

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Missing | — |
| S1 | TODO | SQL top-N by account level |
| S2 | TODO | Per-skill top-N |
| S3 | TODO | `/top` menu + hologram hook |
| S4 | TODO | Cached refresh (every 60s), PAPI `%atlas_lb_...%` |

---

### 6.20 Commands and permissions

**Reference:** AuraSkills commands, Parties commands

**Current:** `/atlas`, `/atlas admin`, `/party *`, `/skill equip|unequip`, `/level`, `/koth`, `/settings`, `/gm`

**Target additions (as systems land)**

- `/skills`, `/stats` (already aliased via `/atlas`)
- `/skill top`, `/skill info <skill>`
- `/party ff`, `/party home`, `/party tp`, `/party info`
- `/chat`, `/msg`, `/r`
- `/atlas reload`, `/atlas backup`
- `/event start|stop|list`
- `/koth start|stop|set|list`

Permission tree: `atlas.<system>.<action>`, default true for player commands, OP for admin.

---

### 6.21 Public API

**Reference:** AuraSkills API, Parties API

**Current:** Bukkit events for damage, profile ready/save, world events, stat recalc.

**Stages**

| Stage | Status | Work |
|---|---|---|
| S0 | Partial | Bukkit + internal bus events |
| S1 | TODO | Stable `dev.portero.atlas.api` package: skills, stats, party, chat, events |
| S2 | TODO | Document events for other plugins |
| S3 | TODO | Custom skill/ability registration (AuraSkills custom content) |

---

## 7. Recommended build order

Do not skip. Later systems assume earlier ones.

1. **Vault + chat S1 + MiniMessage lang** — every other feature prints text
2. **HUD S1 + scoreboard S1** — players can see mana/level
3. **Skills S1–S3** — XP from vanilla actions, level-up, rewards
4. **Combat S2–S3** — FF + Fighting XP
5. **Mana abilities S5–S6** — current loadouts become real abilities
6. **Party S1–S5** — ranks, FF, chat channel
7. **Events YAML + KOTH S1–S3**
8. **Economy + jobs + loot**
9. **Leaderboards, reload, backups, API**

---

## 8. Definition of done (per stage)

A stage is done when:

- It has YAML or code behind a module, not a one-off in `AtlasBootstrap`
- Player text is in lang files
- Placeholders exist if the value is shown on HUD/board/chat
- Admin can inspect it in an existing menu or command
- Checkstyle stays clean
- It does not require clans, guilds, or a second RPG jar

---

## 9. Out of scope until a later document

- Bungee/Velocity party sync
- MythicMobs custom mobs (AuraMobs equivalent)
- Custom items (MMOItems)
- Quests
- Land claims
- Discord chat bridge
- Resource pack / emoji chat

Those can hook Atlas through the API after Phase 6.
