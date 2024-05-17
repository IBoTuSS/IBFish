package ibotus.ibfish;

import ibotus.ibfish.commands.IBFishCommand;
import ibotus.ibfish.configurations.IBConfig;
import ibotus.ibfish.configurations.IBData;
import ibotus.ibfish.events.IBPlayerJoinListener;
import ibotus.ibfish.fish.IBChanceMenu;
import ibotus.ibfish.events.IBFishingEvent;
import ibotus.ibfish.fish.IBFishPlace;
import ibotus.ibfish.utils.IBEventManager;
import ibotus.ibfish.utils.IBFishPlaceholders;
import ibotus.ibfish.utils.IBHexColor;
import ibotus.ibfish.utils.IBInventoryClickListener;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class IBFish extends JavaPlugin {

    private void msg(String msg) {
        String prefix = IBHexColor.color("&aIBFish &7| ");
        Bukkit.getConsoleSender().sendMessage(IBHexColor.color(prefix + msg));
    }

    @Override
    public void onEnable() {
        IBConfig.loadYaml(this);
        IBData.loadYaml(this);
        Bukkit.getConsoleSender().sendMessage("");
        this.msg("&fDeveloper: &aIBoTuS");
        this.msg("&fVersion: &dv" + this.getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage("");
        IBFishCommand ibFishCommand = new IBFishCommand(this);
        IBEventManager ibeventmanager = new IBEventManager(this);
        Objects.requireNonNull(this.getCommand("IBFish")).setExecutor(ibFishCommand);
        Objects.requireNonNull(this.getCommand("IBFish")).setTabCompleter(ibFishCommand);
        this.getServer().getPluginManager().registerEvents(new IBFishingEvent(this), this);
        this.getServer().getPluginManager().registerEvents(new IBChanceMenu(), this);
        this.getServer().getPluginManager().registerEvents(new IBInventoryClickListener(), this);
        this.getServer().getPluginManager().registerEvents(new IBFishPlace(),this);
        this.getServer().getPluginManager().registerEvents(new IBPlayerJoinListener(),this);
        ibeventmanager.startEventTimer();
        new IBFishPlaceholders(ibeventmanager).register();
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("");
        this.msg("&fPlugin disable");
        Bukkit.getConsoleSender().sendMessage("");
    }

}

