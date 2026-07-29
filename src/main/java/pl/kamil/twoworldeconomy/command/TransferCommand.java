package pl.kamil.twoworldeconomy.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.kamil.twoworldeconomy.config.MessageService;
import pl.kamil.twoworldeconomy.economy.EconomyService;
import pl.kamil.twoworldeconomy.util.MoneyFormat;

import java.math.BigDecimal;
import java.util.Map;

public final class TransferCommand implements CommandExecutor {
    public enum Direction { DEPOSIT, WITHDRAW }
    private final EconomyService economy;
    private final MessageService messages;
    private final MoneyFormat money;
    private final Direction direction;

    public TransferCommand(EconomyService economy, MessageService messages, MoneyFormat money, Direction direction) {
        this.economy = economy; this.messages = messages; this.money = money; this.direction = direction;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) { messages.send(sender, "player-only"); return true; }
        if (args.length != 1) return false;
        BigDecimal amount;
        if (args[0].equalsIgnoreCase("all")) {
            amount = direction == Direction.DEPOSIT ? economy.getWalletBalance(player.getUniqueId()) : economy.getBankBalance(player.getUniqueId());
        } else {
            try { amount = new BigDecimal(args[0].replace(',', '.')); }
            catch (NumberFormatException exception) { messages.send(player, "invalid-amount"); return true; }
        }
        if (amount.signum() <= 0) { messages.send(player, "invalid-amount"); return true; }
        boolean success = direction == Direction.DEPOSIT
                ? economy.transferWalletToBank(player.getUniqueId(), amount)
                : economy.transferBankToWallet(player.getUniqueId(), amount);
        if (!success) { messages.send(player, direction == Direction.DEPOSIT ? "not-enough-wallet" : "not-enough-bank"); return true; }
        messages.send(player, direction == Direction.DEPOSIT ? "deposited" : "withdrawn", Map.of("amount", money.format(amount)));
        return true;
    }
}
