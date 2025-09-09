package github.metalshark.cloudwatch.listeners;

import com.github.puregero.multilib.MultiLib;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.TradeSelectEvent;

public class TradeSelectListener extends EventCountListener {

    @EventHandler(priority=EventPriority.MONITOR)
    @SuppressWarnings("unused")
    public void onEvent(TradeSelectEvent event) {
        if (MultiLib.isLocalPlayer((Player) event.getWhoClicked()))
            return;
        count++;
    }

}
