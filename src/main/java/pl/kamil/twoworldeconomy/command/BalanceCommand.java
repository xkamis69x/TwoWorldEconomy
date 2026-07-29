package pl.kamil.twoworldeconomy.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.kamil.twoworldeconomy.config.MessageService;
import pl.kamil.twoworldeconomy.economy.EconomyService;
import pl.kamil.twoworldeconomy.util.MoneyFormat;

import java.util.Map;

public final class BalanceCommand implements CommandExecutor {
    public enum Type { WALLET, BANK }

    private final EconomyService economy;
    private final MessageService messages;
    private final MoneyFormat money;
    private final Type type;

    public BalanceCommand(EconomyService economy, MessageService messages, MoneyFormat money, Type type) {
        this.economy = economy;
        this.messages = messages;
        this.money = money;
        this.type = type;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        OfflinePlayer target;
        if (args.length > 0) {
            if (!sender.hasPermission("twoworldeconomy.balance.others")) {
                messages.send(sender, "no-permission"); return true;
            }
            target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                messages.send(sender, "player-not-found", Map.of("player", args[0])); return true;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            messages.send(sender, "player-only"); return true;
        }

        String formatted = type == Type.WALLET
                ? money.format(economy.getWalletBalance(target.getUniqueId()))
                : money.format(economy.getBankBalance(target.getUniqueId()));
        messages.send(sender, type == Type.WALLET ? "wallet-balance" : "bank-balance",
                Map.of(type == Type.WALLET ? "wallet" : "bank", formatted));
        return true;
    }
}
