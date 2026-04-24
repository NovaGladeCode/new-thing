package org.novagladecode.soullink.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.novagladecode.soullink.SoulLinkPlugin;

/**
 * Keeps two linked players' inventories perfectly synchronised.
 *
 * Strategy:
 *   - After any inventory change (click, drop, pickup), schedule a 1-tick delayed sync
 *     from the acting player → their partner.
 *   - Uses a sync guard so we don't cause an echo loop.
 */
public class InventoryListener implements Listener {

    private final SoulLinkPlugin plugin;
    private final java.util.Set<java.util.UUID> syncGuard = new java.util.HashSet<>();

    public InventoryListener(SoulLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        scheduleSync(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        scheduleSync(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPickup(PlayerPickupItemEvent event) {
        scheduleSync(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        scheduleSync(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        scheduleSync(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        scheduleSync(event.getPlayer());
    }

    private void scheduleSync(Player player) {
        if (!plugin.getConfig().getBoolean("sync-inventory", true)) return;
        if (syncGuard.contains(player.getUniqueId())) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> syncTo(player), 1L);
    }

    /**
     * Copies the full contents of {@code source}'s inventory (including armor & offhand)
     * to their linked partner.
     */
    private void syncTo(Player source) {
        if (syncGuard.contains(source.getUniqueId())) return;

        Player partner = plugin.getLinkManager().getPartner(source);
        if (partner == null || !partner.isOnline()) return;

        syncGuard.add(partner.getUniqueId());
        try {
            // Main inventory (36 slots: hotbar + main)
            for (int i = 0; i < 36; i++) {
                ItemStack item = source.getInventory().getItem(i);
                partner.getInventory().setItem(i, item == null ? null : item.clone());
            }
            // Armor slots
            partner.getInventory().setHelmet(
                    clone(source.getInventory().getHelmet()));
            partner.getInventory().setChestplate(
                    clone(source.getInventory().getChestplate()));
            partner.getInventory().setLeggings(
                    clone(source.getInventory().getLeggings()));
            partner.getInventory().setBoots(
                    clone(source.getInventory().getBoots()));
            // Offhand
            partner.getInventory().setItemInOffHand(
                    clone(source.getInventory().getItemInOffHand()));
            // Held slot
            partner.getInventory().setHeldItemSlot(source.getInventory().getHeldItemSlot());

            partner.updateInventory();
        } finally {
            syncGuard.remove(partner.getUniqueId());
        }
    }

    private ItemStack clone(ItemStack item) {
        return item == null ? null : item.clone();
    }
}
