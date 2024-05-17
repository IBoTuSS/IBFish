package ibotus.ibfish.fish;

import ibotus.ibfish.configurations.IBConfig;
import ibotus.ibfish.utils.IBHexColor;

import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Objects;

public class IBFishPlace implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        ItemStack item = event.getItemInHand();

        if (isUniqueBarrel(item)) {
            Barrel barrel = (Barrel) block.getState();
            transferItemsToPlayer(barrel.getInventory(), player);
            block.setType(Material.AIR, true);
        }
    }

    private boolean isUniqueBarrel(ItemStack item) {
        if (item.getType() != Material.BARREL) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasEnchant(Enchantment.LUCK) || !meta.hasDisplayName()) {
            return false;
        }

        String itemName = meta.getDisplayName();
        FileConfiguration config = IBConfig.getConfig();
        ConfigurationSection fishingConfig = config.getConfigurationSection("fishing");

        if (fishingConfig == null) {
            return false;
        }

        for (String key : fishingConfig.getKeys(false)) {
            String barrelName = IBHexColor.color(config.getString("fishing." + key + ".name"));
            if (Objects.equals(itemName, barrelName)) {
                return true;
            }
        }

        return false;
    }

    private void transferItemsToPlayer(Inventory barrelInventory, Player player) {
        Inventory playerInventory = player.getInventory();

        for (ItemStack itemStack : barrelInventory.getContents()) {
            if (itemStack != null) {
                HashMap<Integer, ItemStack> notAddedItems = playerInventory.addItem(itemStack);

                if (!notAddedItems.isEmpty()) {
                    for (ItemStack notAddedItem : notAddedItems.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), notAddedItem);
                    }
                }
            }
        }

        barrelInventory.clear();
    }
}
