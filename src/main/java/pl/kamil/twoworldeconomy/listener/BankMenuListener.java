package pl.kamil.twoworldeconomy.listener;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import pl.kamil.twoworldeconomy.TwoWorldEconomyPlugin;
import pl.kamil.twoworldeconomy.config.MessageService;
import pl.kamil.twoworldeconomy.economy.EconomyService;
import pl.kamil.twoworldeconomy.gui.BankMenuHolder;
import pl.kamil.twoworldeconomy.gui.BankMenuService;
import pl.kamil.twoworldeconomy.util.MoneyFormat;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BankMenuListener implements Listener {
    private final TwoWorldEconomyPlugin plugin;
    private final EconomyService economy;
    private final BankMenuService menu;
    private final MessageService messages;
    private final MoneyFormat money;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public BankMenuListener(TwoWorldEconomyPlugin plugin, EconomyService economy, BankMenuService menu,
                            MessageService messages, MoneyFormat money) {
        this.plugin = plugin;
        this.economy = economy;
        this.menu = menu;
        this.messages = messages;
        this.money = money;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof BankMenuHolder holder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!holder.playerId().equals(player.getUniqueId())) return;
        if (event.getClickedInventory() == null || event.getClickedInventory() != top) return;

        int slot = event.getRawSlot();
        if (slot == top.getSize() - 5) {
            player.closeInventory();
            return;
        }
        if (!acquire(player.getUniqueId())) return;

        boolean success = false;
        String messageKey = null;
        BigDecimal amount = menu.amountFromSlot(slot, BankMenuService.ActionType.DEPOSIT);
        if (amount != null) {
            success = economy.transferWalletToBank(player.getUniqueId(), amount);
            messageKey = success ? "deposited" : "not-enough-wallet";
        } else if (slot == 17) {
            amount = economy.getWalletBalance(player.getUniqueId());
            if (amount.signum() > 0) success = economy.transferWalletToBank(player.getUniqueId(), amount);
            messageKey = success ? "deposited" : "not-enough-wallet";
        } else {
            amount = menu.amountFromSlot(slot, BankMenuService.ActionType.WITHDRAW);
            if (amount != null) {
                success = economy.transferBankToWallet(player.getUniqueId(), amount);
                messageKey = success ? "withdrawn" : "not-enough-bank";
            } else if (slot == 35) {
                amount = economy.getBankBalance(player.getUniqueId());
                if (amount.signum() > 0) success = economy.transferBankToWallet(player.getUniqueId(), amount);
                messageKey = success ? "withdrawn" : "not-enough-bank";
            }
        }

        if (messageKey == null) return;
        if (success) {
            messages.send(player, messageKey, Map.of("amount", money.format(amount)));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.25f);
        } else {
            messages.send(player, messageKey);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        }
        menu.refresh(player, top);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof BankMenuHolder) event.setCancelled(true);
    }

    private boolean acquire(UUID playerId) {
        long now = System.currentTimeMillis();
        long cooldown = Math.max(0L, plugin.getConfig().getLong("bank-gui.transaction-cooldown-millis", 120L));
        Long previous = cooldowns.put(playerId, now);
        return previous == null || now - previous >= cooldown;
    }
}
