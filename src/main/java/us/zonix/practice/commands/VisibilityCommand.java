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

public class VisibilityCommand extends Command
{
    private final Practice plugin;
    
    public VisibilityCommand() {
        super("visibility");
        this.plugin = Practice.getInstance();
        this.setDescription("Toggles visibility.");
        this.setUsage(ChatColor.RED + "Usage: /visibility");
        this.setAliases((List)Arrays.asList("visibility"));
    }
    
    public boolean execute(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player player = (Player)sender;
        if (!player.hasPermission("practice.visibility")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            return true;
        }
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
            return true;
        }
        playerData.getOptions().setVisibility(!playerData.getOptions().isVisibility());
        final PlayerData playerData2;
        final Player player2;
        final boolean playerSeen;
        final boolean pSeen;
        this.plugin.getServer().getOnlinePlayers().forEach(p -> {
            playerSeen = (playerData2.getOptions().isVisibility() && player2.hasPermission("practice.visibility") && Practice.getInstance().getPlayerManager().getPlayerData(player2.getUniqueId()).getPlayerState() == PlayerState.SPAWN);
            pSeen = (playerData2.getOptions().isVisibility() && player2.hasPermission("practice.visibility") && Practice.getInstance().getPlayerManager().getPlayerData(p.getUniqueId()).getPlayerState() == PlayerState.SPAWN);
            if (playerSeen) {
                p.showPlayer(player2);
            }
            else {
                p.hidePlayer(player2);
            }
            if (pSeen) {
                player2.showPlayer(p);
            }
            else {
                player2.hidePlayer(p);
            }
            return;
        });
        player.sendMessage(ChatColor.YELLOW + "You have toggled the visibility.");
        return true;
    }
}
