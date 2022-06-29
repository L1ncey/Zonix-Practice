package us.zonix.practice.commands.duel;

import us.zonix.practice.match.Match;
import us.zonix.practice.events.PracticeEvent;
import us.zonix.practice.party.Party;
import us.zonix.practice.player.PlayerData;
import me.maiko.dexter.profile.Profile;
import java.util.UUID;
import us.zonix.practice.match.MatchTeam;
import us.zonix.practice.events.redrover.RedroverEvent;
import us.zonix.practice.events.parkour.ParkourEvent;
import us.zonix.practice.events.oitc.OITCEvent;
import us.zonix.practice.events.sumo.SumoEvent;
import us.zonix.practice.util.StringUtil;
import us.zonix.practice.player.PlayerState;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import java.util.List;
import java.util.Arrays;
import org.bukkit.ChatColor;
import us.zonix.practice.Practice;
import org.bukkit.command.Command;

public class SpectateCommand extends Command
{
    private final Practice plugin;
    
    public SpectateCommand() {
        super("spectate");
        this.plugin = Practice.getInstance();
        this.setDescription("Spectate a player's match.");
        this.setUsage(ChatColor.RED + "Usage: /spectate <player>");
        this.setAliases((List)Arrays.asList("spec"));
    }
    
    public boolean execute(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player player = (Player)sender;
        if (args.length < 1) {
            player.sendMessage(this.usageMessage);
            return true;
        }
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        final Party party = this.plugin.getPartyManager().getParty(playerData.getUniqueId());
        if (party != null || (playerData.getPlayerState() != PlayerState.SPAWN && playerData.getPlayerState() != PlayerState.SPECTATING)) {
            player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
            return true;
        }
        final Player target = this.plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[0]));
            return true;
        }
        final PlayerData targetData = this.plugin.getPlayerManager().getPlayerData(target.getUniqueId());
        if (targetData.getPlayerState() == PlayerState.EVENT) {
            final PracticeEvent event = this.plugin.getEventManager().getEventPlaying(target);
            if (event == null) {
                player.sendMessage(ChatColor.RED + "That player is currently not in an event.");
                return true;
            }
            if (event instanceof SumoEvent) {
                player.performCommand("eventspectate Sumo");
            }
            else if (event instanceof OITCEvent) {
                player.performCommand("eventspectate OITC");
            }
            else if (event instanceof ParkourEvent) {
                player.performCommand("eventspectate Parkour");
            }
            else if (event instanceof RedroverEvent) {
                player.performCommand("eventspectate Redrover");
            }
            return true;
        }
        else {
            if (targetData.getPlayerState() != PlayerState.FIGHTING) {
                player.sendMessage(ChatColor.RED + "That player is currently not in a fight.");
                return true;
            }
            final Match targetMatch = this.plugin.getMatchManager().getMatch(targetData);
            if (!targetMatch.isParty()) {
                if (!targetData.getOptions().isSpectators() && !player.hasPermission("core.staff")) {
                    player.sendMessage(ChatColor.RED + "That player has ignored spectators.");
                    return true;
                }
                final MatchTeam team = targetMatch.getTeams().get(0);
                final MatchTeam team2 = targetMatch.getTeams().get(1);
                final PlayerData otherPlayerData = this.plugin.getPlayerManager().getPlayerData((team.getPlayers().get(0) == target.getUniqueId()) ? team2.getPlayers().get(0) : team.getPlayers().get(0));
                if (otherPlayerData != null && !otherPlayerData.getOptions().isSpectators() && !player.hasPermission("core.staff")) {
                    player.sendMessage(ChatColor.RED + "That player has ignored spectators.");
                    return true;
                }
            }
            if (playerData.getPlayerState() == PlayerState.SPECTATING) {
                final Match match = this.plugin.getMatchManager().getSpectatingMatch(player.getUniqueId());
                if (match.equals(targetMatch)) {
                    player.sendMessage(ChatColor.RED + "You are already spectating this player.");
                    return true;
                }
                match.removeSpectator(player.getUniqueId());
            }
            final String targetName = Profile.getByUuidIfAvailable(target.getUniqueId()).getRank().getGameColor() + target.getName();
            player.sendMessage(ChatColor.YELLOW + "You are now spectating " + ChatColor.GREEN + targetName + ChatColor.YELLOW + ".");
            this.plugin.getMatchManager().addSpectator(player, playerData, target, targetMatch);
            return true;
        }
    }
}
