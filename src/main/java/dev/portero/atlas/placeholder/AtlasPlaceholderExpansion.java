package dev.portero.atlas.placeholder;

import dev.portero.atlas.player.AtlasPlayer;
import dev.portero.atlas.player.ProfileManager;
import dev.portero.atlas.resource.ResourceManager;
import dev.portero.atlas.resource.ResourceType;
import dev.portero.atlas.stat.StatManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class AtlasPlaceholderExpansion extends PlaceholderExpansion {

    private final Plugin plugin;
    private final ProfileManager profiles;
    private final StatManager stats;
    private final ResourceManager resources;

    public AtlasPlaceholderExpansion(Plugin plugin, ProfileManager profiles,
                                     StatManager stats, ResourceManager resources) {
        this.plugin = plugin;
        this.profiles = profiles;
        this.stats = stats;
        this.resources = resources;
    }

    @Override
    public @NotNull String getIdentifier() {
        return this.plugin.getPluginMeta().getName().toLowerCase(Locale.ROOT);
    }

    @Override
    public @NotNull String getAuthor() {
        return this.plugin.getPluginMeta().getAuthors().stream().findFirst().orElse("Unknown");
    }

    @Override
    public @NotNull String getVersion() {
        return this.plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.isOnline() || player.getPlayer() == null) {
            return "";
        }

        return this.profiles.find(player.getPlayer())
                .map(atlasPlayer -> this.resolve(atlasPlayer, params))
                .orElse("");
    }

    private String resolve(AtlasPlayer player, String params) {
        if (params.startsWith("stat_")) {
            String statId = params.substring("stat_".length());
            return this.format(this.stats.value(player, statId));
        }

        if (params.endsWith("_max")) {
            String typeId = params.substring(0, params.length() - 4);
            return this.resources.registry().find(typeId)
                    .map(type -> this.format(this.resources.maximum(player, type)))
                    .orElse("");
        }

        ResourceType resource = this.resources.registry().find(params).orElse(null);
        if (resource != null) {
            return this.format(this.resources.current(player, resource));
        }

        if ("name".equals(params)) {
            return player.name();
        }
        return "";
    }

    private String format(double value) {
        if (value == (long) value) {
            return Long.toString((long) value);
        }
        return String.format("%.1f", value);
    }
}
