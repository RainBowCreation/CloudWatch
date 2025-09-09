package github.metalshark.cloudwatch.listeners;

import com.github.puregero.multilib.MultiLib;
import github.metalshark.cloudwatch.CloudWatch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItemListener extends EventCountListener {

    @EventHandler(priority=EventPriority.MONITOR)
    @SuppressWarnings("unused")
    public void onEvent(PlayerDropItemEvent event) {
        if (CloudWatch.IsMultipaper) {
            if (!MultiLib.isLocalPlayer(event.getPlayer()))
                return;
        }
        count++;
    }

}
