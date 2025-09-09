package github.metalshark.cloudwatch;

import com.github.puregero.multilib.MultiLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;

public final class DrainManager {
    private static final AtomicBoolean draining = new AtomicBoolean(false);

    public static boolean isDraining() {
        return draining.get();
    }

    public static void drainAndShutdown(String broadcastMsg, String kickMsg, int graceSeconds) {
        if (!draining.compareAndSet(false, true)) return;

        // Broadcast and start a countdown (optional)
        if (broadcastMsg != null && !broadcastMsg.isEmpty()) {
            TextComponent component = Component.text(broadcastMsg.replace("{seconds}", String.valueOf(graceSeconds)), TextColor.color(Color.RED.asRGB()));
            if (CloudWatch.IsMultipaper) {
                for (Player p: MultiLib.getLocalOnlinePlayers()) {
                    p.sendMessage(component);
                }
            }
            else {
                Bukkit.getScheduler().runTask(CloudWatch.getPlugin(), () ->
                        Bukkit.broadcast(component)
                );
            }
        }

        // After grace period, kick everyone and stop the server
        long delayTicks = Math.max(0, graceSeconds) * 20L;
        Bukkit.getScheduler().runTaskLater(CloudWatch.getPlugin(), () -> {
            // Kick all online players
            TextComponent component = Component.text("Channel restarting, please reconnect.").color(TextColor.color(Color.RED.asRGB()));
            if (kickMsg != null)
                component = Component.text(kickMsg).color(TextColor.color(Color.RED.asRGB()));
            if (CloudWatch.IsMultipaper) {
                for (Player p: MultiLib.getLocalOnlinePlayers()) {
                    try { p.kick(component); } catch (Throwable ignored) {}
                }
            }
            else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    try { p.kick(component); } catch (Throwable ignored) {}
                }
                // Save worlds
                for (World w : Bukkit.getWorlds()) {
                    try {
                        w.save();
                    } catch (Throwable ignored) {
                    }
                }
            }
            Bukkit.shutdown();
        }, delayTicks);
    }
}