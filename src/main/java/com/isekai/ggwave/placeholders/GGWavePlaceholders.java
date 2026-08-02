package com.isekai.ggwave.placeholders;

import com.isekai.ggwave.GGWave;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GGWavePlaceholders extends PlaceholderExpansion {

    private final GGWave plugin;

    public GGWavePlaceholders(GGWave plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() { return "ggwave"; }

    @Override
    public @NotNull String getAuthor() { return "ISekai"; }

    @Override
    public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }

    @Override
    public boolean persist() { return true; }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        var wm = plugin.getWaveManager();

        return switch (params.toLowerCase()) {
            case "active" -> String.valueOf(wm.isActive());
            case "target" -> wm.isActive() && wm.getTarget() != null ? wm.getTarget().getName() : "None";
            case "purchase" -> wm.isActive() && wm.getPurchaseName() != null ? wm.getPurchaseName() : "None";
            case "participants" -> String.valueOf(wm.getParticipantCount());
            case "total_ggs" -> String.valueOf(wm.getTotalGGs());
            case "participated" -> player != null ? String.valueOf(wm.hasParticipated(player.getUniqueId())) : "false";
            default -> null;
        };
    }
}
