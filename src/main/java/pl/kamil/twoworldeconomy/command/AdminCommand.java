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
        this.plugin = plugin;
        this.economy = economy;
        this.messages = messages;
        this.money = money;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("twoworldeconomy.admin")) {
            messages.send(sender, "no-permission");
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            messages.send(sender, "reloaded");
            return true;
        }
        if (args.length == 0) {
            usage(sender);
            return true;
        }

        boolean bank = args[0].equalsIgnoreCase("bank");
        int actionIndex = bank ? 1 : 0;
        int playerIndex = bank ? 2 : 1;
        int amountIndex = bank ? 3 : 2;
        if (args.length <= actionIndex) {
            usage(sender);
            return true;
        }

        String action = args[actionIndex].toLowerCase(Locale.ROOT);
        if (!(action.equals("balance") || action.equals("give") || action.equals("take") || action.equals("set"))) {
            // Zgodność ze starymi komendami alpha.1.
            return handleLegacy(sender, args);
        }
        if (args.length <= playerIndex) {
            usage(sender);
            return true;
        }

        OfflinePlayer target = findTarget(sender, args[playerIndex]);
        if (target == null) return true;

        if (action.equals("balance")) {
            showBalance(sender, target);
            return true;
        }
        if (args.length <= amountIndex) {
            usage(sender);
            return true;
        }

        BigDecimal amount = parseAmount(sender, args[amountIndex]);
        if (amount == null) return true;
        boolean success = bank
                ? mutateBank(action, target, amount)
                : mutateWallet(action, target, amount);
        if (!success) {
            messages.send(sender, action.equals("take") ? (bank ? "not-enough-bank" : "not-enough-wallet") : "invalid-amount");
            return true;
        }
        sender.sendMessage(messages.prefix() + "§aZmieniono " + (bank ? "bank" : "portfel") + " gracza §f"
                + safeName(target) + "§a. Nowe saldo: §f"
                + money.format(bank ? economy.getBankBalance(target.getUniqueId()) : economy.getWalletBalance(target.getUniqueId())));
        return true;
    }

    private boolean mutateWallet(String action, OfflinePlayer target, BigDecimal amount) {
        return switch (action) {
            case "give" -> economy.addWallet(target.getUniqueId(), amount);
            case "take" -> economy.removeWallet(target.getUniqueId(), amount);
            case "set" -> economy.setWalletBalance(target.getUniqueId(), amount);
            default -> false;
        };
    }

    private boolean mutateBank(String action, OfflinePlayer target, BigDecimal amount) {
        return switch (action) {
            case "give" -> economy.addBank(target.getUniqueId(), amount);
            case "take" -> economy.removeBank(target.getUniqueId(), amount);
            case "set" -> economy.setBankBalance(target.getUniqueId(), amount);
            default -> false;
        };
    }

    private boolean handleLegacy(CommandSender sender, String[] args) {
        if (args.length < 2) { usage(sender); return true; }
        String sub = args[0].toLowerCase(Locale.ROOT);
        OfflinePlayer target = findTarget(sender, args[1]);
        if (target == null) return true;
        if (sub.equals("balance")) { showBalance(sender, target); return true; }
        if (args.length < 3) { usage(sender); return true; }
        BigDecimal amount = parseAmount(sender, args[2]);
        if (amount == null) return true;
        boolean ok = switch (sub) {
            case "setwallet" -> economy.setWalletBalance(target.getUniqueId(), amount);
            case "setbank" -> economy.setBankBalance(target.getUniqueId(), amount);
            case "addwallet" -> economy.addWallet(target.getUniqueId(), amount);
            case "addbank" -> economy.addBank(target.getUniqueId(), amount);
            case "removewallet" -> economy.removeWallet(target.getUniqueId(), amount);
            case "removebank" -> economy.removeBank(target.getUniqueId(), amount);
            default -> false;
        };
        if (!ok) { messages.send(sender, "invalid-amount"); return true; }
        sender.sendMessage(messages.prefix() + "§aZmieniono saldo gracza §f" + safeName(target) + "§a.");
        return true;
    }

    private OfflinePlayer findTarget(CommandSender sender, String name) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(name);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            messages.send(sender, "player-not-found", Map.of("player", name));
            return null;
        }
        return target;
    }

    private BigDecimal parseAmount(CommandSender sender, String raw) {
        try {
            BigDecimal amount = new BigDecimal(raw.replace(',', '.'));
            if (amount.signum() < 0) throw new NumberFormatException();
            return amount;
        } catch (NumberFormatException ex) {
            messages.send(sender, "invalid-amount");
            return null;
        }
    }

    private void showBalance(CommandSender sender, OfflinePlayer target) {
        sender.sendMessage(messages.prefix() + "§7Gracz: §f" + safeName(target));
        sender.sendMessage("§7Portfel: §a" + money.format(economy.getWalletBalance(target.getUniqueId())));
        sender.sendMessage("§7Bank: §e" + money.format(economy.getBankBalance(target.getUniqueId())));
    }

    private String safeName(OfflinePlayer target) {
        return target.getName() == null ? target.getUniqueId().toString() : target.getName();
    }

    private void usage(CommandSender sender) {
        sender.sendMessage(messages.prefix() + "§e/eco balance <gracz>");
        sender.sendMessage(messages.prefix() + "§e/eco <give|take|set> <gracz> <kwota>");
        sender.sendMessage(messages.prefix() + "§e/eco bank balance <gracz>");
        sender.sendMessage(messages.prefix() + "§e/eco bank <give|take|set> <gracz> <kwota>");
        sender.sendMessage(messages.prefix() + "§e/eco reload");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return filter(List.of("balance", "give", "take", "set", "bank", "reload"), args[0]);
        if (args[0].equalsIgnoreCase("bank")) {
            if (args.length == 2) return filter(List.of("balance", "give", "take", "set"), args[1]);
            if (args.length == 3) return onlineNames(args[2]);
            if (args.length == 4 && !args[1].equalsIgnoreCase("balance")) return filter(List.of("10", "100", "1000", "10000"), args[3]);
            return List.of();
        }
        if (args.length == 2 && !args[0].equalsIgnoreCase("reload")) return onlineNames(args[1]);
        if (args.length == 3 && !args[0].equalsIgnoreCase("balance")) return filter(List.of("10", "100", "1000", "10000"), args[2]);
        return List.of();
    }

    private List<String> onlineNames(String token) {
        return filter(Bukkit.getOnlinePlayers().stream().map(player -> player.getName()).toList(), token);
    }

    private List<String> filter(List<String> values, String token) {
        String lower = token.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String value : values) if (value.toLowerCase(Locale.ROOT).startsWith(lower)) result.add(value);
        return result;
    }
}
