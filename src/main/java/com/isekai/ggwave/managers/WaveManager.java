package com.isekai.ggwave.managers;

import com.isekai.ggwave.GGWave;
import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.isekai.ggwave.utils.HeadRenderer;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WaveManager {

    private final GGWave plugin;
    private boolean active = false;
    private Player target;
    private String purchaseName;
    private final Set<UUID> participants = ConcurrentHashMap.newKeySet();
    private int colorIndex = 0;
    private BukkitTask waveTask;
    private BukkitTask fireworkTask;
    private long startTime;
    private int totalGGs = 0;

    public WaveManager(GGWave plugin) {
        this.plugin = plugin;
    }

    public boolean isActive() { return active; }
    public Player getTarget() { return target; }
    public String getPurchaseName() { return purchaseName; }
    public int getTotalGGs() { return totalGGs; }
    public int getParticipantCount() { return participants.size(); }

    public boolean startWave(Player initiatedTarget, String purchase) {
        if (active) return false;

        this.active = true;
        this.target = initiatedTarget;
        this.purchaseName = purchase;
        this.participants.clear();
        this.colorIndex = 0;
        this.totalGGs = 0;
        this.startTime = System.currentTimeMillis();

        // Send announcement with player head
        sendAnnouncement();

        // Play start sound
        playSound("sounds.wave-start");

        // Show title to target
        showTitle();

        // Start fireworks
        startFireworks();

        // Schedule wave end
        int duration = plugin.getConfig().getInt("wave-duration", 30);
        waveTask = new BukkitRunnable() {
            @Override
            public void run() {
                endWave();
            }
        }.runTaskLater(plugin, duration * 20L);

        return true;
    }

    private void sendAnnouncement() {
        List<String> lines = plugin.getMessages().getStringList("announcement");
        String store = plugin.getConfig().getString("store-url", "STORE.EXAMPLE.COM");

        // Parse announcement text lines
        List<Component> textLines = new ArrayList<>();
        for (String line : lines) {
            String parsed = line
                    .replace("{player}", target.getName())
                    .replace("{purchase}", purchaseName)
                    .replace("{store}", store);
            textLines.add(plugin.color(parsed));
        }

        // Render the 8x8 player head as colored block characters
        List<Component> headRows = HeadRenderer.renderHead(target, plugin.getLogger());

        // Combine head rows with text lines side by side
        // Head is 8 rows, text fills beside them then continues below
        List<Component> combined = new ArrayList<>();
        int maxRows = Math.max(headRows.size(), textLines.size());
        for (int i = 0; i < maxRows; i++) {
            Component row = Component.empty();
            if (i < headRows.size()) {
                row = row.append(headRows.get(i)).append(Component.text(" "));
            } else {
                // Pad with spaces to align text below head
                row = row.append(Component.text("                  "));
            }
            if (i < textLines.size()) {
                row = row.append(textLines.get(i));
            }
            combined.add(row);
        }

        // Send to all players
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (Component line : combined) {
                p.sendMessage(line);
            }
        }
    }

    private void showTitle() {
        if (!plugin.getConfig().getBoolean("title.enabled", true)) return;

        int duration = plugin.getConfig().getInt("title.duration", 10);
        String titleText = plugin.getConfig().getString("title.title", "<bold>GG WAVE!</bold>");
        String subtitleText = plugin.getConfig().getString("title.subtitle", "<gray>Thank you!</gray>");

        Title title = Title.title(
                plugin.color(titleText),
                plugin.color(subtitleText),
                Title.Times.times(
                        Duration.ofMillis(500),
                        Duration.ofSeconds(duration),
                        Duration.ofMillis(1000)
                )
        );
        target.showTitle(title);
    }

    private void startFireworks() {
        if (!plugin.getConfig().getBoolean("fireworks.enabled", true)) return;

        int duration = plugin.getConfig().getInt("fireworks.duration", 10) * 20;
        int interval = plugin.getConfig().getInt("fireworks.interval", 5);
        Random random = new Random();

        fireworkTask = new BukkitRunnable() {
            int ticks = 0;
            double angle = 0;

            @Override
            public void run() {
                if (ticks >= duration || !active || !target.isOnline()) {
                    cancel();
                    return;
                }

                Location loc = target.getLocation().add(0, 1, 0);
                World world = loc.getWorld();

                // Circular ring of colored particles around the player
                for (int i = 0; i < 36; i++) {
                    double theta = angle + (i * Math.PI * 2 / 36);
                    double radius = 2.5;
                    double x = loc.getX() + Math.cos(theta) * radius;
                    double y = loc.getY() + Math.sin(theta * 0.5) * 0.8;
                    double z = loc.getZ() + Math.sin(theta) * radius;

                    // Cycle through rainbow colors
                    float hue = (float)(i / 36.0);
                    java.awt.Color awtColor = java.awt.Color.getHSBColor(hue, 1.0f, 1.0f);
                    Color color = Color.fromRGB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
                    world.spawnParticle(Particle.DUST, x, y, z, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(color, 1.5f));
                }

                // Spiraling upward helix
                for (int i = 0; i < 12; i++) {
                    double theta = angle * 2 + (i * Math.PI * 2 / 12);
                    double r = 1.5;
                    double yOff = (ticks % 40) / 40.0 * 3.0 + (i * 0.25);
                    double x = loc.getX() + Math.cos(theta) * r;
                    double z = loc.getZ() + Math.sin(theta) * r;

                    Color color = Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                    world.spawnParticle(Particle.DUST, x, loc.getY() + yOff, z, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(color, 2.0f));
                }

                // Burst every second
                if (ticks % 20 == 0) {
                    for (int i = 0; i < 60; i++) {
                        double theta = random.nextDouble() * Math.PI * 2;
                        double phi = random.nextDouble() * Math.PI;
                        double r = 1.5 + random.nextDouble() * 2.5;
                        double x = loc.getX() + Math.sin(phi) * Math.cos(theta) * r;
                        double y = loc.getY() + 1 + Math.cos(phi) * r;
                        double z = loc.getZ() + Math.sin(phi) * Math.sin(theta) * r;

                        Color color = Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                        world.spawnParticle(Particle.DUST, x, y, z, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(color, 2.5f));
                    }
                }

                angle += 0.15;
                ticks += interval;
            }
        }.runTaskTimer(plugin, 0, interval);
    }

    /**
     * Handle a GG message from a player.
     * @param player the player who said GG
     * @param alreadyParticipated true if they already said GG before (still show message, no reward)
     */
    public void handleGG(Player player, boolean alreadyParticipated) {
        if (!active) return;

        totalGGs++;

        // Get color from color loop (always cycle for every GG message)
        List<String> colors = plugin.getConfig().getStringList("color-loop");
        String color;
        if (colors.isEmpty()) {
            color = "&#FFFFFF";
        } else {
            color = colors.get(colorIndex % colors.size());
            colorIndex++;
            // Loop back when we reach the end
            if (colorIndex >= colors.size()) {
                colorIndex = 0;
            }
        }

        // Build the formatted GG message
        String format = plugin.getConfig().getString("player-message.format",
                "{prefix}{player} <dark_gray>→</dark_gray> {colored_gg}");
        boolean obfuscated = plugin.getConfig().getBoolean("player-message.obfuscated", false);

        String prefix = getPlayerPrefix(player);

        String coloredGG;
        if (obfuscated) {
            String hexConverted = color.replace("&#", "<color:#") + ">";
            coloredGG = "<obfuscated>" + hexConverted + "GG</obfuscated>";
        } else {
            String hexConverted = color.replace("&#", "<color:#") + ">";
            coloredGG = hexConverted + "GG";
        }

        String message = format
                .replace("{prefix}", prefix)
                .replace("{player}", player.getName())
                .replace("{colored_gg}", coloredGG);

        Component comp = plugin.color(message);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(comp);
        }

        // Play GG sound
        playSound("sounds.player-message");

        // Only reward if first time participating
        if (!alreadyParticipated) {
            participants.add(player.getUniqueId());

            // Check player limit
            int limit = plugin.getConfig().getInt("rewards.player-limit", 0);
            if (limit <= 0 || participants.size() <= limit) {
                giveRewards(player);
            }
        }
    }

    private String getPlayerPrefix(Player player) {
        // Try LuckPerms via Bukkit scoreboard meta
        try {
            var lp = Bukkit.getServicesManager().getRegistration(
                    Class.forName("net.luckperms.api.LuckPerms"));
            if (lp != null) {
                var api = lp.getProvider();
                var method = api.getClass().getMethod("getPlayerAdapter", Class.class);
                var adapter = method.invoke(api, Player.class);
                var metaMethod = adapter.getClass().getMethod("getMetaData", Object.class);
                var meta = metaMethod.invoke(adapter, player);
                var prefixMethod = meta.getClass().getMethod("getPrefix");
                String prefix = (String) prefixMethod.invoke(meta);
                if (prefix != null && !prefix.isEmpty()) {
                    return prefix + " ";
                }
            }
        } catch (Exception ignored) {}

        return "";
    }

    private void giveRewards(Player player) {
        if (!plugin.getConfig().getBoolean("rewards.enabled", true)) return;

        List<String> commands = plugin.getConfig().getStringList("rewards.commands");
        for (String cmd : commands) {
            String parsed = cmd
                    .replace("{player}", player.getName())
                    .replace("{target}", target != null ? target.getName() : "");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }
    }

    public void endWave() {
        if (!active) return;
        active = false;

        // Play end sound
        playSound("sounds.wave-end");

        // Send end message
        String endMsg = plugin.getPrefix() + plugin.getMsg("wave-ended");
        Component comp = plugin.color(endMsg);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(comp);
        }

        // Cancel tasks
        if (waveTask != null) { waveTask.cancel(); waveTask = null; }
        if (fireworkTask != null) { fireworkTask.cancel(); fireworkTask = null; }

        target = null;
        purchaseName = null;
    }

    public void forceEnd() {
        if (active) endWave();
    }

    private void playSound(String configPath) {
        if (!plugin.getConfig().getBoolean(configPath + ".enabled", true)) return;

        String soundName = plugin.getConfig().getString(configPath + ".sound", "BLOCK_NOTE_BLOCK_PLING");
        float volume = (float) plugin.getConfig().getDouble(configPath + ".volume", 1.0);
        float pitch = (float) plugin.getConfig().getDouble(configPath + ".pitch", 1.0);

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), sound, volume, pitch);
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " + soundName);
        }
    }

    public boolean hasParticipated(UUID uuid) {
        return participants.contains(uuid);
    }
}
