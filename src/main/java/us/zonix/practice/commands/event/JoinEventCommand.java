package us.zonix.practice.commands.event;

import us.zonix.practice.party.Party;
import us.zonix.practice.tournament.Tournament;
import us.zonix.practice.events.PracticeEvent;
import us.zonix.practice.player.PlayerData;
import us.zonix.practice.tournament.TournamentState;
import us.zonix.practice.events.EventState;
import org.apache.commons.lang.math.NumberUtils;
import us.zonix.practice.player.PlayerState;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import us.zonix.practice.Practice;
import org.bukkit.command.Command;

public class JoinEventCommand extends Command
{
    private final Practice plugin;
    
    public JoinEventCommand() {
        super("join");
        this.plugin = Practice.getInstance();
        this.setDescription("Join an event or tournament.");
        this.setUsage(ChatColor.RED + "Usage: /join <id>");
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
        if (playerData.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
            return true;
        }
        final boolean inTournament = this.plugin.getTournamentManager().isInTournament(player.getUniqueId());
        final boolean inEvent = this.plugin.getEventManager().getEventPlaying(player) != null;
        final String eventId = args[0].toLowerCase();
        if (!NumberUtils.isNumber(eventId)) {
            final PracticeEvent event = this.plugin.getEventManager().getByName(eventId);
            if (inTournament) {
                player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
                return true;
            }
            if (event == null) {
                player.sendMessage(ChatColor.RED + "That event doesn't exist.");
                return true;
            }
            if (event.getState() != EventState.WAITING) {
                player.sendMessage(ChatColor.RED + "That event is currently not available.");
                return true;
            }
            if (event.getPlayers().containsKey(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You are already in this event.");
                return true;
            }
            if (event.getPlayers().size() >= event.getLimit() && !player.hasPermission("practice.tournament.full")) {
                player.sendMessage(ChatColor.RED + "Sorry! The event is already full.");
            }
            event.join(player);
            return true;
        }
        else {
            if (inEvent) {
                player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
                return true;
            }
            if (this.plugin.getTournamentManager().isInTournament(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You are currently in a tournament.");
                return true;
            }
            final int id = Integer.parseInt(eventId);
            final Tournament tournament = this.plugin.getTournamentManager().getTournament(Integer.valueOf(id));
            if (tournament != null) {
                if (tournament.getTeamSize() > 1) {
                    final Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
                    if (party == null) {
                        player.sendMessage(ChatColor.RED + "The party size must be of " + tournament.getTeamSize() + " players.");
                        return true;
                    }
                    if (party.getMembers().size() != tournament.getTeamSize()) {
                        player.sendMessage(ChatColor.RED + "The party size must be of " + tournament.getTeamSize() + " players.");
                        return true;
                    }
                    if (tournament.getKitName().equalsIgnoreCase("HCTeams")) {
                        if (party.getArchers().isEmpty() || party.getBards().isEmpty()) {
                            player.sendMessage(ChatColor.RED + "You must specify your party's roles.\nUse: /party hcteams");
                            player.closeInventory();
                        }
                        return true;
                    }
                }
                if (tournament.getSize() > tournament.getPlayers().size()) {
                    if ((tournament.getTournamentState() == TournamentState.WAITING || tournament.getTournamentState() == TournamentState.STARTING) && tournament.getCurrentRound() == 1) {
                        this.plugin.getTournamentManager().joinTournament(id, player);
                    }
                    else {
                        player.sendMessage(ChatColor.RED + "Sorry! The tournament already started.");
                    }
                }
                else {
                    player.sendMessage(ChatColor.RED + "Sorry! The tournament is already full.");
                }
            }
            else {
                player.sendMessage(ChatColor.RED + "That tournament doesn't exist.");
            }
            return true;
        }
    }
}
