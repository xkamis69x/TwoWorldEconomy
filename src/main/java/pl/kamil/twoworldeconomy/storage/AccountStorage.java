package pl.kamil.twoworldeconomy.storage;

import org.bukkit.configuration.file.YamlConfiguration;
import pl.kamil.twoworldeconomy.TwoWorldEconomyPlugin;
import pl.kamil.twoworldeconomy.model.Account;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

public final class AccountStorage {
    private final TwoWorldEconomyPlugin plugin;
    private final File dataFile;
    private final Object ioLock = new Object();

    public AccountStorage(TwoWorldEconomyPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "accounts.yml");
    }

    public Account load(UUID playerId, BigDecimal startingWallet, BigDecimal startingBank) {
        synchronized (ioLock) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
            String base = "accounts." + playerId;
            BigDecimal wallet = parse(yaml.getString(base + ".wallet"), startingWallet);
            BigDecimal bank = parse(yaml.getString(base + ".bank"), startingBank);
            return new Account(playerId, wallet, bank);
        }
    }

    public void save(Account account) {
        synchronized (ioLock) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);
            String base = "accounts." + account.playerId();
            yaml.set(base + ".wallet", account.wallet().toPlainString());
            yaml.set(base + ".bank", account.bank().toPlainString());
            try {
                yaml.save(dataFile);
                account.markSaved();
            } catch (IOException exception) {
                plugin.getLogger().severe("Nie udało się zapisać konta " + account.playerId() + ": " + exception.getMessage());
            }
        }
    }

    private BigDecimal parse(String value, BigDecimal fallback) {
        if (value == null) return fallback;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
