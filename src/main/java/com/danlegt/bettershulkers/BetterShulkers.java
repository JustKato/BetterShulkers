package com.danlegt.bettershulkers;

import com.danlegt.bettershulkers.Events.SafetyEvents;
import com.danlegt.bettershulkers.Events.ShulkerDropEvent;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class BetterShulkers extends JavaPlugin {

    @Override
    public void onEnable() {
        // Register Events
        getServer().getPluginManager().registerEvents(new ShulkerDropEvent(), this);
        getServer().getPluginManager().registerEvents(new SafetyEvents(), this);

        // Init bStats
        new Metrics(this, 19672);
    }

}
