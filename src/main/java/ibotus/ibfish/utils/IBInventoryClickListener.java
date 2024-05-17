package ibotus.ibfish.utils;

import ibotus.ibfish.configurations.IBConfig;
import ibotus.ibfish.fish.IBEditMenu;

import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;

public class IBInventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String inventoryTitle = event.getView().getTitle();
        int clickedSlot = event.getSlot();

        if (event.getClickedInventory() != null && IBEditMenu.isOpenInventory(event.getClickedInventory(), (Player) event.getWhoClicked())) {
            FileConfiguration config = IBConfig.getConfig();
            String editTitleTemplate = config.getString("inventory.inventory-edit.title");
            String expectedTitlePrefix = IBHexColor.color(editTitleTemplate);

            assert expectedTitlePrefix != null;
            if (inventoryTitle.startsWith(expectedTitlePrefix)) {
                Player player = (Player) event.getWhoClicked();
                if (clickedSlot == 31) {
                    event.setCancelled(true);
                    String saveloot = config.getString("messages.save-loot");
                    playSoundAndSendMessage(player, Sound.BLOCK_NOTE_BLOCK_PLING, Objects.requireNonNull(IBHexColor.color(saveloot)));
                    String barrelName = inventoryTitle.substring(expectedTitlePrefix.length());
                    Utils.saveInventory(event.getClickedInventory(), barrelName);
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
}