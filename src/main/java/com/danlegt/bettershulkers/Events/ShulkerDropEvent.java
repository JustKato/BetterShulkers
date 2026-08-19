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

import java.util.*;

public class ShulkerDropEvent implements Listener {

    // ponytail: static maps are fine here; one plugin instance per JVM, single server thread
    static final Map<Player, ItemStack> openShulkerPlayerMap = new HashMap<>();
    static final Set<Inventory> shulkerInventoryBinds = new HashSet<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShulkerDrop(PlayerDropItemEvent ev) {
        var p = ev.getPlayer();
        if (!p.hasPermission("bettershulkers.shulkerboxaccess")) return;
        if (!p.isSneaking()) return;

        var item = ev.getItemDrop().getItemStack();
        var meta = item.getItemMeta();
        if (meta == null) return;

        var sm = getShulkerMeta(meta);
        if (sm == null) return;

        var inv = sm.getInventory();
        shulkerInventoryBinds.add(inv);
        p.openInventory(inv);
        openShulkerPlayerMap.put(p, item);
        BetterShulkers.me.incrementShulkersOpened();
        BetterShulkers.me.recordShulkerColor(item.getType().name());
        p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 1f, 1.25f);
        ev.setCancelled(true);
    }

    @EventHandler
    public void onShulkerInventoryClose(InventoryCloseEvent ev) {
        var inv = ev.getInventory();
        if (!shulkerInventoryBinds.contains(inv)) return;
        if (!(ev.getPlayer() instanceof Player p)) return;

        var item = p.getInventory().getItemInMainHand();
        handleInventoryShananigans(p, inv, item);
        shulkerInventoryBinds.remove(inv);
        openShulkerPlayerMap.remove(p);
        p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_CLOSE, SoundCategory.BLOCKS, 1f, 1.25f);
    }

    @EventHandler
    public void onShulkerInventoryClick(InventoryClickEvent ev) {
        var inv = ev.getInventory();
        if (!shulkerInventoryBinds.contains(inv)) return;
        if (!(ev.getWhoClicked() instanceof Player p)) return;

        handleInventoryShananigans(p, inv, p.getInventory().getItemInMainHand());
    }

    @EventHandler
    public void onShulkerInventoryInteract(InventoryInteractEvent ev) {
        var inv = ev.getInventory();
        if (!shulkerInventoryBinds.contains(inv)) return;
        if (!(ev.getWhoClicked() instanceof Player p)) return;

        handleInventoryShananigans(p, inv, p.getInventory().getItemInMainHand());
    }

    static void handleInventoryShananigans(Player p, Inventory inv, ItemStack item) {
        if (Objects.isNull(item) || Objects.isNull(openShulkerPlayerMap.get(p)) || !openShulkerPlayerMap.get(p).equals(item)) {
            Bukkit.getLogger().warning("Player " + p.getName() + " has tried to duplicate, or has accidentally switched shulker boxes");
            BetterShulkers.me.incrementDupeAttempts();
            openShulkerPlayerMap.remove(p);
            shulkerInventoryBinds.remove(inv);
            p.closeInventory();
            return;
        }

        var meta = item.getItemMeta();
        if (meta == null) return;

        var sm = getShulkerMeta(meta);
        if (sm == null) return;

        sm.getInventory().setContents(inv.getContents());
        BlockStateMeta bsm = (BlockStateMeta) meta;
        bsm.setBlockState(sm);
        item.setItemMeta(bsm);
    }

    static ShulkerBox getShulkerMeta(ItemMeta meta) {
        if (!(meta instanceof BlockStateMeta bsm)) return null;
        var csm = bsm.getBlockState();
        if (!(csm instanceof ShulkerBox sb)) return null;
        return sb;
    }
}
