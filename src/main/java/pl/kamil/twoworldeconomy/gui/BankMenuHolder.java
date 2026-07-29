package pl.kamil.twoworldeconomy.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class BankMenuHolder implements InventoryHolder {
    private final UUID playerId;
    private Inventory inventory;

    public BankMenuHolder(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID playerId() { return playerId; }
    public void bind(Inventory inventory) { this.inventory = inventory; }

    @Override
    public @NotNull Inventory getInventory() {
        if (inventory == null) throw new IllegalStateException("Inventory not bound");
        return inventory;
    }
}
