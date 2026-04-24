package org.novagladecode.soullink.listeners;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.TimeSkipEvent;
import org.novagladecode.soullink.SoulLinkPlugin;

/**
 * Increments the day counter every time the world transitions to a new day
 * (i.e. when time is set to 0 — sunrise).
 *
 * TimeSkipEvent fires for /time set, natural day cycle, sleep, etc.
 * We detect whenever the resulting time wraps around (new day begins).
 */
public class DayCounterListener implements Listener {

    private final SoulLinkPlugin plugin;

    public DayCounterListener(SoulLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTimeSkip(TimeSkipEvent event) {
        if (plugin.getPauseManager().isPaused()) return;
        if (event.getSkipReason() == TimeSkipEvent.SkipReason.CUSTOM) return; // ignore /time commands

        World world = event.getWorld();
        long currentTime = world.getTime();
        long newTime = (currentTime + event.getSkipAmount()) % 24000;

        // New day when we cross from night (>= 18000) back to morning (< 6000)
        // or from any point to exactly 0 (sleep shortcut)
        boolean crossedDawn = (currentTime < newTime)
                ? false  // normal tick forward within same day section
                : (newTime < 6000); // wrapped around midnight → new day

        // Simpler reliable approach: detect full day crossings
        long currentDay = world.getFullTime() / 24000;
        long newDay = (world.getFullTime() + event.getSkipAmount()) / 24000;

        if (newDay > currentDay) {
            for (int i = 0; i < (newDay - currentDay); i++) {
                plugin.getDayManager().increment();
            }
        }
    }
}
