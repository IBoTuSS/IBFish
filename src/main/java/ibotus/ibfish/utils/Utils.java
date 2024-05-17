package ibotus.ibfish.utils;

import ibotus.ibfish.configurations.IBConfig;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Utils {

    public static ItemStack createBarrel(String key) {
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

    public static void loadItems(Inventory inventory, String barrelName) {
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

    public static void saveInventory(Inventory inventory, String barrelName) {
        YamlConfiguration yaml = new YamlConfiguration();
        for (int i = 0; i <= 26; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                yaml.set(i + ".item", item);
                yaml.set(i + ".chance", 100);
            }
        }

        File directory = new File("plugins/IBFish/drop");
        if (!directory.exists()) {
            boolean result = directory.mkdirs();
            if (!result) {
                System.out.println("Не удалось создать директорию: " + directory.getAbsolutePath());
                return;
            }
        }

        try {
            yaml.save(new File(directory, barrelName + ".yml"));
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении файла: " + e.getMessage());
        }
    }

}
