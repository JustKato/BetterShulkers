package com.danlegt.bettershulkers;

import com.danlegt.bettershulkers.Events.SafetyEvents;
import com.danlegt.bettershulkers.Events.ShulkerDropEvent;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;

public final class BetterShulkers extends JavaPlugin {
    // Singleton
    public static BetterShulkers me;
    // A simple stat
    private int shulkersOpened = 0;

    @Override
    public void onEnable() {
        // Singleton Assignation
        me = this;

        // Register Events
        getServer().getPluginManager().registerEvents(new ShulkerDropEvent(), this);
        getServer().getPluginManager().registerEvents(new SafetyEvents(), this);

        // Init bStats
        var metrics = new Metrics(this, 19672);
        // Register custom Single Line Chart
        metrics.addCustomChart(new SingleLineChart("shulkers_opened", this::getShulkersOpened));
    }

    private int getShulkersOpened() {
        return shulkersOpened;
    }

    public void incrementShulkersOpened() {
        shulkersOpened++;
    }

}
