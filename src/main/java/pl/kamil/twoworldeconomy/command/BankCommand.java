package pl.kamil.twoworldeconomy.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.kamil.twoworldeconomy.config.MessageService;
import pl.kamil.twoworldeconomy.economy.EconomyService;
import pl.kamil.twoworldeconomy.gui.BankMenuService;
import pl.kamil.twoworldeconomy.util.MoneyFormat;

import java.util.Map;

public final class BankCommand implements CommandExecutor {
    private final EconomyService economy;
    private final BankMenuService menu;
    private final MessageService messages;
    private final MoneyFormat money;

    public BankCommand(EconomyService economy, BankMenuService menu, MessageService messages, MoneyFormat money) {
        this.economy = economy; this.menu = menu; this.messages = messages; this.money = money;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) { messages.send(sender, "player-only"); return true; }
        if (args.length > 0 && args[0].equalsIgnoreCase("saldo")) {
            messages.send(player, "bank-balance", Map.of("bank", money.format(economy.getBankBalance(player.getUniqueId()))));
            return true;
        }
        menu.open(player);
        return true;
    }
}
