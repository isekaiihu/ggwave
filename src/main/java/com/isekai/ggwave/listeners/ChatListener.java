package com.isekai.ggwave.listeners;

import com.isekai.ggwave.GGWave;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final GGWave plugin;

    public ChatListener(GGWave plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        String message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        // Check if message is just "gg" (case insensitive)
        if (!message.equalsIgnoreCase("gg")) return;

        // Only intercept during active wave
        if (!plugin.getWaveManager().isActive()) return;

        // Cancel the original message
        event.setCancelled(true);

        // Player can send multiple GG messages but only gets rewarded once
        boolean alreadyParticipated = plugin.getWaveManager().hasParticipated(event.getPlayer().getUniqueId());

        // Handle on main thread
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getWaveManager().handleGG(event.getPlayer(), alreadyParticipated);
        });
    }
}
