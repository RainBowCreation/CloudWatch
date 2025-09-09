package github.metalshark.cloudwatch.listeners;

import com.github.puregero.multilib.MultiLib;
import github.metalshark.cloudwatch.CloudWatch;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private double maxOnlinePlayers = 0;

    @EventHandler(priority=EventPriority.MONITOR)
    @SuppressWarnings("unused")
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (CloudWatch.IsMultipaper) {
            if (!MultiLib.isLocalPlayer(event.getPlayer()))
                return;
        }
        final double onlinePlayers = Bukkit.getOnlinePlayers().size();
        if (onlinePlayers > maxOnlinePlayers) maxOnlinePlayers = onlinePlayers;
    }

    public double getMaxOnlinePlayersAndReset() {
        final double prevMaxOnlinePlayers = maxOnlinePlayers;
        maxOnlinePlayers = 0;
        return prevMaxOnlinePlayers;
    }

}
