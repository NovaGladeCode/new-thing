package org.novagladecode.soullink.listeners;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

import org.novagladecode.soullink.SoulLinkPlugin;

/**
 * Makes armor placed in the offhand slot provide real protection.
 *
 * We intercept EntityDamageEvent BEFORE damage is finalised and reduce it
 * by the armor-fraction contributed by the offhand armor piece.
 *
 * Formula: each armor point = 4 % damage reduction, capped at 80 % total.
 * We add the offhand armor points on top of the player's current vanilla armor
 * attribute value to compute the additional reduction fraction.
 */
public class OffhandArmorListener implements Listener {

    private final SoulLinkPlugin plugin;

    private static final Set<Material> ARMOR_ITEMS = EnumSet.of(
            Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET,
            Material.IRON_HELMET, Material.GOLDEN_HELMET,
            Material.DIAMOND_HELMET, Material.NETHERITE_HELMET,
            Material.TURTLE_HELMET,
            Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE,
            Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE,
            Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE,
            Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS,
            Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS,
            Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS,
            Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS,
            Material.IRON_BOOTS, Material.GOLDEN_BOOTS,
            Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS
    );

    /** Vanilla armor point values for each piece */
    private static int getArmorPoints(Material m) {
        return switch (m) {
            case LEATHER_HELMET, CHAINMAIL_HELMET, IRON_HELMET,
                 GOLDEN_HELMET, TURTLE_HELMET             -> 2;
            case DIAMOND_HELMET, NETHERITE_HELMET         -> 3;
            case LEATHER_CHESTPLATE                       -> 3;
            case CHAINMAIL_CHESTPLATE, GOLDEN_CHESTPLATE  -> 5;
            case IRON_CHESTPLATE                          -> 6;
            case DIAMOND_CHESTPLATE, NETHERITE_CHESTPLATE -> 8;
            case LEATHER_LEGGINGS                         -> 2;
            case CHAINMAIL_LEGGINGS, GOLDEN_LEGGINGS      -> 4;
            case IRON_LEGGINGS                            -> 5;
            case DIAMOND_LEGGINGS, NETHERITE_LEGGINGS     -> 6;
            case LEATHER_BOOTS, GOLDEN_BOOTS              -> 1;
            case CHAINMAIL_BOOTS, IRON_BOOTS              -> 2;
            case DIAMOND_BOOTS, NETHERITE_BOOTS           -> 3;
            default                                       -> 0;
        };
    }

    public OffhandArmorListener(SoulLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!plugin.getConfig().getBoolean("sync-armor-offhand", true)) return;

        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand == null || !ARMOR_ITEMS.contains(offhand.getType())) return;

        int offhandPoints = getArmorPoints(offhand.getType());
        if (offhandPoints <= 0) return;

        // Get the player's current total armor value from the attribute (this is
        // what vanilla already accounts for with worn armor).
        AttributeInstance armorAttr = player.getAttribute(Attribute.GENERIC_ARMOR);
        double currentArmor = (armorAttr != null) ? armorAttr.getValue() : 0.0;

        // Vanilla: damage reduction = min(armor * 0.04, 0.80)
        double existingReduction  = Math.min(currentArmor * 0.04, 0.80);
        double withOffhand        = Math.min((currentArmor + offhandPoints) * 0.04, 0.80);
        double additionalFraction = withOffhand - existingReduction;

        if (additionalFraction <= 0.0) return;

        // Reduce the raw pre-armor damage (DAMAGE base) by the additional fraction
        double baseDamage = event.getDamage(EntityDamageEvent.DamageModifier.BASE);
        if (baseDamage <= 0) return;

        // We subtract extra absorption from the ARMOR modifier
        double currentArmorModifier = event.getDamage(EntityDamageEvent.DamageModifier.ARMOR);
        double extra = -(baseDamage * additionalFraction);
        try {
            event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, currentArmorModifier + extra);
        } catch (UnsupportedOperationException ignored) {
            // Fallback: directly reduce final damage
            event.setDamage(Math.max(0, event.getFinalDamage() - baseDamage * additionalFraction));
        }
    }
}
