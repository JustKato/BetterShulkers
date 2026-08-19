package com.danlegt.bettershulkers;

import com.danlegt.bettershulkers.Events.SafetyEvents;
import com.danlegt.bettershulkers.Events.ShulkerDropEvent;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;

public class BetterShulkers extends JavaPlugin {

    public static BetterShulkers me;
    private int shulkersOpened = 0;

    @Override
    public void onEnable() {
        me = this;
        getServer().getPluginManager().registerEvents(new ShulkerDropEvent(), this);
        getServer().getPluginManager().registerEvents(new SafetyEvents(), this);

        @SuppressWarnings("unused")
        var metrics = new Metrics(this, 19672);
        metrics.addCustomChart(new SingleLineChart("shulkers_opened", this::getShulkersOpened));
    }

    private int getShulkersOpened() {
        return shulkersOpened;
    }

    public void incrementShulkersOpened() {
        shulkersOpened++;
    }
}
