package ibotus.ibfish.fish;

import ibotus.ibfish.configurations.IBConfig;
import ibotus.ibfish.utils.IBHexColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IBChanceMenu implements Listener {

    public IBChanceMenu() {
    }

    public static void open(Player player, String barrelName) {
        FileConfiguration config = IBConfig.getConfig();
        String titleTemplate = config.getString("inventory.inventory-chance.title");
        String title = IBHexColor.color(titleTemplate + barrelName);
        Inventory inventory = Bukkit.createInventory(null, 36, title);

        loadInventory(inventory, barrelName);

        ItemStack grayGlass = createGrayGlassPane();

        int[] graySlots = {27, 28, 29, 30, 31, 32, 33, 34, 35};
        for (int slot : graySlots) {
            inventory.setItem(slot, grayGlass);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_BARREL_OPEN, 0.5f, 1.0f);
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        FileConfiguration config = IBConfig.getConfig();
        String titleTemplate = config.getString("inventory.inventory-chance.title");
        String inventoryTitle = event.getView().getTitle();

        if (inventoryTitle.startsWith(Objects.requireNonNull(IBHexColor.color(titleTemplate)))) {
            event.setCancelled(true);

            int slot = event.getRawSlot();
            ItemStack item = event.getCurrentItem();

            if (item != null && item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).hasLore()) {
                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta.getLore();

                if (lore != null && !lore.isEmpty()) {
                    String chanceLine = lore.get(0);
                    int chance = Integer.parseInt(chanceLine.split(": ")[1].replace("%", ""));

                    if (event.isShiftClick()) {
                        chance = adjustChanceShiftClick(event, chance);
                    } else {
                        chance = adjustChanceClick(event, chance);
                    }

                    lore.set(0, IBHexColor.color("&aШанс: " + chance + "%"));
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    event.getInventory().setItem(slot, item);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        FileConfiguration config = IBConfig.getConfig();
        String titleTemplate = config.getString("inventory.inventory-chance.title");
        String inventoryTitle = event.getView().getTitle();

        if (inventoryTitle.startsWith(Objects.requireNonNull(IBHexColor.color(titleTemplate)))) {
            String barrelName = inventoryTitle.replace(IBHexColor.color(titleTemplate), "");
            File file = new File("plugins/IBFish/drop", barrelName + ".yml");

            if (file.exists()) {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                saveInventory(event, yaml);
                try {
                    yaml.save(file);
                    Player player = (Player) event.getPlayer();
                    String message = config.getString("messages.chance-loot");
                    player.sendMessage(IBHexColor.color(Objects.requireNonNull(message)));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f);
                } catch (IOException e) {
                    throw new RuntimeException("Error saving file: " + e.getMessage());
                }
            }
        }
    }

    private static void loadInventory(Inventory inventory, String barrelName) {
        File file = new File("plugins/IBFish/drop", barrelName + ".yml");

        if (file.exists()) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

            for (String key : yaml.getKeys(false)) {
                ItemStack item = yaml.getItemStack(key + ".item");
                if (item != null) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta == null) {
                        throw new RuntimeException("ItemMeta is null");
                    }
                    List<String> lore = new ArrayList<>();
                    lore.add(IBHexColor.color("&aШанс: " + yaml.getInt(key + ".chance") + "%"));
                    lore.add(IBHexColor.color("&f"));
                    lore.add(IBHexColor.color("&fЛевый клик: &c-1%"));
                    lore.add(IBHexColor.color("&fПравый клик: &a+1%"));
                    lore.add(IBHexColor.color("&fShift + Левый клик: &c-10%"));
                    lore.add(IBHexColor.color("&fShift + Правый клик: &a+10%"));
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    inventory.setItem(Integer.parseInt(key), item);
                }
            }
        }
    }

    private static ItemStack createGrayGlassPane() {
        ItemStack itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            throw new RuntimeException("ItemMeta is null");
        }
        meta.setDisplayName(IBHexColor.color("&7"));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private static void saveInventory(InventoryCloseEvent event, YamlConfiguration yaml) {
        for (int slot = 0; slot < event.getInventory().getSize(); slot++) {
            ItemStack item = event.getInventory().getItem(slot);
            if (item != null && item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).hasLore()) {
                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta.getLore();

                if (lore != null && !lore.isEmpty()) {
                    String chanceLine = lore.get(0);
                    int chance = Integer.parseInt(chanceLine.split(": ")[1].replace("%", ""));
                    yaml.set(slot + ".chance", chance);
                }
            }
        }
    }

    private static int adjustChanceClick(InventoryClickEvent event, int chance) {
        if (event.isLeftClick() && chance > 0) {
            chance--;
        } else if (event.isRightClick() && chance < 100) {
            chance++;
        }
        return chance;
    }

    private static int adjustChanceShiftClick(InventoryClickEvent event, int chance) {
        if (event.isLeftClick() && chance >= 10) {
            chance -= 10;
        } else if (event.isRightClick() && chance <= 90) {
            chance += 10;
        }
        return chance;
    }
}