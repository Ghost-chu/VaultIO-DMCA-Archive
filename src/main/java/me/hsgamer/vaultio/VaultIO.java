package me.hsgamer.vaultio;

import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.common.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class VaultIO extends JavaPlugin {
    private final BukkitConfig moneyConfig = new BukkitConfig(this, "money.yml");
    private final VaultProvider vault = new VaultProvider();
    private CommandSender sender;

    @Override
    public void onEnable() {
        moneyConfig.setup();
        vault.setupEconomy();
        if (!vault.isValid()) {
            getLogger().warning("Failed to init Vault, unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Successfully loaded VaultIO");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.sender = sender;
        if (!sender.hasPermission("vaultio.do"))
            return false;
        if (label.equalsIgnoreCase("vaulto")) {
            (new BukkitRunnable() {
                public void run() {
                    VaultIO.this.out();
                }
            }).runTaskAsynchronously(this);
            return true;
        }
        if (label.equalsIgnoreCase("vaulti")) {
            (new BukkitRunnable() {
                public void run() {
                    VaultIO.this.in();
                }
            }).runTaskAsynchronously(this);
            return true;
        }
        return false;
    }

    private void in() {
        sender.sendMessage(ChatColor.GRAY + "Reading money.yml, Please wait...");
        List<String> moneyInformation = CollectionUtils.createStringListFromObject(moneyConfig.get("data", Collections.emptyList()), true);
        for (String string : moneyInformation) {
            String[] playerDataMap = string.split(":", 2);
            if (playerDataMap.length != 2) {
                sender.sendMessage(ChatColor.RED + "Found a invalid coord in data: " + string + " Skipping...");
                continue;
            }
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(playerDataMap[0]));
            sender.sendMessage(ChatColor.GRAY + "Importing " + player.getName() + "'s money...");
            double money;
            try {
                money = Double.parseDouble(playerDataMap[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Found a invalid coord in data: " + string + " Skipping...");
                continue;
            }
            vault.set(player.getUniqueId(), money);
        }
        sender.sendMessage(ChatColor.GREEN + "Completed! All players economy data imported!");
    }

    private void out() {
        sender.sendMessage(ChatColor.GRAY + "Reading all players, Please wait...");
        List<String> moneyInformation = new ArrayList<>();
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            UUID uuid = offlinePlayer.getUniqueId();
            double balance = vault.getBalance(uuid);
            if (balance == 0 || !offlinePlayer.hasPlayedBefore()) {
                sender.sendMessage(ChatColor.GRAY + "Skipping " + offlinePlayer.getName() + "'s money...");
            } else {
                sender.sendMessage(ChatColor.GRAY + "Exporting " + offlinePlayer.getName() + "'s money...");
                moneyInformation.add(uuid + ":" + balance);
            }
        }
        sender.sendMessage(ChatColor.GRAY + "Saving all data info money.yml...");
        moneyConfig.set("data", moneyInformation);
        moneyConfig.save();
        sender.sendMessage(ChatColor.GREEN + "Completed! All players economy data saved to money.yml");
        sender.sendMessage(ChatColor.YELLOW + "If you want change economy plugin, you can delete old economy plugin, and install new economy plugin, finally run command /vaulti");
    }
}
