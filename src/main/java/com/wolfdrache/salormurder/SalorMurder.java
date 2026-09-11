package com.wolfdrache.salormurder;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import com.wolfdrache.murderknifes.api.MurderKnfesAPI;

public class SalorMurder extends JavaPlugin {

    private Economy economy;
    private MurderKnfesAPI murderKnfes;

    @Override
    public void onEnable(){
        if (!setupEconomy()) {
            getLogger().severe("Vault not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupMurderKnfes()) {
            getLogger().severe("MurderKnifes API not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        getLogger().info("SalorMurder has been enabled!");
    }
    
    @Override
    public void onDisable(){
        getLogger().info("SalorMurder has been disabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return true;
    }

    private boolean setupMurderKnfes() {
        RegisteredServiceProvider<MurderKnfesAPI> provider = getServer().getServicesManager().getRegistration(MurderKnfesAPI.class);

        if (provider == null) {
            getLogger().severe("MurderKnifes API not found!");
            return false;
        }

        murderKnfes = provider.getProvider();
        return true;
    }
}
