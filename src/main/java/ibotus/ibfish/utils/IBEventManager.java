package ibotus.ibfish.utils;

import ibotus.ibfish.IBFish;
import ibotus.ibfish.configurations.IBConfig;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class IBEventManager {
    private final IBFish plugin;
    private final int delay;
    public static BossBar bossBar;
    public static boolean isEventRunning = false;
    private long lastUpdateTime;

    public IBEventManager(IBFish plugin) {
        this.plugin = plugin;
        this.delay = plugin.getConfig().getInt("settings.event-time") * 60 * 20;
        resetUpdateTime();
    }

    public void resetUpdateTime() {
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public static boolean isEventRunning() {
        return isEventRunning;
    }

    public void startEventTimer() {
        new BukkitRunnable(){
            public void run() {
                startEvent();
            }
        }.runTaskTimer(this.plugin, this.delay, this.delay);
    }

    public String getRemainingTime() {
        int timePassed = (int)((System.currentTimeMillis() - this.lastUpdateTime) / 1000L);
        int updateInterval = IBConfig.getConfig().getInt("settings.event-time") * 60;
        int timeLeft = updateInterval - timePassed;
        return formatTime(timeLeft);
    }

    private String formatTime(int timeLeft) {
        int hours = timeLeft / 3600;
        int minutes = (timeLeft % 3600) / 60;
        int seconds = timeLeft % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void startEvent() {
        if (isEventRunning) {
            return;
        }

        isEventRunning = true;

        String message = IBHexColor.color(IBConfig.getConfig().getString("settings.bossbar.message"));
        BarColor color = BarColor.valueOf(IBConfig.getConfig().getString("settings.bossbar.color"));
        BarStyle style = BarStyle.valueOf(IBConfig.getConfig().getString("settings.bossbar.style"));
        bossBar = Bukkit.createBossBar(message, color, style);

        int eventTime = IBConfig.getConfig().getInt("settings.bossbar.time");

        String soundKey = "sound.event-start";
        Sound sound = Sound.valueOf(IBConfig.getConfig().getString(soundKey + ".sound"));
        float volume = (float) IBConfig.getConfig().getDouble(soundKey + ".volume");
        float pitch = (float) IBConfig.getConfig().getDouble(soundKey + ".pitch");
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.playSound(onlinePlayer.getLocation(), sound, volume, pitch);
        }

        new BukkitRunnable() {
            int timeLeft = eventTime;
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    resetUpdateTime();
                    bossBar.removeAll();
                    bossBar = null;
                    isEventRunning = false;
                    this.cancel();
                } else {
                    bossBar.setProgress((double) timeLeft / eventTime);
                    timeLeft--;
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);
    }
}

