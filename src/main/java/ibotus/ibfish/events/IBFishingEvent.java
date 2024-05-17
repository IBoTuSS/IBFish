package ibotus.ibfish.events;

import ibotus.ibfish.configurations.IBData;
import ibotus.ibfish.utils.IBEventManager;
import ibotus.ibfish.configurations.IBConfig;
import ibotus.ibfish.utils.IBHexColor;
import ibotus.ibfish.utils.Utils;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class IBFishingEvent implements Listener {
    private final JavaPlugin plugin;
    private final Random random = new Random();

    public IBFishingEvent(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (!IBEventManager.isEventRunning() || event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }

        Player player = event.getPlayer();
        Item caught = (Item) event.getCaught();
        if (caught == null) {
            return;
        }

        FileConfiguration config = IBConfig.getConfig();
        if (!config.getStringList("settings.world").contains(player.getWorld().getName())) {
            return;
        }

        ConfigurationSection fishingConfig = config.getConfigurationSection("fishing");
        if (fishingConfig == null) {
            return;
        }

        for (String key : fishingConfig.getKeys(false)) {
            if (random.nextInt(100) >= fishingConfig.getInt(key + ".chance")) {
                continue;
            }

            ItemStack barrel = Utils.createBarrel(key);
            caught.setItemStack(barrel);

            String soundName = fishingConfig.getString(key + ".sound");
            if (soundName != null) {
                player.playSound(player.getLocation(), Sound.valueOf(soundName), 0.5F, 1.0F);
            }

            String barrelName = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(fishingConfig.getString(key + ".name")));
            String message = IBHexColor.color(Objects.requireNonNull(config.getString("messages.fishing")).replace("{barrel}", barrelName));
            player.sendMessage(message);

            updatePlayerData(player, key);

            List<String> commands = config.getStringList("fishing." + key + ".commands");
            executeCommands(player, commands);

            break;
        }
    }

    private void updatePlayerData(Player player, String key) {
        FileConfiguration data = IBData.getData();
        String playerName = player.getName();
        int currentCount = data.getInt("fishing." + key + "." + playerName, 0);
        data.set("fishing." + key + "." + playerName, currentCount + 1);

        try {
            data.save(new File(plugin.getDataFolder(), "data.yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void executeCommands(Player player, List<String> commands) {
        if (commands == null || commands.isEmpty()) {
            return;
        }

        for (String command : commands) {
            String finalCommand = command.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
        }
    }
}


