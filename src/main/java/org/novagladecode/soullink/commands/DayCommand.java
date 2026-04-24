package org.novagladecode.soullink.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.novagladecode.soullink.SoulLinkPlugin;

/**
 * /slday  — shows the current day counter.
 * /slreset — resets the counter to 1 (requires soullink.admin).
 */
public class DayCommand implements CommandExecutor {

    private final SoulLinkPlugin plugin;

    public DayCommand(SoulLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("slreset")) {
            if (!sender.hasPermission("soullink.admin")) {
                sender.sendMessage(plugin.prefix("&cNo permission."));
                return true;
            }
            plugin.getDayManager().reset();
            sender.sendMessage(plugin.prefix("&aDay counter reset to &6Day 1&a."));
            return true;
        }

        // /slday
        int day = plugin.getDayManager().getDay();
        sender.sendMessage(plugin.prefix("&7It is currently &6Day " + day + "&7 of your SMP."));
        return true;
    }
}
