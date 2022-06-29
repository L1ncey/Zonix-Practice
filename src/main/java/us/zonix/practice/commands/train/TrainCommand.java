package us.zonix.practice.commands.train;

import us.zonix.practice.player.PlayerData;
import us.zonix.practice.player.PlayerState;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import us.zonix.practice.Practice;
import org.bukkit.command.Command;

public class TrainCommand extends Command
{
    private final Practice plugin;
    
    public TrainCommand() {
        super("train");
        this.plugin = Practice.getInstance();
        this.setDescription("Train with a bot.");
        this.setUsage(ChatColor.RED + "Usage: /train");
    }
    
    public boolean execute(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player player = (Player)sender;
        if (this.plugin.getTournamentManager().getTournament(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "You are currently in a tournament.");
            return true;
        }
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
            return true;
        }
        player.openInventory(this.plugin.getInventoryManager().getTrainInventory().getCurrentPage());
        return true;
    }
}
