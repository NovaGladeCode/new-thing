package org.novagladecode.soullink.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.novagladecode.soullink.SoulLinkPlugin;

/**
 * Synchronises hunger (food level + saturation) between linked players.
 * When one player's food level changes, the partner gets the same level.
 */
public class HungerListener implements Listener {

    private final SoulLinkPlugin plugin;
    private final java.util.Set<java.util.UUID> hungerGuard = new java.util.HashSet<>();

    public HungerListener(SoulLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getConfig().getBoolean("sync-hunger", true)) return;
        if (hungerGuard.contains(player.getUniqueId())) return;

        Player partner = plugin.getLinkManager().getPartner(player);
        if (partner == null || !partner.isOnline()) return;

        int newFood = event.getFoodLevel();

        hungerGuard.add(partner.getUniqueId());
        try {
            partner.setFoodLevel(newFood);
            partner.setSaturation(player.getSaturation());
            partner.setExhaustion(player.getExhaustion());
        } finally {
            hungerGuard.remove(partner.getUniqueId());
        }
    }
}
