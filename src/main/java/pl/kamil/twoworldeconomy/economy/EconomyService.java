package pl.kamil.twoworldeconomy.economy;

import org.bukkit.Bukkit;
import pl.kamil.twoworldeconomy.TwoWorldEconomyPlugin;
import pl.kamil.twoworldeconomy.api.TwoWorldEconomyApi;
import pl.kamil.twoworldeconomy.model.Account;
import pl.kamil.twoworldeconomy.storage.AccountStorage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class EconomyService implements TwoWorldEconomyApi {
    private final TwoWorldEconomyPlugin plugin;
    private final AccountStorage storage;
    private final ConcurrentMap<UUID, Account> accounts = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<Consumer<UUID>> balanceListeners = new CopyOnWriteArrayList<>();

    public EconomyService(TwoWorldEconomyPlugin plugin, AccountStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    private Account account(UUID playerId) {
        return accounts.computeIfAbsent(playerId, id -> storage.load(id, start("wallet"), start("bank")));
    }

    private BigDecimal start(String type) {
        return normalize(BigDecimal.valueOf(plugin.getConfig().getDouble("starting-balance." + type, 0.0)));
    }

    public Collection<Account> cachedAccounts() { return accounts.values(); }

    public void saveDirty() {
        for (Account account : accounts.values()) {
            if (account.dirty()) storage.save(account);
        }
    }

    public void saveAndUnload(UUID playerId) {
        Account account = accounts.remove(playerId);
        if (account != null && account.dirty()) storage.save(account);
    }

    @Override public BigDecimal getWalletBalance(UUID id) { return account(id).wallet(); }
    @Override public BigDecimal getBankBalance(UUID id) { return account(id).bank(); }
    @Override public BigDecimal getTotalBalance(UUID id) { return getWalletBalance(id).add(getBankBalance(id)); }

    @Override public boolean setWalletBalance(UUID id, BigDecimal amount) {
        if (!validNonNegative(amount)) return false;
        account(id).setWallet(normalize(amount));
        changed(id);
        return true;
    }

    @Override public boolean setBankBalance(UUID id, BigDecimal amount) {
        if (!validNonNegative(amount)) return false;
        account(id).setBank(normalize(amount));
        changed(id);
        return true;
    }

    @Override public boolean addWallet(UUID id, BigDecimal amount) {
        if (!validPositive(amount)) return false;
        Account a = account(id);
        synchronized (a) { a.setWallet(a.wallet().add(normalize(amount))); }
        changed(id);
        return true;
    }

    @Override public boolean addBank(UUID id, BigDecimal amount) {
        if (!validPositive(amount)) return false;
        Account a = account(id);
        synchronized (a) { a.setBank(a.bank().add(normalize(amount))); }
        changed(id);
        return true;
    }

    @Override public boolean removeWallet(UUID id, BigDecimal amount) {
        if (!validPositive(amount)) return false;
        Account a = account(id);
        synchronized (a) {
            BigDecimal n = normalize(amount);
            if (a.wallet().compareTo(n) < 0) return false;
            a.setWallet(a.wallet().subtract(n));
        }
        changed(id);
        return true;
    }

    @Override public boolean removeBank(UUID id, BigDecimal amount) {
        if (!validPositive(amount)) return false;
        Account a = account(id);
        synchronized (a) {
            BigDecimal n = normalize(amount);
            if (a.bank().compareTo(n) < 0) return false;
            a.setBank(a.bank().subtract(n));
        }
        changed(id);
        return true;
    }

    @Override public boolean transferWalletToBank(UUID id, BigDecimal amount) {
        if (!validPositive(amount)) return false;
        Account a = account(id);
        synchronized (a) {
            BigDecimal n = normalize(amount);
            if (a.wallet().compareTo(n) < 0) return false;
            a.setWallet(a.wallet().subtract(n));
            a.setBank(a.bank().add(n));
        }
        changed(id);
        return true;
    }

    @Override public boolean transferBankToWallet(UUID id, BigDecimal amount) {
        if (!validPositive(amount)) return false;
        Account a = account(id);
        synchronized (a) {
            BigDecimal n = normalize(amount);
            if (a.bank().compareTo(n) < 0) return false;
            a.setBank(a.bank().subtract(n));
            a.setWallet(a.wallet().add(n));
        }
        changed(id);
        return true;
    }

    @Override public void addBalanceListener(Consumer<UUID> listener) {
        if (listener != null) balanceListeners.addIfAbsent(listener);
    }

    @Override public void removeBalanceListener(Consumer<UUID> listener) {
        balanceListeners.remove(listener);
    }

    private void changed(UUID playerId) {
        Runnable notification = () -> {
            for (Consumer<UUID> listener : balanceListeners) {
                try { listener.accept(playerId); }
                catch (RuntimeException ex) { plugin.getLogger().warning("Błąd listenera zmiany salda: " + ex.getMessage()); }
            }
        };
        if (Bukkit.isPrimaryThread()) notification.run();
        else Bukkit.getScheduler().runTask(plugin, notification);
    }

    private boolean validPositive(BigDecimal v) { return v != null && v.signum() > 0; }
    private boolean validNonNegative(BigDecimal v) { return v != null && v.signum() >= 0; }
    private BigDecimal normalize(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }
}
