package us.zonix.practice.commands;

import java.util.Iterator;
import us.zonix.practice.player.PlayerData;
import us.zonix.practice.util.StringUtil;
import us.zonix.practice.kit.Kit;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import java.util.List;
import java.util.Arrays;
import us.zonix.practice.Practice;
import org.bukkit.command.Command;

public class StatsCommand extends Command
{
    private final Practice plugin;
    
    public StatsCommand() {
        super("stats");
        this.plugin = Practice.getInstance();
        this.setAliases((List)Arrays.asList("elo", "statistics"));
        this.setUsage(ChatColor.RED + "Usage: /stats [player]");
    }
    
    public boolean execute(final CommandSender sender, final String s, final String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(((Player)sender).getUniqueId());
                sender.sendMessage(ChatColor.DARK_RED.toString() + ChatColor.BOLD + sender.getName() + "'s Statistics");
                sender.sendMessage(ChatColor.RED + "Global" + ChatColor.GRAY + ": " + ChatColor.YELLOW + playerData.getGlobalStats("ELO") + " ELO " + ChatColor.GRAY + "\u2503 " + ChatColor.GREEN + playerData.getGlobalStats("WINS") + " Wins " + ChatColor.GRAY + "\u2503 " + ChatColor.GOLD + playerData.getGlobalStats("LOSSES") + " Losses");
                for (final Kit kit : this.plugin.getKitManager().getKits()) {
                    sender.sendMessage(ChatColor.RED + kit.getName() + ChatColor.GRAY + ": " + ChatColor.YELLOW + playerData.getElo(kit.getName()) + " ELO " + ChatColor.GRAY + "\u2503 " + ChatColor.GREEN + playerData.getWins(kit.getName()) + " Wins " + ChatColor.GRAY + "\u2503 " + ChatColor.GOLD + playerData.getLosses(kit.getName()) + " Losses");
                }
            }
            sender.sendMessage(ChatColor.RED + "Check out other players using /stats [player]");
            return true;
        }
        final Player target = this.plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[0]));
            return true;
        }
        final PlayerData playerData2 = this.plugin.getPlayerManager().getPlayerData(target.getUniqueId());
        sender.sendMessage(ChatColor.DARK_RED.toString() + ChatColor.BOLD + target.getName() + "'s Statistics");
        sender.sendMessage(ChatColor.RED + "Global" + ChatColor.GRAY + ": " + ChatColor.YELLOW + playerData2.getGlobalStats("ELO") + " ELO " + ChatColor.GRAY + "\u2503 " + ChatColor.GREEN + playerData2.getGlobalStats("WINS") + " Wins " + ChatColor.GRAY + "\u2503 " + ChatColor.GOLD + playerData2.getGlobalStats("LOSSES") + " Losses");
        for (final Kit kit2 : this.plugin.getKitManager().getKits()) {
            sender.sendMessage(ChatColor.RED + kit2.getName() + ChatColor.GRAY + ": " + ChatColor.YELLOW + playerData2.getElo(kit2.getName()) + " ELO " + ChatColor.GRAY + "\u2503 " + ChatColor.GREEN + playerData2.getWins(kit2.getName()) + " Wins " + ChatColor.GRAY + "\u2503 " + ChatColor.GOLD + playerData2.getLosses(kit2.getName()) + " Losses");
        }
        return true;
    }
}
