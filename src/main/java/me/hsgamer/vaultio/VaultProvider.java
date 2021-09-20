package me.hsgamer.vaultio;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

public class VaultProvider {
    private Economy vault;

    public void setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null)
            this.vault = economyProvider.getProvider();
    }

    public boolean isValid() {
        return (this.vault != null);
    }

    public void set(UUID name, double amount) {
        withdraw(name, getBalance(name));
        deposit(name, amount);
    }

    public void deposit(UUID name, double amount) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(name);
        this.vault.depositPlayer(p, amount).transactionSuccess();
    }

    public void withdraw(UUID name, double amount) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(name);
        this.vault.withdrawPlayer(p, amount).transactionSuccess();
    }

    public double getBalance(UUID name) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(name);
        return this.vault.getBalance(p);
    }
}
