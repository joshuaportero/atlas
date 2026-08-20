package dev.portero.atlas.menu;

import dev.portero.atlas.combat.CombatManager;
import dev.portero.atlas.config.ConfigManager;
import dev.portero.atlas.database.DatabaseManager;
import dev.portero.atlas.level.LevelService;
import dev.portero.atlas.menu.admin.AdminCombatMenu;
import dev.portero.atlas.menu.admin.AdminEventMenu;
import dev.portero.atlas.menu.admin.AdminHubMenu;
import dev.portero.atlas.menu.admin.AdminPlayerListMenu;
import dev.portero.atlas.menu.admin.AdminPlayerMenu;
import dev.portero.atlas.menu.admin.AdminResourceMenu;
import dev.portero.atlas.menu.admin.AdminSettingsMenu;
import dev.portero.atlas.menu.admin.AdminSkillMenu;
import dev.portero.atlas.menu.admin.AdminStatMenu;
import dev.portero.atlas.menu.api.AtlasMenu;
import dev.portero.atlas.menu.player.CombatSettingsMenu;
import dev.portero.atlas.menu.player.CooldownMenu;
import dev.portero.atlas.menu.player.EventMenu;
import dev.portero.atlas.menu.player.PartyMenu;
import dev.portero.atlas.menu.player.PlayerHubMenu;
import dev.portero.atlas.menu.player.PlayerSettingsMenu;
import dev.portero.atlas.menu.player.ProfileMenu;
import dev.portero.atlas.menu.player.QuestMenu;
import dev.portero.atlas.menu.player.ResourceMenu;
import dev.portero.atlas.menu.player.SkillMenu;
import dev.portero.atlas.menu.player.StatMenu;
import dev.portero.atlas.party.PartyService;
import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.player.ProfileManager;
import dev.portero.atlas.player.SettingsComponent;
import dev.portero.atlas.quest.QuestService;
import dev.portero.atlas.resource.ResourceManager;
import dev.portero.atlas.skill.SkillService;
import dev.portero.atlas.stat.StatManager;
import dev.portero.atlas.worldevent.WorldEventService;
import org.bukkit.entity.Player;

public final class MenuFactory {

    private final ProfileManager profiles;
    private final StatManager stats;
    private final ResourceManager resources;
    private final CombatManager combat;
    private final SkillService skills;
    private final WorldEventService events;
    private final ConfigManager configs;
    private final DatabaseManager database;
    private final PartyService parties;
    private final QuestService quests;
    private final LevelService levels;

    public MenuFactory(ProfileManager profiles, StatManager stats, ResourceManager resources,
                       CombatManager combat, SkillService skills, WorldEventService events,
                       ConfigManager configs, DatabaseManager database, PartyService parties,
                       QuestService quests, LevelService levels) {
        this.profiles = profiles;
        this.stats = stats;
        this.resources = resources;
        this.combat = combat;
        this.skills = skills;
        this.events = events;
        this.configs = configs;
        this.database = database;
        this.parties = parties;
        this.quests = quests;
        this.levels = levels;
    }

    public ProfileManager profiles() {
        return this.profiles;
    }

    public StatManager stats() {
        return this.stats;
    }

    public ResourceManager resources() {
        return this.resources;
    }

    public CombatManager combat() {
        return this.combat;
    }

    public SkillService skills() {
        return this.skills;
    }

    public WorldEventService events() {
        return this.events;
    }

    public ConfigManager configs() {
        return this.configs;
    }

    public DatabaseManager database() {
        return this.database;
    }

    public PartyService parties() {
        return this.parties;
    }

    public QuestService quests() {
        return this.quests;
    }

    public LevelService levels() {
        return this.levels;
    }

    public AtlasPlayer atlas(Player player) {
        return this.profiles.require(player);
    }

    public SettingsComponent settings(AtlasPlayer player) {
        SettingsComponent settings = player.profile().component(SettingsComponent.class)
                .orElseGet(SettingsComponent::new);
        player.profile().attach(settings, false);
        return settings;
    }

    public AtlasMenu playerHub() {
        return new PlayerHubMenu(this);
    }

    public AtlasMenu profileMenu() {
        return new ProfileMenu(this);
    }

    public AtlasMenu statMenu() {
        return new StatMenu(this);
    }

    public AtlasMenu resourceMenu() {
        return new ResourceMenu(this);
    }

    public AtlasMenu skillMenu() {
        return new SkillMenu(this);
    }

    public AtlasMenu questMenu() {
        return new QuestMenu(this);
    }

    public AtlasMenu eventMenu() {
        return new EventMenu(this);
    }

    public AtlasMenu combatSettingsMenu() {
        return new CombatSettingsMenu(this);
    }

    public AtlasMenu cooldownMenu() {
        return new CooldownMenu(this);
    }

    public AtlasMenu settingsMenu() {
        return new PlayerSettingsMenu(this);
    }

    public AtlasMenu partyMenu() {
        return new PartyMenu(this);
    }

    public AtlasMenu adminHub() {
        return new AdminHubMenu(this);
    }

    public AtlasMenu adminPlayers() {
        return new AdminPlayerListMenu(this);
    }

    public AtlasMenu adminPlayer(Player target) {
        return new AdminPlayerMenu(this, target);
    }

    public AtlasMenu adminStats(Player target) {
        return new AdminStatMenu(this, target);
    }

    public AtlasMenu adminResources(Player target) {
        return new AdminResourceMenu(this, target);
    }

    public AtlasMenu adminSkills(Player target) {
        return new AdminSkillMenu(this, target);
    }

    public AtlasMenu adminSettings() {
        return new AdminSettingsMenu(this);
    }

    public AtlasMenu adminCombat() {
        return new AdminCombatMenu(this);
    }

    public AtlasMenu adminEvents() {
        return new AdminEventMenu(this);
    }
}
