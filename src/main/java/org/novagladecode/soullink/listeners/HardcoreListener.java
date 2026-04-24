package org.novagladecode.soullink.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.novagladecode.soullink.SoulLinkPlugin;

/**
 * Hardcore mode implementation.
 *
 * When hardcore is enabled:
 *   - If a player dies, their LINKED PARTNER also dies simultaneously.
 *   - Both players are then set to SPECTATOR mode permanently (no respawn).
 *   - The game is "over" — a dramatic game-over title is broadcast.
 *   - Operators can use /soullink admin revive <player> to undo this.
 */
public class HardcoreListener implements Listener {

    private final SoulLinkPlugin plugin;
    private final java.util.Set<java.util.UUID> killGuard = new java.util.HashSet<>();

    public HardcoreListener(SoulLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        if (!plugin.getConfig().getBoolean("hardcore", true)) return;

        Player dead = event.getPlayer();

        // Announce the death to everyone
        String deathMsg = "§4☠ " + dead.getName() + " has died. Game over.";
        Bukkit.broadcast(Component.text(deathMsg));

        // Kill the partner too (if linked and alive)
        Player partner = plugin.getLinkManager().getPartner(dead);
        if (partner != null && partner.isOnline() && !killGuard.contains(partner.getUniqueId())) {
            killGuard.add(partner.getUniqueId());
            try {
                partner.setHealth(0);
                partner.sendMessage(plugin.prefix("&4Your soul-link is broken... you died with " + dead.getName() + "."));
            } finally {
                // Guard removed in PlayerDeathEvent for partner (recursive call ends here)
            }
        }

        // 1 tick later: set both to spectator
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            setSpectator(dead);
            if (partner != null && partner.isOnline()) setSpectator(partner);

            // Broadcast dramatic title
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle(
                        "§4§lGAME OVER",
                        "§c" + dead.getName() + " and their soul perished.",
                        10, 100, 20
                );
            }
        }, 1L);

        killGuard.remove(dead.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        if (!plugin.getConfig().getBoolean("hardcore", true)) return;

        // Force dead players back into spectator after respawn screen
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> setSpectator(player), 1L);
    }

    private void setSpectator(Player player) {
        player.setGameMode(GameMode.SPECTATOR);
        player.sendMessage(plugin.prefix("&4You are in §lHardcore Spectator§r§4 mode. Ask an admin to revive you."));
    }
}
