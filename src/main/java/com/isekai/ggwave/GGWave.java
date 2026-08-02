package com.isekai.ggwave;

import com.isekai.ggwave.commands.GGWaveCommand;
import com.isekai.ggwave.commands.GGWaveTabCompleter;
import com.isekai.ggwave.listeners.ChatListener;
import com.isekai.ggwave.managers.WaveManager;
import com.isekai.ggwave.placeholders.GGWavePlaceholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class GGWave extends JavaPlugin {

    private static GGWave instance;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private WaveManager waveManager;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        instance = this;
        printAscii();
        saveDefaultConfig();
        saveResource("messages.yml", false);
        loadMessages();

        waveManager = new WaveManager(this);

        var cmd = getCommand("ggwave");
        if (cmd != null) {
            cmd.setExecutor(new GGWaveCommand(this));
            cmd.setTabCompleter(new GGWaveTabCompleter());
        }

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        // PlaceholderAPI hook
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new GGWavePlaceholders(this).register();
            getLogger().info("PlaceholderAPI hooked!");
        }

        getLogger().info("GGWave has been enabled!");
        getLogger().info("Author: ISekai | Discord: akumasekai");
    }

    @Override
    public void onDisable() {
        if (waveManager != null) {
            waveManager.forceEnd();
        }
        getLogger().info("GGWave has been disabled!");
    }

    private void printAscii() {
        String[] ascii = {
            "",
            "§6╔═══════════════════════════════════════════════════════════════╗",
            "§6║                                                               ║",
            "§6║   §e ██  ███████ ███████ ██   ██  █████  ██                    §6║",
            "§6║   §e ██  ██      ██      ██  ██  ██   ██ ██                    §6║",
            "§6║   §e ██  ███████ █████   █████   ███████ ██                    §6║",
            "§6║   §e ██       ██ ██      ██  ██  ██   ██ ██                    §6║",
            "§6║   §e ██  ███████ ███████ ██   ██ ██   ██ ██                    §6║",
            "§6║                                                               ║",
            "§6║            §fGGWave v" + getDescription().getVersion() + " §7| §fAuthor: ISekai              §6║",
            "§6║            §fDiscord: akumasekai                               §6║",
            "§6╚═══════════════════════════════════════════════════════════════╝",
            ""
        };
        for (String line : ascii) {
            getServer().getConsoleSender().sendMessage(line);
        }
    }

    public void loadMessages() {
        File f = new File(getDataFolder(), "messages.yml");
        if (!f.exists()) saveResource("messages.yml", false);
        messagesConfig = YamlConfiguration.loadConfiguration(f);
    }

    public void reload() {
        reloadConfig();
        loadMessages();
        getLogger().info("Configuration & messages reloaded!");
    }

    public static GGWave getInstance() { return instance; }
    public WaveManager getWaveManager() { return waveManager; }
    public FileConfiguration getMessages() { return messagesConfig; }

    public Component color(String message) {
        // Support &#RRGGBB format by converting to MiniMessage <color:#RRGGBB>
        String converted = convertHexColors(message);
        return miniMessage.deserialize(converted);
    }

    public Component colorLegacy(String message) {
        // Support legacy & color codes and &#RRGGBB
        String converted = message.replace("§", "&");
        return legacySerializer.deserialize(converted);
    }

    private String convertHexColors(String input) {
        // Convert &#RRGGBB to <color:#RRGGBB>
        return input.replaceAll("&#([0-9a-fA-F]{6})", "<color:#$1>");
    }

    public String getMsg(String key) {
        return messagesConfig.getString(key, "<red>Missing message: " + key);
    }

    public String getPrefix() {
        return messagesConfig.getString("prefix", "<gradient:#FFD700:#FFA500>GGWave</gradient> <dark_gray>»</dark_gray> ");
    }
}
