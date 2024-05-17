package ibotus.ibfish.fish;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ibotus.ibfish.utils.IBHexColor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IBEditMenu {
    private static final Map<UUID, Inventory> openInventories = new HashMap<>();

    public static void open(Player player, String barrelName) {
        Inventory inventory = Bukkit.createInventory(null, 36, IBHexColor.color("&8Предметы в бочке: " + barrelName));

        loadInventory(inventory, barrelName);

        ItemStack grayGlass = createItemStack(Material.GRAY_STAINED_GLASS_PANE, "&7");
        ItemStack limeGlass = createItemStack(Material.LIME_STAINED_GLASS_PANE, "&aСохранить");

        int[] graySlots = {27, 28, 29, 30, 32, 33, 34, 35};
        for (int slot : graySlots) {
            inventory.setItem(slot, grayGlass);
        }

        inventory.setItem(31, limeGlass);

        player.playSound(player.getLocation(), Sound.BLOCK_BARREL_OPEN, 0.5f, 1.0f);
        openInventories.put(player.getUniqueId(), inventory);
        player.openInventory(inventory);
    }

    public static boolean isOpenInventory(Inventory inventory, Player player) {
        Inventory openInventory = openInventories.get(player.getUniqueId());
        return openInventory != null && openInventory.equals(inventory);
    }

    private static void loadInventory(Inventory inventory, String barrelName) {
        File file = new File("plugins/IBFish/drop", barrelName + ".yml");
        if (file.exists()) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            for (String key : yaml.getKeys(false)) {
                ItemStack item = yaml.getItemStack(key + ".item");
                if (item != null) {
                    inventory.setItem(Integer.parseInt(key), item);
                }
            }
        }
    }

    private static ItemStack createItemStack(Material material, String displayName) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            throw new RuntimeException("ItemMeta is null");
        }
        meta.setDisplayName(IBHexColor.color(displayName));
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}


