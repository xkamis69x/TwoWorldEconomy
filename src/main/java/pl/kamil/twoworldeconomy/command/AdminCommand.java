package pl.kamil.twoworldeconomy.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.kamil.twoworldeconomy.TwoWorldEconomyPlugin;
import pl.kamil.twoworldeconomy.config.MessageService;
import pl.kamil.twoworldeconomy.economy.EconomyService;
import pl.kamil.twoworldeconomy.util.MoneyFormat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AdminCommand implements CommandExecutor, TabCompleter {
    private final TwoWorldEconomyPlugin plugin;
    private final EconomyService economy;
    private final MessageService messages;
    private final MoneyFormat money;

    public AdminCommand(TwoWorldEconomyPlugin plugin, EconomyService economy, MessageService messages, MoneyFormat money) {
        this.plugin = plugin; this.economy = economy; this.messages = messages; this.money = money;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("twoworldeconomy.admin")) { messages.send(sender, "no-permission"); return true; }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig(); messages.send(sender, "reloaded"); return true;
        }
        if (args.length < 2) return false;
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) { messages.send(sender, "player-not-found", Map.of("player", args[1])); return true; }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("balance")) {
            sender.sendMessage(messages.prefix() + "§7Gracz: §f" + target.getName());
            sender.sendMessage("§7Portfel: §a" + money.format(economy.getWalletBalance(target.getUniqueId())));
            sender.sendMessage("§7Bank: §e" + money.format(economy.getBankBalance(target.getUniqueId())));
            return true;
        }
        if (args.length < 3) return false;
        BigDecimal amount;
        try { amount = new BigDecimal(args[2].replace(',', '.')); }
        catch (NumberFormatException e) { messages.send(sender, "invalid-amount"); return true; }
        boolean ok = switch (sub) {
            case "setwallet" -> economy.setWalletBalance(target.getUniqueId(), amount);
            case "setbank" -> economy.setBankBalance(target.getUniqueId(), amount);
            case "addwallet" -> economy.addWallet(target.getUniqueId(), amount);
            case "addbank" -> economy.addBank(target.getUniqueId(), amount);
            default -> false;
        };
        if (!ok) { messages.send(sender, "invalid-amount"); return true; }
        sender.sendMessage(messages.prefix() + "§aZmieniono saldo gracza §f" + target.getName() + "§a.");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return filter(List.of("balance", "setwallet", "setbank", "addwallet", "addbank", "reload"), args[0]);
        if (args.length == 2 && !args[0].equalsIgnoreCase("reload")) {
            List<String> names = Bukkit.getOnlinePlayers().stream().map(Player -> Player.getName()).toList();
            return filter(names, args[1]);
        }
        return List.of();
    }

    private List<String> filter(List<String> values, String token) {
        String lower = token.toLowerCase(Locale.ROOT); List<String> result = new ArrayList<>();
        for (String value : values) if (value.toLowerCase(Locale.ROOT).startsWith(lower)) result.add(value);
        return result;
    }
}
