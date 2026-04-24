package org.novagladecode.soullink.commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.novagladecode.soullink.SoulLinkPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /soullink <subcommand>
 *
 * Subcommands:
 *   link <player>          — link yourself to another player
 *   unlink                 — break your current link
 *   status                 — show link status, day, pause state
 *   admin revive <player>  — revive a hardcore-dead player back to survival
 *   admin hardcore <on|off>— toggle hardcore mode at runtime
 *   admin reload           — reload config
 */
public class SoulLinkCommand implements CommandExecutor, TabCompleter {

    private final SoulLinkPlugin plugin;

    public SoulLinkCommand(SoulLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            // ── /soullink link <player> ──────────────────────────────────────
            case "link" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.prefix("&cOnly players can link."));
                    return true;
                }
                if (!player.hasPermission("soullink.link")) {
                    player.sendMessage(plugin.prefix("&cNo permission."));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(plugin.prefix("&cUsage: /soullink link <player>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(plugin.prefix("&c" + args[1] + " is not online."));
                    return true;
                }
                if (target.equals(player)) {
                    player.sendMessage(plugin.prefix("&cYou cannot link to yourself."));
                    return true;
                }
                plugin.getLinkManager().link(player, target);
                player.sendMessage(plugin.prefix("&aYou are now soul-linked with &e" + target.getName() + "&a!"));
                target.sendMessage(plugin.prefix("&a" + player.getName() + " has soul-linked with you!"));
                // Force immediate inventory sync
                target.getInventory().setContents(player.getInventory().getContents());
                target.getInventory().setArmorContents(player.getInventory().getArmorContents());
                target.getInventory().setItemInOffHand(player.getInventory().getItemInOffHand());
                target.getInventory().setHeldItemSlot(player.getInventory().getHeldItemSlot());
                target.updateInventory();

                // Force immediate health & food sync
                target.setHealth(player.getHealth());
                target.setFoodLevel(player.getFoodLevel());
                target.setSaturation(player.getSaturation());
                target.setExhaustion(player.getExhaustion());
            }

            // ── /soullink unlink ─────────────────────────────────────────────
            case "unlink" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.prefix("&cOnly players can unlink."));
                    return true;
                }
                Player partner = plugin.getLinkManager().getPartner(player);
                plugin.getLinkManager().unlink(player);
                player.sendMessage(plugin.prefix("&eYou have unlinked your soul."));
                if (partner != null && partner.isOnline()) {
                    partner.sendMessage(plugin.prefix("&e" + player.getName() + " has severed the soul link."));
                }
            }

            // ── /soullink status ─────────────────────────────────────────────
            case "status" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(plugin.prefix("&cOnly players can check status."));
                    return true;
                }
                Player partner = plugin.getLinkManager().getPartner(player);
                String partnerName = (partner != null) ? "&a" + partner.getName() : "&cNone";
                player.sendMessage(plugin.prefix("&7Soul Partner: " + partnerName));
                player.sendMessage(plugin.prefix("&7Day: &6" + plugin.getDayManager().getDay()));
                player.sendMessage(plugin.prefix("&7Paused: " + (plugin.getPauseManager().isPaused() ? "&cYes" : "&aNo")));
                player.sendMessage(plugin.prefix("&7Hardcore: " + (plugin.getConfig().getBoolean("hardcore") ? "&cOn" : "&aOff")));
            }

            // ── /soullink admin <sub> ────────────────────────────────────────
            case "admin" -> {
                if (!sender.hasPermission("soullink.admin")) {
                    sender.sendMessage(plugin.prefix("&cNo permission."));
                    return true;
                }
                if (args.length < 2) { sendAdminHelp(sender); return true; }

                switch (args[1].toLowerCase()) {
                    case "revive" -> {
                        if (args.length < 3) {
                            sender.sendMessage(plugin.prefix("&cUsage: /soullink admin revive <player>"));
                            return true;
                        }
                        Player target = Bukkit.getPlayer(args[2]);
                        if (target == null) {
                            sender.sendMessage(plugin.prefix("&c" + args[2] + " is not online."));
                            return true;
                        }
                        target.setGameMode(GameMode.SURVIVAL);
                        target.setHealth(target.getMaxHealth());
                        target.setFoodLevel(20);
                        sender.sendMessage(plugin.prefix("&aRevived &e" + target.getName() + "&a."));
                        target.sendMessage(plugin.prefix("&aYou have been revived by an admin!"));
                    }
                    case "hardcore" -> {
                        if (args.length < 3) {
                            sender.sendMessage(plugin.prefix("&cUsage: /soullink admin hardcore <on|off>"));
                            return true;
                        }
                        boolean val = args[2].equalsIgnoreCase("on");
                        plugin.getConfig().set("hardcore", val);
                        plugin.saveConfig();
                        sender.sendMessage(plugin.prefix("&aHardcore mode is now " + (val ? "&con" : "&aoff") + "&a."));
                    }
                    case "reload" -> {
                        plugin.reloadConfig();
                        sender.sendMessage(plugin.prefix("&aConfig reloaded."));
                    }
                    default -> sendAdminHelp(sender);
                }
            }

            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage(plugin.prefix("&7Commands:"));
        s.sendMessage("  &d/soullink link <player> &7— link souls");
        s.sendMessage("  &d/soullink unlink &7— break link");
        s.sendMessage("  &d/soullink status &7— view status");
        s.sendMessage("  &d/slpause &7— pause/unpause the world");
        s.sendMessage("  &d/slday &7— show day counter");
    }

    private void sendAdminHelp(CommandSender s) {
        s.sendMessage(plugin.prefix("&7Admin Commands:"));
        s.sendMessage("  &d/soullink admin revive <player>");
        s.sendMessage("  &d/soullink admin hardcore <on|off>");
        s.sendMessage("  &d/soullink admin reload");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("link", "unlink", "status", "admin")
                    .stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("link")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            return Arrays.asList("revive", "hardcore", "reload")
                    .stream().filter(s -> s.startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("admin")) {
            if (args[1].equalsIgnoreCase("revive")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName).collect(Collectors.toList());
            }
            if (args[1].equalsIgnoreCase("hardcore")) {
                return Arrays.asList("on", "off");
            }
        }
        return List.of();
    }
}
