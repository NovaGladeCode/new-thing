package org.novagladecode.soullink;

import org.bukkit.plugin.java.JavaPlugin;
import org.novagladecode.soullink.commands.SoulLinkCommand;
import org.novagladecode.soullink.commands.PauseCommand;
import org.novagladecode.soullink.commands.DayCommand;
import org.novagladecode.soullink.listeners.DamageListener;
import org.novagladecode.soullink.listeners.InventoryListener;
import org.novagladecode.soullink.listeners.HungerListener;
import org.novagladecode.soullink.listeners.OffhandArmorListener;
import org.novagladecode.soullink.listeners.HardcoreListener;
import org.novagladecode.soullink.listeners.DayCounterListener;
import org.novagladecode.soullink.managers.LinkManager;
import org.novagladecode.soullink.managers.PauseManager;
import org.novagladecode.soullink.managers.DayManager;

import java.util.Objects;

public class SoulLinkPlugin extends JavaPlugin {

    private static SoulLinkPlugin instance;

    private LinkManager linkManager;
    private PauseManager pauseManager;
    private DayManager dayManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Managers
        linkManager = new LinkManager(this);
        pauseManager = new PauseManager(this);
        dayManager = new DayManager(this);

        // Listeners
        getServer().getPluginManager().registerEvents(new DamageListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new HungerListener(this), this);
        getServer().getPluginManager().registerEvents(new OffhandArmorListener(this), this);
        getServer().getPluginManager().registerEvents(new HardcoreListener(this), this);
        getServer().getPluginManager().registerEvents(new DayCounterListener(this), this);

        // Commands
        Objects.requireNonNull(getCommand("soullink")).setExecutor(new SoulLinkCommand(this));
        Objects.requireNonNull(getCommand("slpause")).setExecutor(new PauseCommand(this));
        Objects.requireNonNull(getCommand("slday")).setExecutor(new DayCommand(this));
        Objects.requireNonNull(getCommand("slreset")).setExecutor(new DayCommand(this));

        getLogger().info("SoulLink enabled! Shared lives, shared pain.");
    }

    @Override
    public void onDisable() {
        dayManager.save();
        getLogger().info("SoulLink disabled.");
    }

    public static SoulLinkPlugin getInstance() { return instance; }
    public LinkManager getLinkManager()         { return linkManager; }
    public PauseManager getPauseManager()       { return pauseManager; }
    public DayManager getDayManager()           { return dayManager; }

    /** Coloured message with plugin prefix */
    public String prefix(String msg) {
        String raw = getConfig().getString("prefix", "&5[&dSoulLink&5]&r ") + msg;
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', raw);
    }
}
