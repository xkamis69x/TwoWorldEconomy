package pl.kamil.twoworldeconomy.api;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Consumer;

/** Publiczne API ekonomii przeznaczone m.in. dla HubCore. */
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

    /** Listener otrzymuje UUID po każdej udanej zmianie portfela lub banku. */
    void addBalanceListener(Consumer<UUID> listener);
    void removeBalanceListener(Consumer<UUID> listener);
}
