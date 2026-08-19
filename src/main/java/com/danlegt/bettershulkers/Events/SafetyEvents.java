package com.danlegt.bettershulkers.Events;

import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;

public class SafetyEvents implements Listener {

    @EventHandler
    public void onPlayerDropOpenedShulker(PlayerDropItemEvent ev) {
        if (ev.getPlayer().getOpenInventory().getType().equals(InventoryType.SHULKER_BOX))
            ev.setCancelled(true);
    }

    @EventHandler
    public void onMoveOpenedShulkers(InventoryClickEvent ev) {
        if (!(ev.getWhoClicked() instanceof Player p)) return;
        var invRef = ev.getInventory();
        if (!invRef.getType().equals(InventoryType.SHULKER_BOX)) return;

        if (p.getInventory().getItemInMainHand().equals(ev.getCurrentItem()))
            ev.setCancelled(true);
    }

    @EventHandler
    public void onShulkerPlace(BlockPlaceEvent ev) {
        if (!Tag.SHULKER_BOXES.isTagged(ev.getBlockPlaced().getType())) return;
        if (ev.getPlayer().getOpenInventory().getType().equals(InventoryType.SHULKER_BOX))
            ev.setCancelled(true);
    }
}
