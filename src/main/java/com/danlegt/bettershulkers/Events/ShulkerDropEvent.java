package com.danlegt.bettershulkers.Events;

import com.danlegt.bettershulkers.BetterShulkers;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.*;

public class ShulkerDropEvent implements Listener {

    public static Map<Player, ItemStack> openShulkerPlayerMap = new HashMap<>();

    public static List<Inventory> shulkerInventoryBinds = new ArrayList<>();

    // React each time a shulker is dropped
    @EventHandler(priority = EventPriority.LOWEST)
    public void onShulkerDrop(PlayerDropItemEvent ev) {
        // Get a reference to the player
        var p = ev.getPlayer();

        // Check if the player has the required permission to perform the action
        if (!p.hasPermission("bettershulkers.shulkerboxaccess")) {
            return;
        }

        // Check if the player is not sneaking
        if (!p.isSneaking()) return;

        // Get a reference to the dropped item
        var item = ev.getItemDrop().getItemStack();
        // Get the meta of the dropped item
        var meta = item.getItemMeta();

        // Check if the meta of the item is null, if it is simply ignore this event.
        if (meta == null) return;

        // Get the ShulkerBox meta
        var sm = getShulkerMeta(meta);

        // Check if the item is a shulker
        if (sm == null) return;

        // Get a reference to the inventory
        var inv = sm.getInventory();
        // Mark the inventory as a shulker listener
        shulkerInventoryBinds.add(inv);

        // Open the inventory of the shulker for the player
        p.openInventory(inv);

        // Mark this shulker as the opened shulker
        openShulkerPlayerMap.put(p, item);

        // Increment shulker open metric
        BetterShulkers.me.incrementShulkersOpened();

        // Play open sound
        p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 1f, 1.25f);
        // Cancel the throw event
        ev.setCancelled(true);
    }

    @EventHandler
    public void onShulkerInventoryClose(InventoryCloseEvent ev) {
        var inv = ev.getInventory();
        if (!shulkerInventoryBinds.contains(inv)) return;
        var item = ev.getPlayer().getInventory().getItemInMainHand();
        if (!(ev.getPlayer() instanceof Player p)) return;

        handleInventoryShananigans(p, inv, item);
        p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 1f, 1.25f);
        openShulkerPlayerMap.remove(p);
    }

    @EventHandler
    public void onShulkerInventoryClick(InventoryClickEvent ev) {
        var inv = ev.getInventory();
        if (!shulkerInventoryBinds.contains(inv)) return;
        if (!(ev.getWhoClicked() instanceof Player p)) return;
        var item = p.getInventory().getItemInMainHand();

        handleInventoryShananigans(p, inv, item);
    }

    @EventHandler
    public void onShulkerInventoryInteract(InventoryInteractEvent ev) {
        var inv = ev.getInventory();
        if (!shulkerInventoryBinds.contains(inv)) return;
        if (!(ev.getWhoClicked() instanceof Player p)) return;
        var item = p.getInventory().getItemInMainHand();

        handleInventoryShananigans(p, inv, item);
    }

    private static void handleInventoryShananigans(Player p, Inventory inv, ItemStack item) {

        // ! Check that this is the correct Shulker
        if ( Objects.isNull(item) || Objects.isNull(openShulkerPlayerMap.get(p)) || !openShulkerPlayerMap.get(p).equals(item) ) {
            Bukkit.getLogger().warning("Player " + p.getName() + " has tried to duplicate, or has accidentally switched shulker boxes");
            openShulkerPlayerMap.remove(p);
            shulkerInventoryBinds.remove(inv);
            p.closeInventory();
            return;
        }

        // Get the meta of the item
        var meta = item.getItemMeta();

        // Check if the meta of the item is null, if it is simply ignore this event.
        if (meta == null) return;
        // Get the ShulkerBox meta
        var sm = getShulkerMeta(meta);
        // Check if the meta is indeed a ShulkerBox meta
        if (sm == null) return;
        // Set the shulker box's inventory contents
        sm.getInventory().setContents(inv.getContents());
        // Important: Update the BlockStateMeta with the modified ShulkerBox
        BlockStateMeta bsm = (BlockStateMeta) meta;
        bsm.setBlockState(sm);
        // Finally, apply the updated BlockStateMeta back to the original item
        item.setItemMeta(bsm);

        openShulkerPlayerMap.put(p, p.getInventory().getItemInMainHand());
    }

    private static ShulkerBox getShulkerMeta(@Nonnull ItemMeta meta) {
        if (!(meta instanceof BlockStateMeta bsm)) return null;
        var csm = bsm.getBlockState();
        if (!(csm instanceof ShulkerBox sb)) return null;

        return sb;
    }

}
