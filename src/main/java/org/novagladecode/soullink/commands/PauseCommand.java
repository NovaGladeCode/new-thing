package org.novagladecode.soullink.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.novagladecode.soullink.SoulLinkPlugin;

/**
 * /slpause — toggle world pause on/off.
 * Requires soullink.pause permission (default: op).
 */
public class PauseCommand implements CommandExecutor {

    private final SoulLinkPlugin plugin;

    public PauseCommand(SoulLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("soullink.pause")) {
            sender.sendMessage(plugin.prefix("&cYou do not have permission to pause the world."));
            return true;
        }

        plugin.getPauseManager().toggle();

        boolean nowPaused = plugin.getPauseManager().isPaused();
        sender.sendMessage(plugin.prefix(
                nowPaused ? "&eWorld &c⏸ PAUSED&e by " + sender.getName() + "."
                          : "&eWorld &a▶ RESUMED&e by " + sender.getName() + "."
        ));
        return true;
    }
}
