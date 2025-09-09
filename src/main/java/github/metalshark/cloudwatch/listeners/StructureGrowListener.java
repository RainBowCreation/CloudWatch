package github.metalshark.cloudwatch.listeners;

import com.github.puregero.multilib.MultiLib;
import github.metalshark.cloudwatch.CloudWatch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.StructureGrowEvent;

public class StructureGrowListener extends EventCountListener {

    @EventHandler(priority=EventPriority.MONITOR)
    @SuppressWarnings("unused")
    public void onEvent(StructureGrowEvent event) {
        if (CloudWatch.IsMultipaper) {
            if (!MultiLib.isChunkLocal(event.getLocation()))
                return;
        }
        count++;
    }

}
