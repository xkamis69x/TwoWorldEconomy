package pl.kamil.twoworldeconomy.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.kamil.twoworldeconomy.TwoWorldEconomyPlugin;
import pl.kamil.twoworldeconomy.economy.EconomyService;
import pl.kamil.twoworldeconomy.util.ColorUtil;
import pl.kamil.twoworldeconomy.util.MoneyFormat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class BankMenuService {
    public enum ActionType { DEPOSIT, WITHDRAW }

    private final TwoWorldEconomyPlugin plugin;
    private final EconomyService economy;
    private final MoneyFormat money;

    public BankMenuService(TwoWorldEconomyPlugin plugin, EconomyService economy, MoneyFormat money) {
        this.plugin = plugin;
        this.economy = economy;
        this.money = money;
    }

    public void open(Player player) {
        if (!plugin.getConfig().getBoolean("bank-gui.enabled", true)) return;
        BankMenuHolder holder = new BankMenuHolder(player.getUniqueId());
        int size = normalizeSize(plugin.getConfig().getInt("bank-gui.size", 54));
        Inventory inventory = Bukkit.createInventory(holder, size,
                ColorUtil.color(plugin.getConfig().getString("bank-gui.title", "&8Bank")));
        holder.bind(inventory);
        render(player, inventory);
        player.openInventory(inventory);
    }

    public void refresh(Player player, Inventory inventory) {
        if (!(inventory.getHolder() instanceof BankMenuHolder holder)) return;
        if (!holder.playerId().equals(player.getUniqueId())) return;
        render(player, inventory);
    }

    public void render(Player player, Inventory inventory) {
        inventory.clear();
        fill(inventory);

        inventory.setItem(4, item(Material.GOLD_INGOT, "&6Twoje saldo",
                List.of("&7Portfel: &a" + money.format(economy.getWalletBalance(player.getUniqueId())),
                        "&7Bank: &e" + money.format(economy.getBankBalance(player.getUniqueId())),
                        "", "&8Menu pozostaje otwarte po transakcji.")));

        List<Double> deposits = plugin.getConfig().getDoubleList("bank-gui.amounts.deposit");
        List<Double> withdrawals = plugin.getConfig().getDoubleList("bank-gui.amounts.withdraw");
        int[] depositSlots = {10, 11, 12, 13, 14, 15, 16};
        int[] withdrawSlots = {28, 29, 30, 31, 32, 33, 34};

        for (int i = 0; i < Math.min(deposits.size(), depositSlots.length); i++) {
            BigDecimal amount = BigDecimal.valueOf(deposits.get(i));
            inventory.setItem(depositSlots[i], transactionItem(Material.LIME_DYE, ActionType.DEPOSIT, amount));
        }
        inventory.setItem(17, item(Material.LIME_CONCRETE, "&aWpłać wszystko",
                List.of("&7Przenieś cały portfel do banku.", "", "&eKliknij, aby wpłacić.")));

        for (int i = 0; i < Math.min(withdrawals.size(), withdrawSlots.length); i++) {
            BigDecimal amount = BigDecimal.valueOf(withdrawals.get(i));
            inventory.setItem(withdrawSlots[i], transactionItem(Material.RED_DYE, ActionType.WITHDRAW, amount));
        }
        inventory.setItem(35, item(Material.RED_CONCRETE, "&cWypłać wszystko",
                List.of("&7Przenieś całe saldo banku do portfela.", "", "&eKliknij, aby wypłacić.")));

        inventory.setItem(inventory.getSize() - 5, item(Material.BARRIER, "&cZamknij", List.of("&7Kliknij, aby zamknąć menu.")));
    }

    public BigDecimal amountFromSlot(int slot, ActionType type) {
        int[] slots = type == ActionType.DEPOSIT ? new int[]{10,11,12,13,14,15,16} : new int[]{28,29,30,31,32,33,34};
        List<Double> values = plugin.getConfig().getDoubleList(type == ActionType.DEPOSIT
                ? "bank-gui.amounts.deposit" : "bank-gui.amounts.withdraw");
        for (int i = 0; i < slots.length && i < values.size(); i++) {
            if (slots[i] == slot) return BigDecimal.valueOf(values.get(i));
        }
        return null;
    }

    private ItemStack transactionItem(Material material, ActionType type, BigDecimal amount) {
        String verb = type == ActionType.DEPOSIT ? "Wpłać " : "Wypłać ";
        return item(material, (type == ActionType.DEPOSIT ? "&a" : "&c") + verb + money.format(amount),
                List.of("&7Kwota: &f" + money.format(amount), "", "&eKlikaj wielokrotnie bez zamykania GUI."));
    }

    private void fill(Inventory inventory) {
        ItemStack filler = item(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler);
    }

    private ItemStack item(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ColorUtil.color(name));
        List<String> colored = new ArrayList<>();
        for (String line : lore) colored.add(ColorUtil.color(line));
        meta.setLore(colored);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        stack.setItemMeta(meta);
        return stack;
    }

    private int normalizeSize(int value) {
        int bounded = Math.max(27, Math.min(54, value));
        return ((bounded + 8) / 9) * 9;
    }
}
