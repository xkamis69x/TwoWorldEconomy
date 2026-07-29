package pl.kamil.twoworldeconomy;

import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import pl.kamil.twoworldeconomy.api.TwoWorldEconomyApi;
import pl.kamil.twoworldeconomy.command.AdminCommand;
import pl.kamil.twoworldeconomy.command.BalanceCommand;
import pl.kamil.twoworldeconomy.command.BankCommand;
import pl.kamil.twoworldeconomy.command.TransferCommand;
import pl.kamil.twoworldeconomy.config.MessageService;
import pl.kamil.twoworldeconomy.economy.EconomyService;
import pl.kamil.twoworldeconomy.gui.BankMenuService;
import pl.kamil.twoworldeconomy.gui.WalletItemService;
import pl.kamil.twoworldeconomy.listener.BankMenuListener;
import pl.kamil.twoworldeconomy.listener.PlayerDataListener;
import pl.kamil.twoworldeconomy.listener.WalletItemListener;
import pl.kamil.twoworldeconomy.storage.AccountStorage;
import pl.kamil.twoworldeconomy.util.MoneyFormat;

import java.util.Objects;

public final class TwoWorldEconomyPlugin extends JavaPlugin {
    private EconomyService economyService;
    private WalletItemService walletItemService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        AccountStorage storage = new AccountStorage(this);
        economyService = new EconomyService(this, storage);
        MessageService messages = new MessageService(this);
        MoneyFormat money = new MoneyFormat(this);
        BankMenuService bankMenu = new BankMenuService(this, economyService, money);
        walletItemService = new WalletItemService(this, economyService, money, bankMenu);

        getServer().getServicesManager().register(TwoWorldEconomyApi.class, economyService, this, ServicePriority.Normal);
        economyService.addBalanceListener(uuid -> {
            Player player = getServer().getPlayer(uuid);
            if (player != null) walletItemService.refresh(player);
        });

        registerCommands(messages, money, bankMenu);
        getServer().getPluginManager().registerEvents(
                new BankMenuListener(this, economyService, bankMenu, messages, money), this);
        getServer().getPluginManager().registerEvents(new PlayerDataListener(economyService), this);
        getServer().getPluginManager().registerEvents(new WalletItemListener(this, walletItemService), this);

        long autosaveTicks = Math.max(20L, getConfig().getLong("storage.autosave-seconds", 60L) * 20L);
        getServer().getScheduler().runTaskTimerAsynchronously(this, economyService::saveDirty, autosaveTicks, autosaveTicks);
        getServer().getScheduler().runTask(this, () -> getServer().getOnlinePlayers().forEach(walletItemService::refresh));

        getLogger().info("TwoWorldEconomy " + getPluginMeta().getVersion() + " uruchomiono.");
        getLogger().info("Plugin nie tworzy scoreboardu. Sidebar kontroluje wyłącznie HubCore.");
    }

    @Override
    public void onDisable() {
        if (economyService != null) economyService.saveDirty();
        getServer().getServicesManager().unregisterAll(this);
    }

    public EconomyService economyService() { return economyService; }
    public EconomyService getEconomyService() { return economyService; }
    public TwoWorldEconomyApi getApi() { return economyService; }
    public TwoWorldEconomyApi getAPI() { return economyService; }

    private void registerCommands(MessageService messages, MoneyFormat money, BankMenuService bankMenu) {
        command("hajs").setExecutor(new BalanceCommand(economyService, messages, money, BalanceCommand.Type.WALLET));
        command("bank").setExecutor(new BankCommand(economyService, bankMenu, messages, money));
        command("wplac").setExecutor(new TransferCommand(economyService, messages, money, TransferCommand.Direction.DEPOSIT));
        command("wyplac").setExecutor(new TransferCommand(economyService, messages, money, TransferCommand.Direction.WITHDRAW));

        AdminCommand admin = new AdminCommand(this, economyService, messages, money);
        command("tweconomy").setExecutor(admin);
        command("tweconomy").setTabCompleter(admin);
    }

    private PluginCommand command(String name) {
        return Objects.requireNonNull(getCommand(name), "Brak komendy " + name + " w plugin.yml");
    }
}
