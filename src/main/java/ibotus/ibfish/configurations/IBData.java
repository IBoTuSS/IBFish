package ibotus.ibfish.configurations;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class IBData {
    private static FileConfiguration data;

    public static void loadYaml(Plugin plugin) {
        File file = new File(plugin.getDataFolder(), "data.yml");
        if (!file.exists()) {
            plugin.saveResource("data.yml", true);
        }

        data = YamlConfiguration.loadConfiguration(file);

        FileConfiguration config = IBConfig.getConfig();
        ConfigurationSection fishingConfig = config.getConfigurationSection("fishing");
        if (fishingConfig != null) {
            for (String key : fishingConfig.getKeys(false)) {
                if (!data.isSet("fishing." + key)) {
                    data.createSection("fishing." + key);
                }
            }
        }

        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static FileConfiguration getData() {
        return data;
    }
}

