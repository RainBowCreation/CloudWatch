package github.metalshark.cloudwatch.listeners;

import com.github.puregero.multilib.MultiLib;
import github.metalshark.cloudwatch.CloudWatch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathListener extends EventCountListener {

    @EventHandler(priority=EventPriority.MONITOR)
    @SuppressWarnings("unused")
    public void onEvent(EntityDeathEvent event) {
        if (CloudWatch.IsMultipaper) {
            if (!MultiLib.isChunkLocal(event.getEntity()))
                return;
        }
        count++;
    }

}
