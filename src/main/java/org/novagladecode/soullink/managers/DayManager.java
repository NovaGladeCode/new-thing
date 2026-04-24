package org.novagladecode.soullink.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.novagladecode.soullink.SoulLinkPlugin;

/**
 * Tracks and persists the in-game day counter.
 * Increments every time the world transitions from day to night (sun rise in Minecraft = new day at tick 0).
 */
public class DayManager {

    private final SoulLinkPlugin plugin;
    private int dayCount;

    public DayManager(SoulLinkPlugin plugin) {
        this.plugin = plugin;
        dayCount = plugin.getConfig().getInt("day-counter", 1);
    }

    public int getDay() { return dayCount; }

    public void increment() {
        dayCount++;
        save();
        broadcastDay();
    }

    public void reset() {
        dayCount = 1;
        save();
    }

    public void save() {
        plugin.getConfig().set("day-counter", dayCount);
        plugin.saveConfig();
    }

    private void broadcastDay() {
        String msg = plugin.prefix("&eSunrise — it is now &6Day " + dayCount + "&e!");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(msg);
            p.sendTitle("", "§6Day " + dayCount, 10, 60, 20);
        }
    }
}
