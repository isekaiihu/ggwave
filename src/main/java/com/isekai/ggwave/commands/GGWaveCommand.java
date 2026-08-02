package com.isekai.ggwave.commands;

import com.isekai.ggwave.GGWave;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GGWaveCommand implements CommandExecutor {

    private final GGWave plugin;

    public GGWaveCommand(GGWave plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("ggwave.admin")) {
            sender.sendMessage(plugin.color(plugin.getPrefix() + plugin.getMsg("no-permission")));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.color(plugin.getPrefix() + plugin.getMsg("usage")));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> handleStart(sender, args);
            case "stop" -> handleStop(sender);
            case "reload" -> {
                plugin.reload();
                sender.sendMessage(plugin.color(plugin.getPrefix() + plugin.getMsg("reload")));
            }
            default -> sender.sendMessage(plugin.color(plugin.getPrefix() + plugin.getMsg("usage")));
        }

        return true;
    }

    private void handleStart(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.color(plugin.getPrefix() + plugin.getMsg("usage")));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.color(plugin.getPrefix() + plugin.getMsg("no-player")));
            return;
        }

        // Join remaining args as purchase name
        StringBuilder purchase = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) purchase.append(" ");
            purchase.append(args[i]);
        }

        boolean started = plugin.getWaveManager().startWave(target, purchase.toString());
        if (started) {
            sender.sendMessage(plugin.color(plugin.getPrefix() +
                    plugin.getMsg("wave-started").replace("{player}", target.getName())));
        } else {
            sender.sendMessage(plugin.color(plugin.getPrefix() + plugin.getMsg("wave-already-active")));
        }
    }

    private void handleStop(CommandSender sender) {
        if (plugin.getWaveManager().isActive()) {
            plugin.getWaveManager().forceEnd();
            sender.sendMessage(plugin.color(plugin.getPrefix() + "<green>GG Wave force stopped!"));
        } else {
            sender.sendMessage(plugin.color(plugin.getPrefix() + "<red>No active GG wave!"));
        }
    }
}
