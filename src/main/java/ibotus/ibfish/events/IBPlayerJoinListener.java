package ibotus.ibfish.events;

import ibotus.ibfish.utils.IBEventManager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class IBPlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (IBEventManager.isEventRunning() && IBEventManager.bossBar != null) {
            IBEventManager.bossBar.addPlayer(player);
        }
    }
}
