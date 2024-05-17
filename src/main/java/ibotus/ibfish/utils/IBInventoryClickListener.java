package ibotus.ibfish.utils;

import ibotus.ibfish.configurations.IBConfig;
import ibotus.ibfish.fish.IBEditMenu;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class IBInventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String inventoryTitle = event.getView().getTitle();
        int clickedSlot = event.getSlot();

        if (event.getClickedInventory() != null && IBEditMenu.isOpenInventory(event.getClickedInventory(), (Player) event.getWhoClicked())) {
            if (inventoryTitle.startsWith(IBHexColor.color("&8Предметы в бочке: "))) {
                Player player = (Player) event.getWhoClicked();
                if (clickedSlot == 31) {
                    event.setCancelled(true);
                    String saveloot = IBConfig.getConfig().getString("messages.save-loot");
                    playSoundAndSendMessage(player, Sound.BLOCK_NOTE_BLOCK_PLING, Objects.requireNonNull(IBHexColor.color(saveloot)));
                    String barrelName = inventoryTitle.substring(IBHexColor.color("&8Предметы в бочке: ").length());
                    saveInventory(event.getClickedInventory(), barrelName);
                    event.getWhoClicked().closeInventory();
                } else if (clickedSlot >= 27 && clickedSlot <= 35) {
                    event.setCancelled(true);
                    playSoundAndSendMessage(player, Sound.UI_BUTTON_CLICK, "");
                }
            }
        }
    }

    private void playSoundAndSendMessage(Player player, Sound sound, String message) {
        player.playSound(player.getLocation(), sound, 0.5f, 1.0f);
        if (!message.isEmpty()) {
            player.sendMessage(IBHexColor.color(message));
        }
    }

    private void saveInventory(Inventory inventory, String barrelName) {
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
