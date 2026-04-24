package org.novagladecode.soullink.managers;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.novagladecode.soullink.SoulLinkPlugin;

/**
 * Handles pausing and unpausing the game world.
 *
 * Pausing does the following:
 *   - Freezes time (doDaylightCycle = false)
 *   - Freezes weather
 *   - Stops mob spawning
 *   - Every 2 ticks, sets all entity freeze ticks to max so they are frozen in place
 *   - Every 2 ticks, zeroes player velocity so they cannot move
 */
public class PauseManager {

    private final SoulLinkPlugin plugin;
    private boolean paused = false;
    private BukkitTask freezeTask;

    // Entity.setFreezeTicks exists in Paper 1.17+ but Player does NOT have
    // setFreezeTickDuration. We use Entity#setFreezeTicks(int) which is on
    // all Entities including Players in Paper 1.21.x.
    private static final int FREEZE_TICKS = Integer.MAX_VALUE / 2;

    public PauseManager(SoulLinkPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isPaused() { return paused; }

    public void pause() {
        if (paused) return;
        paused = true;

        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        }

        // Every 2 ticks: freeze all entities via powder-snow freeze ticks
        freezeTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    entity.setFreezeTicks(FREEZE_TICKS);
                    if (entity instanceof Player p) {
                        p.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                    }
                }
            }
        }, 0L, 2L);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(plugin.prefix("&eThe world is now &c⏸ PAUSED&e."));
        }
        plugin.getLogger().info("[SoulLink] World paused.");
    }

    public void unpause() {
        if (!paused) return;
        paused = false;

        if (freezeTask != null) {
            freezeTask.cancel();
            freezeTask = null;
        }

        // Unfreeze all entities
        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
            for (Entity entity : world.getEntities()) {
                entity.setFreezeTicks(0);
            }
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(plugin.prefix("&aThe world is now &2▶ RESUMED&a."));
        }
        plugin.getLogger().info("[SoulLink] World resumed.");
    }

    public void toggle() {
        if (paused) unpause(); else pause();
    }
}
