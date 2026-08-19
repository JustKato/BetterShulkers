package com.danlegt.bettershulkers.Events;

import com.danlegt.bettershulkers.BetterShulkers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShulkerDropEventTest {

    private ShulkerDropEvent listener;

    @BeforeAll
    @SuppressWarnings("unchecked")
    static void setUpServer() throws Exception {
        // Set Bukkit.server BEFORE Registry or Sound static initializers run.
        // Both interfaces call Bukkit.getRegistry() in their <clinit> and need a non-null server.
        var server = mock(Server.class, withSettings().stubOnly());
        when(server.getLogger()).thenReturn(Logger.getLogger("test"));

        // Set server FIRST before any Bukkit class loads.
        Field serverField = Bukkit.class.getDeclaredField("server");
        serverField.setAccessible(true);
        serverField.set(null, server);

        // Stub getRegistry to return null initially — Registry.<clinit> will requireNonNull() the result,
        // so we need the proxy BEFORE forName. Use a trampoline: stub with a supplier that builds lazily.
        // Workaround: use answer-based stubbing so the proxy is created inside the answer (avoids forName ordering).
        doAnswer(invoc -> {
            var cls = invoc.getArgument(0, Class.class);
            return Proxy.newProxyInstance(
                    cls.getClassLoader(),
                    new Class<?>[]{ Class.forName("org.bukkit.Registry") },
                    (proxy, method, args) -> switch (method.getName()) {
                        case "get" -> null;
                        case "getOrThrow" -> null;
                        case "stream" -> Stream.empty();
                        case "iterator" -> Collections.emptyIterator();
                        default -> null;
                    });
        }).when(server).getRegistry(any());
    }

    @BeforeEach
    void setUp() {
        listener = new ShulkerDropEvent();
        ShulkerDropEvent.openShulkerPlayerMap.clear();
        ShulkerDropEvent.shulkerInventoryBinds.clear();
        BetterShulkers.me = mock(BetterShulkers.class);
    }

    // --- helpers ---

    /**
     * ItemStack subclass that returns a preset meta without needing a Bukkit server.
     */
    static class StubItemStack extends ItemStack {
        private final org.bukkit.inventory.meta.ItemMeta preset;

        StubItemStack(Material mat, org.bukkit.inventory.meta.ItemMeta preset) {
            super(mat);
            this.preset = preset;
        }

        @Override
        public org.bukkit.inventory.meta.ItemMeta getItemMeta() {
            return preset;
        }

        @Override
        public boolean setItemMeta(org.bukkit.inventory.meta.ItemMeta meta) {
            return true;
        }
    }

    private Player mockPlayer(ItemStack mainHand) {
        var player = mock(Player.class);
        var inv = mock(PlayerInventory.class);
        when(player.getInventory()).thenReturn(inv);
        when(inv.getItemInMainHand()).thenReturn(mainHand);
        when(player.hasPermission("bettershulkers.shulkerboxaccess")).thenReturn(true);
        when(player.getLocation()).thenReturn(mock(Location.class));
        return player;
    }

    private ItemStack shulkerItem() {
        var inv = mock(Inventory.class);
        when(inv.getContents()).thenReturn(new ItemStack[27]);

        var shulkerBox = mock(ShulkerBox.class);
        when(shulkerBox.getInventory()).thenReturn(inv);

        var bsm = mock(BlockStateMeta.class);
        when(bsm.getBlockState()).thenReturn(shulkerBox);

        return new StubItemStack(Material.SHULKER_BOX, bsm);
    }

    private PlayerDropItemEvent dropEvent(Player player, ItemStack item, boolean sneaking) {
        when(player.isSneaking()).thenReturn(sneaking);
        var dropped = mock(Item.class);
        when(dropped.getItemStack()).thenReturn(item);
        var event = new PlayerDropItemEvent(player, dropped);
        listener.onShulkerDrop(event);
        return event;
    }

    // --- tests ---

    @Test
    void sneakDropShulker_cancelsDropAndTracksPlayer() {
        var item = shulkerItem();
        var player = mockPlayer(item);

        var event = dropEvent(player, item, true);

        assertTrue(event.isCancelled(), "Drop should be cancelled");
        assertTrue(ShulkerDropEvent.openShulkerPlayerMap.containsKey(player), "Player should be tracked");
        assertFalse(ShulkerDropEvent.shulkerInventoryBinds.isEmpty(), "Inventory should be registered");
    }

    @Test
    void nonSneakDrop_doesNotOpenShulker() {
        var item = shulkerItem();
        var player = mockPlayer(item);

        var event = dropEvent(player, item, false);

        assertFalse(event.isCancelled());
        assertFalse(ShulkerDropEvent.openShulkerPlayerMap.containsKey(player));
    }

    @Test
    void nonShulkerDrop_doesNotOpenShulker() {
        var meta = mock(org.bukkit.inventory.meta.ItemMeta.class);
        var item = new StubItemStack(Material.DIRT, meta);
        var player = mockPlayer(item);

        var event = dropEvent(player, item, true);

        assertFalse(event.isCancelled());
        assertFalse(ShulkerDropEvent.openShulkerPlayerMap.containsKey(player));
    }

    @Test
    void inventoryClose_removesFromBothMaps() {
        var item = shulkerItem();
        var player = mockPlayer(item);
        dropEvent(player, item, true);

        var inv = ShulkerDropEvent.shulkerInventoryBinds.iterator().next();
        var view = mock(InventoryView.class);
        when(view.getPlayer()).thenReturn(player);
        when(view.getTopInventory()).thenReturn(inv);

        listener.onShulkerInventoryClose(new InventoryCloseEvent(view));

        assertFalse(ShulkerDropEvent.openShulkerPlayerMap.containsKey(player));
        assertFalse(ShulkerDropEvent.shulkerInventoryBinds.contains(inv));
    }

    @Test
    void inventoryClose_playsCloseSound() {
        var item = shulkerItem();
        var player = mockPlayer(item);
        dropEvent(player, item, true);

        var inv = ShulkerDropEvent.shulkerInventoryBinds.iterator().next();
        var view = mock(InventoryView.class);
        when(view.getPlayer()).thenReturn(player);
        when(view.getTopInventory()).thenReturn(inv);

        // Count playSound calls before and after close — close should add exactly one more
        listener.onShulkerInventoryClose(new InventoryCloseEvent(view));

        // Sound constants are null (no server), but playSound should still be called once on close
        verify(player, atLeast(2)).playSound(any(Location.class), (Sound) isNull(), any(SoundCategory.class), anyFloat(), anyFloat());
    }

    @Test
    void dupeProtection_closesInventoryWhenItemChanged() {
        var item = shulkerItem();
        var player = mockPlayer(item);
        dropEvent(player, item, true);

        var inv = ShulkerDropEvent.shulkerInventoryBinds.iterator().next();

        // Swap main hand to a different item
        var dirt = new StubItemStack(Material.DIRT, null);
        when(player.getInventory().getItemInMainHand()).thenReturn(dirt);

        var view = mock(InventoryView.class);
        when(view.getPlayer()).thenReturn(player);
        when(view.getTopInventory()).thenReturn(inv);

        var clickEvent = new InventoryClickEvent(view, InventoryType.SlotType.CONTAINER, 0,
                ClickType.LEFT, InventoryAction.PICKUP_ONE);
        listener.onShulkerInventoryClick(clickEvent);

        assertFalse(ShulkerDropEvent.openShulkerPlayerMap.containsKey(player), "Dupe protection should remove player");
        verify(player).closeInventory();
    }
}
