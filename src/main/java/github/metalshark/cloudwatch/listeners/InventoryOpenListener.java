package github.metalshark.cloudwatch.listeners;

import com.github.puregero.multilib.MultiLib;
import github.metalshark.cloudwatch.CloudWatch;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class InventoryOpenListener extends EventCountListener {

    @EventHandler(priority=EventPriority.MONITOR)
    @SuppressWarnings("unused")
    public void onEvent(InventoryOpenEvent event) {
        if (CloudWatch.IsMultipaper) {
            if (!MultiLib.isLocalPlayer((Player) event.getPlayer()))
                return;
        }
        count++;
    }

}
