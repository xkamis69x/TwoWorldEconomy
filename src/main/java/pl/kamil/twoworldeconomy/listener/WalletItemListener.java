package pl.kamil.twoworldeconomy.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import pl.kamil.twoworldeconomy.TwoWorldEconomyPlugin;
import pl.kamil.twoworldeconomy.gui.WalletItemService;

public final class WalletItemListener implements Listener {
    private final TwoWorldEconomyPlugin plugin;
    private final WalletItemService walletItems;

    public WalletItemListener(TwoWorldEconomyPlugin plugin, WalletItemService walletItems) {
        this.plugin = plugin;
        this.walletItems = walletItems;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, () -> walletItems.refresh(event.getPlayer()));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!walletItems.isWalletItem(event.getItem())) return;
        event.setCancelled(true);
        walletItems.openBank(event.getPlayer());
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (walletItems.isWalletItem(event.getItemDrop().getItemStack())) event.setCancelled(true);
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (walletItems.isWalletItem(event.getMainHandItem()) || walletItems.isWalletItem(event.getOffHandItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        if (walletItems.isWalletItem(current) || walletItems.isWalletItem(cursor)) {
            event.setCancelled(true);
            plugin.getServer().getScheduler().runTask(plugin, () -> walletItems.refresh(player));
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (walletItems.isWalletItem(event.getOldCursor())) event.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.getDrops().removeIf(walletItems::isWalletItem);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, () -> walletItems.refresh(event.getPlayer()));
    }
}
