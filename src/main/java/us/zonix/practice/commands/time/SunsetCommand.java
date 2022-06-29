package us.zonix.practice.commands.time;

import us.zonix.practice.player.PlayerData;
import us.zonix.practice.settings.item.ProfileOptionsItemState;
import us.zonix.practice.Practice;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;

public class SunsetCommand extends Command
{
    public SunsetCommand() {
        super("sunset");
        this.setDescription("Set player time to sunset.");
        this.setUsage(ChatColor.RED + "Usage: /sunset");
    }
    
    public boolean execute(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        ((Player)sender).setPlayerTime(12000L, false);
        final PlayerData playerData = Practice.getInstance().getPlayerManager().getPlayerData(((Player)sender).getUniqueId());
        playerData.getOptions().setTime(ProfileOptionsItemState.SUNSET);
        return true;
    }
}
