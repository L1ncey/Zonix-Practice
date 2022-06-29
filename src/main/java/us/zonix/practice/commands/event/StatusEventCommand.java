package us.zonix.practice.commands.event;

import us.zonix.practice.match.Match;
import java.util.Iterator;
import us.zonix.practice.player.PlayerData;
import us.zonix.practice.util.Clickable;
import us.zonix.practice.match.MatchTeam;
import java.util.UUID;
import us.zonix.practice.tournament.Tournament;
import us.zonix.practice.player.PlayerState;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import us.zonix.practice.Practice;
import org.bukkit.command.Command;

public class StatusEventCommand extends Command
{
    private final Practice plugin;
    
    public StatusEventCommand() {
        super("eventstatus");
        this.plugin = Practice.getInstance();
        this.setDescription("Show an event or tournament status.");
        this.setUsage(ChatColor.RED + "Usage: /status");
    }
    
    public boolean execute(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player player = (Player)sender;
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
            return true;
        }
        if (this.plugin.getTournamentManager().getTournaments().size() == 0) {
            player.sendMessage(ChatColor.RED + "There is no available tournaments.");
            return true;
        }
        for (final Tournament tournament : this.plugin.getTournamentManager().getTournaments().values()) {
            if (tournament == null) {
                player.sendMessage(ChatColor.RED + "This tournament doesn't exist.");
                return true;
            }
            player.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------");
            player.sendMessage(" ");
            player.sendMessage(ChatColor.RED.toString() + "Tournament [" + tournament.getTeamSize() + "v" + tournament.getTeamSize() + "] " + ChatColor.WHITE.toString() + tournament.getKitName());
            if (tournament.getMatches().size() == 0) {
                player.sendMessage(ChatColor.RED + "There is no available matches.");
                player.sendMessage(" ");
                player.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------");
                return true;
            }
            for (final UUID matchUUID : tournament.getMatches()) {
                final Match match = this.plugin.getMatchManager().getMatchFromUUID(matchUUID);
                final MatchTeam teamA = match.getTeams().get(0);
                final MatchTeam teamB = match.getTeams().get(1);
                final String teamANames = (tournament.getTeamSize() > 1) ? (teamA.getLeaderName() + "'s Party") : teamA.getLeaderName();
                final String teamBNames = (tournament.getTeamSize() > 1) ? (teamB.getLeaderName() + "'s Party") : teamB.getLeaderName();
                final Clickable clickable = new Clickable(ChatColor.WHITE.toString() + ChatColor.BOLD + "* " + ChatColor.GOLD.toString() + teamANames + " vs " + teamBNames + ChatColor.DARK_GRAY + " \u2503 " + ChatColor.GRAY + "[Click to Spectate]", ChatColor.GRAY + "Click to spectate", "/spectate " + teamA.getLeaderName());
                clickable.sendToPlayer(player);
            }
            player.sendMessage(" ");
            player.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------");
        }
        return true;
    }
}
