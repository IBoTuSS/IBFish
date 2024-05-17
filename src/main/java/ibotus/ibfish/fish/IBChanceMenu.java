package ibotus.ibfish.fish;

import ibotus.ibfish.configurations.IBConfig;
import ibotus.ibfish.utils.IBHexColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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
    private static final String ITEM_CHANCES = "&8Шанс предметов: ";

    public static void open(Player player, String barrelName) {
        Inventory inventory = Bukkit.createInventory(null, 36, IBHexColor.color(ITEM_CHANCES + barrelName));

        loadInventory(inventory, barrelName);

        ItemStack grayGlass = createItemStack();

        int[] graySlots = {27, 28, 29, 30, 31, 32, 33, 34, 35};
        for (int slot : graySlots) {
            inventory.setItem(slot, grayGlass);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_BARREL_OPEN, 0.5f, 1.0f);
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String inventoryTitle = event.getView().getTitle();
        if (inventoryTitle.startsWith(IBHexColor.color(ITEM_CHANCES))) {
            event.setCancelled(true);

            int slot = event.getRawSlot();

            ItemStack item = event.getCurrentItem();
            if (item != null) {
                ItemMeta meta = item.getItemMeta();
                if (meta == null) {
                    throw new RuntimeException("ItemMeta is null");
                }
                List<String> lore;
                if (meta.hasLore()) {
                    lore = meta.getLore();
                    if (lore != null && !lore.isEmpty()) {

                        String chanceLine = lore.get(0);
                        int chance = Integer.parseInt(chanceLine.split(": ")[1].replace("%", ""));

                        if (event.isShiftClick()) {
                            if (event.isLeftClick() && chance >= 10) {
                                chance -= 10;
                            } else if (event.isRightClick() && chance <= 90) {
                                chance += 10;
                            }
                        } else {
                            if (event.isLeftClick() && chance > 0) {
                                chance--;
                            } else if (event.isRightClick() && chance < 100) {
                                chance++;
                            }
                        }

                        lore.set(0, IBHexColor.color("&aШанс: " + chance + "%"));
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                        event.getInventory().setItem(slot, item);
                    }
                }
            }
        }
    }

    private static ItemStack createItemStack() {
        ItemStack itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            throw new RuntimeException("ItemMeta is null");
        }
        meta.setDisplayName(IBHexColor.color("&7"));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String inventoryTitle = event.getView().getTitle();
        if (inventoryTitle.startsWith(IBHexColor.color(ITEM_CHANCES))) {
            String barrelName = inventoryTitle.replace(IBHexColor.color(ITEM_CHANCES), "");
            File file = new File("plugins/IBFish/drop", barrelName + ".yml");
            if (file.exists()) {
                YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                for (int slot = 0; slot < event.getInventory().getSize(); slot++) {
                    ItemStack item = event.getInventory().getItem(slot);
                    if (item != null) {
                        ItemMeta meta = item.getItemMeta();
                        if (meta == null) {
                            throw new RuntimeException("ItemMeta is null");
                        }
                        List<String> lore = meta.getLore();
                        if (lore != null && !lore.isEmpty()) {
                            String chanceLine = lore.get(0);
                            int chance = Integer.parseInt(chanceLine.split(": ")[1].replace("%", ""));
                            yaml.set(slot + ".chance", chance);
                        }
                    }
                }
                try {
                    Player player = (Player) event.getPlayer();
                    String chanceloot = IBConfig.getConfig().getString("messages.chance-loot");
                    player.sendMessage(Objects.requireNonNull(IBHexColor.color(chanceloot)));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f);
                    yaml.save(file);
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
}