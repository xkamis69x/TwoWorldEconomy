package pl.kamil.twoworldeconomy.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.kamil.twoworldeconomy.economy.EconomyService;

public final class PlayerDataListener implements Listener {
    private final EconomyService economy;

    public PlayerDataListener(EconomyService economy) {
        this.economy = economy;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        economy.saveAndUnload(event.getPlayer().getUniqueId());
    }
}
