package ibotus.ibfish.utils;

import ibotus.ibfish.configurations.IBConfig;
import ibotus.ibfish.configurations.IBData;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class IBFishPlaceholders extends PlaceholderExpansion {

    private final IBEventManager ibEventManager;

    public IBFishPlaceholders(IBEventManager ibeventmanager) {
        this.ibEventManager = ibeventmanager;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return "IBoTuS";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "IBFish";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.1";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        FileConfiguration data = IBData.getData();
        if (data.isSet("fishing." + identifier)) {
            String topPlayer = null;
            int maxCount = 0;
            for (String playerName : Objects.requireNonNull(data.getConfigurationSection("fishing." + identifier)).getKeys(false)) {
                int count = data.getInt("fishing." + identifier + "." + playerName);
                if (count > maxCount) {
                    maxCount = count;
                    topPlayer = playerName;
                }
            }

            return topPlayer != null ? topPlayer : IBConfig.getConfig().getString("settings.replace-placeholder.no-player");
        }

        if ("event".equals(identifier)) {
            if (IBEventManager.isEventRunning) {
                return IBHexColor.color(IBConfig.getConfig().getString("settings.replace-placeholder.event-active"));
            } else {
                return ibEventManager.getRemainingTime();
            }
        }

        return null;
    }
}

