package pl.kamil.twoworldeconomy.api;

import java.math.BigDecimal;
import java.util.UUID;

public interface TwoWorldEconomyApi {
    BigDecimal getWalletBalance(UUID playerId);
    BigDecimal getBankBalance(UUID playerId);
    BigDecimal getTotalBalance(UUID playerId);

    boolean setWalletBalance(UUID playerId, BigDecimal amount);
    boolean setBankBalance(UUID playerId, BigDecimal amount);
    boolean addWallet(UUID playerId, BigDecimal amount);
    boolean addBank(UUID playerId, BigDecimal amount);
    boolean removeWallet(UUID playerId, BigDecimal amount);
    boolean removeBank(UUID playerId, BigDecimal amount);
    boolean transferWalletToBank(UUID playerId, BigDecimal amount);
    boolean transferBankToWallet(UUID playerId, BigDecimal amount);
}
