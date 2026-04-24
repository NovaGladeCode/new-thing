package org.novagladecode.soullink.managers;

import org.bukkit.entity.Player;
import org.novagladecode.soullink.SoulLinkPlugin;

import java.util.*;

/**
 * Manages paired (soul-linked) players.
 * The link is bidirectional and stored in memory only — players re-link on each session.
 */
public class LinkManager {

    private final SoulLinkPlugin plugin;

    /** Map from one player UUID to their linked partner UUID */
    private final Map<UUID, UUID> links = new HashMap<>();

    public LinkManager(SoulLinkPlugin plugin) {
        this.plugin = plugin;
    }

    /** Link two players together (bidirectional). */
    public void link(Player a, Player b) {
        // Remove any existing links for both
        unlink(a);
        unlink(b);
        links.put(a.getUniqueId(), b.getUniqueId());
        links.put(b.getUniqueId(), a.getUniqueId());
        plugin.getLogger().info("Linked " + a.getName() + " <-> " + b.getName());
    }

    /** Remove all links for this player. */
    public void unlink(Player player) {
        UUID partner = links.remove(player.getUniqueId());
        if (partner != null) links.remove(partner);
    }

    /** @return the linked partner of this player, or null if unlinked / offline. */
    public Player getPartner(Player player) {
        UUID partnerUUID = links.get(player.getUniqueId());
        if (partnerUUID == null) return null;
        return plugin.getServer().getPlayer(partnerUUID);
    }

    /** True if this player is currently soul-linked. */
    public boolean isLinked(Player player) {
        return links.containsKey(player.getUniqueId());
    }

    /** True if the two UUIDs are linked together. */
    public boolean areLinked(UUID a, UUID b) {
        return b.equals(links.get(a));
    }
}
