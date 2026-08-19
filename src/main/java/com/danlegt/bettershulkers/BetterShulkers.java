package com.danlegt.bettershulkers;

import com.danlegt.bettershulkers.Events.SafetyEvents;
import com.danlegt.bettershulkers.Events.ShulkerDropEvent;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BetterShulkers extends JavaPlugin {

    public static BetterShulkers me;
    private int shulkersOpened = 0;
    private int dupeAttempts = 0;
    private final Map<String, Integer> shulkerColorCounts = new HashMap<>();

    @Override
    public void onEnable() {
        me = this;
        getServer().getPluginManager().registerEvents(new ShulkerDropEvent(), this);
        getServer().getPluginManager().registerEvents(new SafetyEvents(), this);

        @SuppressWarnings("unused")
        var metrics = new Metrics(this, 19672);
        metrics.addCustomChart(new SingleLineChart("shulkers_opened", this::getShulkersOpened));
        metrics.addCustomChart(new SingleLineChart("dupe_attempts", () -> dupeAttempts));
        metrics.addCustomChart(new DrilldownPie("shulker_color_distribution", () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            shulkerColorCounts.forEach((color, count) ->
                    map.put(color, Collections.singletonMap(color, count)));
            return map;
        }));
    }

    private int getShulkersOpened() {
        return shulkersOpened;
    }

    public void incrementShulkersOpened() {
        shulkersOpened++;
    }

    public void incrementDupeAttempts() {
        dupeAttempts++;
    }

    public void recordShulkerColor(String color) {
        shulkerColorCounts.merge(color, 1, Integer::sum);
    }
}
