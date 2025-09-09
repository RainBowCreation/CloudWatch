package github.metalshark.cloudwatch.commands;

import github.metalshark.cloudwatch.DrainManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DrainNowCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int grace = 5; // seconds
        String broadcast = "Server is scaling in; kicking players in {seconds}s…";
        String kick = "Channel closed. Please rejoin in a moment.";
        DrainManager.drainAndShutdown(broadcast, kick, grace);
        sender.sendMessage("Drain initiated.");
        return true;
    }
}