package com.danlegt.bettershulkers.Events;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
        // Get the currently open inventory
        var openedInventory = ev.getPlayer().getOpenInventory();

        // Check if the currently open inventory is a shulker
        if ( openedInventory.getType().equals(InventoryType.SHULKER_BOX) )
            // Cancel the event
            ev.setCancelled(true);
    }

    @EventHandler
    public void onMoveOpenedShulkers(InventoryClickEvent ev) {
        if ( !(ev.getWhoClicked() instanceof Player p) )
            return;

        var itemRef = p.getInventory().getItemInMainHand();
        var invRef = ev.getInventory();

        // Check if this is even a shulker inventory
        if ( !invRef.getType().equals(InventoryType.SHULKER_BOX) )
            return;

        // Make sure the item that is being moved is not the shulker.
        if ( itemRef.equals(ev.getCurrentItem()) ) {
            Bukkit.broadcastMessage("Shulker Moved");
            ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onShulkerPlace(BlockPlaceEvent ev) {
        var blockPlaced = ev.getBlockPlaced();

        // Check if the block being placed is even a shulker
        if ( !(blockPlaced.getType().equals(Material.SHULKER_BOX)) )
            return;

        // Get the currently open inventory
        var openedInventory = ev.getPlayer().getOpenInventory();

        // Check if the currently open inventory is a shulker
        if ( openedInventory.getType().equals(InventoryType.SHULKER_BOX) )
            // Cancel the event
            ev.setCancelled(true);

    }


}
