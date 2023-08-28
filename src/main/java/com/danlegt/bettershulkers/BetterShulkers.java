package com.danlegt.bettershulkers;

import com.danlegt.bettershulkers.Events.ShulkerDropEvent;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class BetterShulkers extends JavaPlugin {

    @Override
    public void onEnable() {
        // Register the drop event
        getServer().getPluginManager().registerEvents(new ShulkerDropEvent(), this);
        // Init bStats
        Metrics metrics = new Metrics(this, 19672);
    }

}
