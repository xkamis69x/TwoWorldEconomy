package pl.kamil.twoworldeconomy.config;

import org.bukkit.command.CommandSender;
import pl.kamil.twoworldeconomy.TwoWorldEconomyPlugin;
import pl.kamil.twoworldeconomy.util.ColorUtil;

import java.util.Map;

public final class MessageService {
    private final TwoWorldEconomyPlugin plugin;

    public MessageService(TwoWorldEconomyPlugin plugin) {
        this.plugin = plugin;
    }

    public String raw(String key) {
        return ColorUtil.color(plugin.getConfig().getString("messages." + key, key));
    }

    public String format(String key, Map<String, String> placeholders) {
        String text = raw(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return text;
    }

    public void send(CommandSender sender, String key) {
        sender.sendMessage(prefix() + raw(key));
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        sender.sendMessage(prefix() + format(key, placeholders));
    }

    public String prefix() {
        return raw("prefix");
    }
}
