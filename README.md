# TwoWorldEconomy 3.0.0-alpha.1

Nowa, czysta implementacja ekonomii dla serwera Paper 1.21.5 / Java 21.

## Najważniejsze cechy

- portfel i bank,
- `/hajs`, `/bank`, `/wplac`, `/wyplac`,
- GUI banku pozostające otwarte po każdej transakcji,
- wielokrotne klikanie przycisków,
- zabezpieczenie przed podwójnym wykonaniem transakcji,
- zapis do `plugins/TwoWorldEconomy/accounts.yml`,
- publiczne API przez Bukkit `ServicesManager`,
- brak własnego scoreboardu — sidebar kontroluje HubCore,
- GitHub Actions budujący gotowy JAR.

## Budowanie na GitHubie

Wgraj zawartość tego folderu do głównego katalogu repozytorium. Workflow `Build TwoWorldEconomy` uruchomi się automatycznie po commicie.

Artefakt:

```text
TwoWorldEconomy-3.0.0-alpha.1.jar
```

## Publiczne API

```java
RegisteredServiceProvider<TwoWorldEconomyApi> registration =
        Bukkit.getServicesManager().getRegistration(TwoWorldEconomyApi.class);

TwoWorldEconomyApi economy = registration.getProvider();
BigDecimal wallet = economy.getWalletBalance(player.getUniqueId());
BigDecimal bank = economy.getBankBalance(player.getUniqueId());
```

## Ważne przy instalacji

1. Zatrzymaj serwer.
2. Zrób kopię folderu starego pluginu.
3. Usuń stary `TwoWorldEconomy-2.0.2.jar`.
4. Wgraj nowy JAR.
5. Nie uruchamiaj obu wersji jednocześnie.

Pierwsza wersja używa nowego pliku `accounts.yml`. Migrację starych sald dodamy po potwierdzeniu dokładnego formatu `balances-v2.properties`.
