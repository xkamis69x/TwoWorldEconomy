package pl.kamil.twoworldeconomy.gui;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import pl.kamil.twoworldeconomy.TwoWorldEconomyPlugin;
import pl.kamil.twoworldeconomy.economy.EconomyService;
import pl.kamil.twoworldeconomy.util.ColorUtil;
import pl.kamil.twoworldeconomy.util.MoneyFormat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class WalletItemService {
    private final TwoWorldEconomyPlugin plugin;
    private final EconomyService economy;
    private final MoneyFormat money;
    private final BankMenuService bankMenu;
    private final NamespacedKey marker;

    public WalletItemService(TwoWorldEconomyPlugin plugin, EconomyService economy, MoneyFormat money,
                             BankMenuService bankMenu) {
        this.plugin = plugin;
        this.economy = economy;
        this.money = money;
        this.bankMenu = bankMenu;
        this.marker = new NamespacedKey(plugin, "wallet_item");
    }

    public void refresh(Player player) {
        removeAll(player);
        if (!plugin.getConfig().getBoolean("wallet-item.enabled", true)) return;
        BigDecimal balance = economy.getWalletBalance(player.getUniqueId());
        if (balance.signum() <= 0 && plugin.getConfig().getBoolean("wallet-item.remove-when-empty", true)) return;

        int slot = Math.max(0, Math.min(8, plugin.getConfig().getInt("wallet-item.slot", 7)));
        PlayerInventory inventory = player.getInventory();
        ItemStack current = inventory.getItem(slot);
        if (current != null && !current.getType().isAir() && !isWalletItem(current)) {
            int empty = inventory.firstEmpty();
            if (empty < 0) {
                plugin.getLogger().warning("Nie można umieścić itemu portfela gracza " + player.getName()
                        + ": brak wolnego slotu.");
                return;
            }
            inventory.setItem(empty, current);
        }
        inventory.setItem(slot, create(balance));
    }

    public void openBank(Player player) {
        bankMenu.open(player);
    }

    public boolean isWalletItem(ItemStack stack) {
        if (stack == null || stack.getType().isAir() || !stack.hasItemMeta()) return false;
        Byte value = stack.getItemMeta().getPersistentDataContainer().get(marker, PersistentDataType.BYTE);
        return value != null && value == (byte) 1;
    }

    public void removeAll(Player player) {
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (isWalletItem(inventory.getItem(i))) inventory.setItem(i, null);
        }
        if (isWalletItem(inventory.getItemInOffHand())) inventory.setItemInOffHand(null);
    }

    private ItemStack create(BigDecimal balance) {
        Material material = Material.matchMaterial(plugin.getConfig().getString("wallet-item.material", "SUNFLOWER"));
        if (material == null || material.isAir()) material = Material.SUNFLOWER;
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ColorUtil.color(plugin.getConfig().getString("wallet-item.name", "&a&l$ PORTFEL")));
        List<String> lore = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("wallet-item.lore")) {
            lore.add(ColorUtil.color(line.replace("%wallet%", money.format(balance))));
        }
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(marker, PersistentDataType.BYTE, (byte) 1);
        stack.setItemMeta(meta);
        return stack;
    }
}
