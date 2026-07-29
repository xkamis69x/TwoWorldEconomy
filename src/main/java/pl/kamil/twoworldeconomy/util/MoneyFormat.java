package pl.kamil.twoworldeconomy.util;

import pl.kamil.twoworldeconomy.TwoWorldEconomyPlugin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class MoneyFormat {
    private final TwoWorldEconomyPlugin plugin;

    public MoneyFormat(TwoWorldEconomyPlugin plugin) {
        this.plugin = plugin;
    }

    public String format(BigDecimal amount) {
        int decimals = Math.max(0, Math.min(6, plugin.getConfig().getInt("currency.decimals", 2)));
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat(decimals == 0 ? "#,##0" : "#,##0." + "0".repeat(decimals), symbols);
        return plugin.getConfig().getString("currency.symbol", "$") + format.format(amount.setScale(decimals, RoundingMode.HALF_UP));
    }
}
