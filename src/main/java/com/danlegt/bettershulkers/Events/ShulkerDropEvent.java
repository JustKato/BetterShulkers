package com.danlegt.bettershulkers.Events;

import com.danlegt.bettershulkers.BetterShulkers;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ShulkerDropEvent implements Listener {

    public static List<Inventory> shulkerInventoryBinds = new ArrayList<>();
    //ArrayList to prevent players from opening and placing Shulkers at the same time to stop duping
    private static ArrayList<Player> blockedPlayers = new ArrayList<>();
    private static ArrayList<Player> openShulkers = new ArrayList<>();

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

        //Prevent player from dropping this Shulker or moving it in inventory
        openShulkers.add(p);
        // Open the inventory of the shulker for the player
        p.openInventory(inv);
        // Play open sound
        p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 1f, 1.25f);
        // Cancel the throw event
        ev.setCancelled(true);
        //block placer from placing the same shulker
        blockedPlayers.add(p);

        Bukkit.getScheduler().runTaskLaterAsynchronously(BetterShulkers.getINSTANCE(), new Runnable() {
            public void run() {
                blockedPlayers.remove(p);
            }
        }, 1L);

    }

    @EventHandler
    public void onPlayerDropOpenedShulker(PlayerDropItemEvent ev) {
        Player p = ev.getPlayer();
        if (!(openShulkers.contains(p))) return;
        var item = p.getInventory().getItemInMainHand();
        if (item.equals(ev.getItemDrop().getItemStack())) ev.setCancelled(true);
    }

    @EventHandler
    public void onMoveOpenedShulkers(InventoryClickEvent ev) {
        if (!(ev.getWhoClicked() instanceof Player p)) return;
        if(!openShulkers.contains(p)) return;
        var item = p.getInventory().getItemInMainHand();
        if (item.equals(ev.getCurrentItem())) ev.setCancelled(true);
    }

    @EventHandler
    public void onShulkerPlace(BlockPlaceEvent ev) {
        Player p = ev.getPlayer();
        if (!(ev.getBlockPlaced().getState() instanceof ShulkerBox)) return;

        // Check if player is in list containing players that opened shulker 1 tick ago
        if (blockedPlayers.contains(p)) ev.setCancelled(true);

    }

    @EventHandler
    public void onShulkerInventoryClose(InventoryCloseEvent ev) {
        var inv = ev.getInventory();
        if (!shulkerInventoryBinds.contains(inv)) return;
        var item = ev.getPlayer().getInventory().getItemInMainHand();
        if (!(ev.getPlayer() instanceof Player p)) return;

        handleInventoryShananigans(inv, item);
        p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 1f, 1.25f);
        openShulkers.remove(p);
    }

    @EventHandler
    public void onShulkerInventoryClick(InventoryClickEvent ev) {
        var inv = ev.getInventory();
        if (!shulkerInventoryBinds.contains(inv)) return;
        if (!(ev.getWhoClicked() instanceof Player p)) return;
        var item = p.getInventory().getItemInMainHand();

        handleInventoryShananigans(inv, item);
    }

    @EventHandler
    public void onShulkerInventoryInteract(InventoryInteractEvent ev) {
        var inv = ev.getInventory();
        if (!shulkerInventoryBinds.contains(inv)) return;
        if (!(ev.getWhoClicked() instanceof Player p)) return;
        var item = p.getInventory().getItemInMainHand();

        handleInventoryShananigans(inv, item);
    }

    private static void handleInventoryShananigans(Inventory inv, ItemStack item) {

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
    }

    private static ShulkerBox getShulkerMeta(@Nonnull ItemMeta meta) {
        if (!(meta instanceof BlockStateMeta bsm)) return null;
        var csm = bsm.getBlockState();
        if (!(csm instanceof ShulkerBox sb)) return null;

        return sb;
    }

}
