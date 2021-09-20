package me.hsgamer.vaultio;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VaultIO extends JavaPlugin {
    private CommandSender sender;
    private VaultProvider vault;
    private File moneyFile;
    private YamlConfiguration moneyYaml;

    @Override
    public void onEnable() {
        moneyFile = new File(getDataFolder(), "money.yml");
        if (!moneyFile.exists()) {
            getLogger().info("Creating money.yml");
            saveResource("money.yml", true);
        }
        moneyYaml = YamlConfiguration.loadConfiguration(moneyFile);
        moneyYaml.options().copyDefaults(true);
        YamlConfiguration defaultYAML = YamlConfiguration.loadConfiguration(new InputStreamReader(getResource("money.yml")));
        moneyYaml.setDefaults(defaultYAML);
        this.vault = new VaultProvider();
        if (!this.vault.isValid()) {
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
        this.sender.sendMessage(ChatColor.GRAY + "Reading money.yml, Please wait...");
        moneyYaml = YamlConfiguration.loadConfiguration(moneyFile);
        List<String> moneyInformation = moneyYaml.getStringList("data");
        for (String string : moneyInformation) {
            String[] playerDataMap = string.split(":");
            if (playerDataMap.length != 2) {
                this.sender.sendMessage(ChatColor.RED + "Found a invalid coord in data: " + string + " Skipping...");
                continue;
            }
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(playerDataMap[0]));
            this.sender.sendMessage(ChatColor.GRAY + "Importing " + player.getName() + "'s money...");
            double money;
            try {
                money = Double.parseDouble(playerDataMap[1]);
            } catch (NumberFormatException e) {
                this.sender.sendMessage(ChatColor.RED + "Found a invalid coord in data: " + string + " Skipping...");
                continue;
            }
            this.vault.set(player.getUniqueId(), money);
        }
        this.sender.sendMessage(ChatColor.GREEN + "Completed! All players economy data imported!");
    }

    private void out() {
        this.sender.sendMessage(ChatColor.GRAY + "Reading all players, Please wait...");
        List<String> moneyInformation = new ArrayList<>();
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            UUID uuid = offlinePlayer.getUniqueId();
            double balance = this.vault.getBalance(uuid);
            if (balance == 0 || !offlinePlayer.hasPlayedBefore()) {
                this.sender.sendMessage(ChatColor.GRAY + "Skipping " + offlinePlayer.getName() + "'s money...");
            } else {
                this.sender.sendMessage(ChatColor.GRAY + "Exporting " + offlinePlayer.getName() + "'s money...");
                moneyInformation.add(uuid + ":" + balance);
            }
        }
        this.sender.sendMessage(ChatColor.GRAY + "Saving all data info money.yml...");
        moneyYaml.set("data", moneyInformation);
        try {
            moneyYaml.save(moneyFile);
            this.sender.sendMessage(ChatColor.GREEN + "Completed! All players economy data saved to money.yml");
            this.sender.sendMessage(ChatColor.YELLOW + "If you want change economy plugin, you can delete old economy plugin, and install new economy plugin, finally run command /vaulti");
        } catch (IOException e) {
            e.printStackTrace();
            this.sender.sendMessage(ChatColor.RED + "Failed save data, please check the console.");
        }
    }
}
