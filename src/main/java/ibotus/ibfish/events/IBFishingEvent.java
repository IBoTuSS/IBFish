package ibotus.ibfish.events;

import ibotus.ibfish.configurations.IBData;
import ibotus.ibfish.utils.IBEventManager;
import ibotus.ibfish.configurations.IBConfig;
import ibotus.ibfish.utils.IBHexColor;

import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class IBFishingEvent implements Listener {
    private final JavaPlugin plugin;

    public IBFishingEvent(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (!IBEventManager.isEventRunning()) {
            return;
        }
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Player player = event.getPlayer();
            Item caught = (Item) event.getCaught();
            FileConfiguration config = IBConfig.getConfig();
            Random random = new Random();

            World playerWorld = player.getWorld();
            List<String> worldNames = config.getStringList("settings.world");

            if (worldNames.contains(playerWorld.getName())) {
                ConfigurationSection fishingConfig = config.getConfigurationSection("fishing");
                if (fishingConfig == null) {
                    return;
                }
                for (String key : fishingConfig.getKeys(false)) {
                    if (random.nextInt(100) < fishingConfig.getInt(key + ".chance")) {
                        if (caught == null) {
                            return;
                        }
                        ItemStack barrel = createBarrel(key);
                        caught.setItemStack(barrel);
                        player.playSound(player.getLocation(), Sound.valueOf(fishingConfig.getString(key + ".sound")), 0.5F, 1.0F);

                        String barrelName = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(fishingConfig.getString(key + ".name")));
                        String message = IBHexColor.color(Objects.requireNonNull(IBConfig.getConfig().getString("messages.fishing")).replace("{barrel}", barrelName));
                        player.sendMessage(message);

                        FileConfiguration data = IBData.getData();
                        String playerName = player.getName();
                        int currentCount = data.getInt("fishing." + key + "." + playerName, 0);
                        data.set("fishing." + key + "." + playerName, currentCount + 1);
                        try {
                            data.save(new File(plugin.getDataFolder(), "data.yml"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (config.isSet("fishing." + key + ".commands")) {
                            List<String> commands = config.getStringList("fishing." + key + ".commands");
                            if (!commands.isEmpty()) {
                                for (String command : commands) {
                                    command = command.replace("%player%", player.getName());
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    private ItemStack createBarrel(String key) {
        FileConfiguration config = IBConfig.getConfig();
        ItemStack barrel = new ItemStack(Material.BARREL);
        ItemMeta meta = barrel.getItemMeta();
        if (meta instanceof BlockStateMeta blockStateMeta) {
            BlockState state = blockStateMeta.getBlockState();

            if (state instanceof Container container) {
                loadItems(container.getInventory(), key);
                blockStateMeta.setBlockState(state);
            }

            blockStateMeta.setDisplayName(IBHexColor.color(config.getString("fishing." + key + ".name")));

            if (config.getBoolean("fishing." + key + ".glow")) {
                blockStateMeta.addEnchant(Enchantment.LUCK, 1, true);
                blockStateMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            barrel.setItemMeta(blockStateMeta);
        }
        return barrel;
    }



    private void loadItems(Inventory inventory, String barrelName) {
        File file = new File("plugins/IBFish/drop", barrelName + ".yml");
        if (file.exists()) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            for (String key : yaml.getKeys(false)) {
                ItemStack item = yaml.getItemStack(key + ".item");
                int chance = yaml.getInt(key + ".chance");
                Random random = new Random();
                if (random.nextInt(100) < chance) {
                    int slot = Integer.parseInt(key);
                    inventory.setItem(slot, item);
                }
            }
        }
    }
}


