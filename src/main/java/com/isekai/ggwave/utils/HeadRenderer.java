package com.isekai.ggwave.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerTextures;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class HeadRenderer {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    /**
     * Renders the 8x8 face layer of a player's skin as colored block characters.
     * Returns a list of Components, one per row (8 rows).
     */
    public static List<Component> renderHead(Player player, Logger logger) {
        List<Component> rows = new ArrayList<>();
        try {
            PlayerProfile profile = player.getPlayerProfile();
            profile.complete(true);

            URL skinUrl = profile.getTextures().getSkin();
            if (skinUrl == null) {
                // Fallback to Steve skin
                skinUrl = URI.create("https://textures.minecraft.net/texture/31f477eb1a7beee631c2ca64d06f8f68fa93a3386d04452ab27f43acdf1b60cb").toURL();
            }

            BufferedImage skin = ImageIO.read(skinUrl);

            // The face is at pixels (8,8) to (15,15) on the skin texture
            for (int y = 8; y < 16; y++) {
                StringBuilder row = new StringBuilder();
                for (int x = 8; x < 16; x++) {
                    int rgb = skin.getRGB(x, y);
                    int alpha = (rgb >>> 24) & 0xFF;
                    if (alpha < 20) {
                        // Transparent pixel - use space
                        row.append(" ");
                    } else {
                        String hex = String.format("%06X", rgb & 0xFFFFFF);
                        row.append("<#").append(hex).append(">█");
                    }
                }
                rows.add(MM.deserialize(row.toString()));
            }
        } catch (Exception e) {
            logger.warning("Failed to render player head for " + player.getName() + ": " + e.getMessage());
            // Return empty rows as fallback
            for (int i = 0; i < 8; i++) {
                rows.add(Component.empty());
            }
        }
        return rows;
    }
}
