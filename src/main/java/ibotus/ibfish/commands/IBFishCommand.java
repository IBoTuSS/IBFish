package ibotus.ibfish.commands;

import ibotus.ibfish.IBFish;
import ibotus.ibfish.configurations.IBData;
import ibotus.ibfish.fish.IBChanceMenu;
import ibotus.ibfish.fish.IBEditMenu;
import ibotus.ibfish.utils.IBEventManager;
import ibotus.ibfish.utils.IBHexColor;
import ibotus.ibfish.configurations.IBConfig;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class IBFishCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final IBEventManager ibEventManager;

    public IBFishCommand(JavaPlugin plugin) {
        this.ibEventManager = new IBEventManager((IBFish) plugin);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length > 0) {
            if (sender instanceof Player player) {
                if ("reload".equalsIgnoreCase(args[0])) {
                    if (!player.hasPermission("ibfish.reload")) {
                        String noPermissionMessage = IBConfig.getConfig().getString("messages.permission");
                        assert noPermissionMessage != null;
                        player.sendMessage(IBHexColor.color(noPermissionMessage));
                        return true;
                    }
                    IBConfig.loadYaml(this.plugin);
                    IBData.loadYaml(this.plugin);
                    String reloadMessage = IBConfig.getConfig().getString("messages.reload");
                    assert reloadMessage != null;
                    player.sendMessage(IBHexColor.color(reloadMessage));
                    return true;
                } else if ("event-start".equalsIgnoreCase(args[0])) {
                    if (!player.hasPermission("ibfish.event-start")) {
                        String noPermissionMessage = IBConfig.getConfig().getString("messages.permission");
                        assert noPermissionMessage != null;
                        player.sendMessage(IBHexColor.color(noPermissionMessage));
                        return true;
                    }
                    if (IBEventManager.isEventRunning()) {
                        sender.sendMessage(Objects.requireNonNull(IBHexColor.color(IBConfig.getConfig().getString("messages.event-running"))));
                        return true;
                    }
                    this.ibEventManager.startEvent();
                    sender.sendMessage(Objects.requireNonNull(IBHexColor.color(IBConfig.getConfig().getString("messages.event-start"))));
                } else if (args.length > 1) {
                    if (!player.hasPermission("ibfish.command")) {
                        String noPermissionMessage = IBConfig.getConfig().getString("messages.permission");
                        assert noPermissionMessage != null;
                        player.sendMessage(IBHexColor.color(noPermissionMessage));
                        return true;
                    }
                    FileConfiguration config = IBConfig.getConfig();
                    ConfigurationSection fishingConfig = config.getConfigurationSection("fishing");
                    if (fishingConfig != null && fishingConfig.contains(args[1])) {
                        if ("editdrop".equalsIgnoreCase(args[0])) {
                            IBEditMenu.open(player, args[1]);
                        } else if ("editchance".equalsIgnoreCase(args[0])) {
                            IBChanceMenu.open(player, args[1]);
                        }
                    } else {
                        String message = IBConfig.getConfig().getString("messages.command");
                        assert message != null;
                        player.sendMessage(IBHexColor.color(message.replace("{name}", args[1])));
                    }
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 1) { return Arrays.asList("reload", "event-start", "editdrop", "editchance"); } else if (args.length == 2) {
            if ("editdrop".equalsIgnoreCase(args[0]) || "editchance".equalsIgnoreCase(args[0])) {
                ConfigurationSection fishingConfig = IBConfig.getConfig().getConfigurationSection("fishing");
                assert fishingConfig != null;
                return new ArrayList<>(fishingConfig.getKeys(false));
            }
        }
        return null;
    }
}


