package us.zonix.practice.commands.event;

import us.zonix.practice.events.PracticeEvent;
import us.zonix.practice.tournament.Tournament;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import us.zonix.practice.Practice;
import org.bukkit.command.Command;

public class LeaveEventCommand extends Command
{
    private final Practice plugin;
    
    public LeaveEventCommand() {
        super("leave");
        this.plugin = Practice.getInstance();
        this.setDescription("Leave an event or tournament.");
        this.setUsage(ChatColor.RED + "Usage: /leave");
    }
    
    public boolean execute(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player player = (Player)sender;
        final boolean inTournament = this.plugin.getTournamentManager().isInTournament(player.getUniqueId());
        final boolean inEvent = this.plugin.getEventManager().getEventPlaying(player) != null;
        if (inEvent) {
            this.leaveEvent(player);
        }
        else if (inTournament) {
            this.leaveTournament(player);
        }
        else {
            player.sendMessage(ChatColor.RED + "There is nothing to leave.");
        }
        return true;
    }
    
    private void leaveTournament(final Player player) {
        final Tournament tournament = this.plugin.getTournamentManager().getTournament(player.getUniqueId());
        if (tournament != null) {
            this.plugin.getTournamentManager().leaveTournament(player);
        }
    }
    
    private void leaveEvent(final Player player) {
        final PracticeEvent event = this.plugin.getEventManager().getEventPlaying(player);
        if (event == null) {
            player.sendMessage(ChatColor.RED + "That event doesn't exist.");
            return;
        }
        if (!this.plugin.getEventManager().isPlaying(player, event)) {
            player.sendMessage(ChatColor.RED + "You are not in an event.");
            return;
        }
        event.leave(player);
    }
}
