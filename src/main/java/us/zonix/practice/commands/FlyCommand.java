package us.zonix.practice.commands;

import us.zonix.practice.player.PlayerData;
import us.zonix.practice.player.PlayerState;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import java.util.List;
import java.util.Arrays;
import org.bukkit.ChatColor;
import us.zonix.practice.Practice;
import org.bukkit.command.Command;

public class FlyCommand extends Command
{
    private final Practice plugin;
    
    public FlyCommand() {
        super("fly");
        this.plugin = Practice.getInstance();
        this.setDescription("Toggles flight.");
        this.setUsage(ChatColor.RED + "Usage: /fly");
        this.setAliases((List)Arrays.asList("flight"));
    }
    
    public boolean execute(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player player = (Player)sender;
        if (!player.hasPermission("practice.fly")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            return true;
        }
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
            return true;
        }
        player.setAllowFlight(!player.getAllowFlight());
        if (player.getAllowFlight()) {
            player.sendMessage(ChatColor.YELLOW + "Your flight has been enabled.");
        }
        else {
            player.sendMessage(ChatColor.YELLOW + "Your flight has been disabled.");
        }
        return true;
    }
}
