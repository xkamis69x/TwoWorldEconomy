package pl.kamil.twoworldeconomy.model;

import java.math.BigDecimal;
import java.util.UUID;

public final class Account {
    private final UUID playerId;
    private BigDecimal wallet;
    private BigDecimal bank;
    private boolean dirty;

    public Account(UUID playerId, BigDecimal wallet, BigDecimal bank) {
        this.playerId = playerId;
        this.wallet = normalize(wallet);
        this.bank = normalize(bank);
    }

    public UUID playerId() { return playerId; }
    public synchronized BigDecimal wallet() { return wallet; }
    public synchronized BigDecimal bank() { return bank; }
    public synchronized boolean dirty() { return dirty; }

    public synchronized void setWallet(BigDecimal value) {
        wallet = normalize(value);
        dirty = true;
    }

    public synchronized void setBank(BigDecimal value) {
        bank = normalize(value);
        dirty = true;
    }

    public synchronized void markSaved() { dirty = false; }

    private static BigDecimal normalize(BigDecimal value) {
        if (value == null || value.signum() < 0) return BigDecimal.ZERO;
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
