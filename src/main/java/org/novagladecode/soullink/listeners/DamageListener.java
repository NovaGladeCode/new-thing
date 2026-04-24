package org.novagladecode.soullink.listeners;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.novagladecode.soullink.SoulLinkPlugin;

/**
 * When one linked player takes damage, their partner receives the same damage.
 * Uses a guard flag to prevent infinite recursion.
 */
public class DamageListener implements Listener {

    private final SoulLinkPlugin plugin;
    /** Guard set so we don't echo the partner's echo back to the first player */
    private final java.util.Set<java.util.UUID> damageGuard = new java.util.HashSet<>();

    public DamageListener(SoulLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getConfig().getBoolean("sync-damage", true)) return;

        // Skip if we caused this event ourselves (partner echo)
        if (damageGuard.contains(player.getUniqueId())) return;

        Player partner = plugin.getLinkManager().getPartner(player);
        if (partner == null || !partner.isOnline()) return;

        double damage = event.getFinalDamage();

        // Guard the partner so the recursion stops at one hop
        damageGuard.add(partner.getUniqueId());
        try {
            // Apply damage directly — bypasses any pending EntityDamageEvent on partner
            partner.damage(damage);
        } finally {
            damageGuard.remove(partner.getUniqueId());
        }

        partner.sendMessage(plugin.prefix("&c" + player.getName() + " took &4" +
                String.format("%.1f", damage) + " ❤ damage — &cyou felt it too!"));
    }
}
