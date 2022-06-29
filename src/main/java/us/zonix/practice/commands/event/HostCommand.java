package us.zonix.practice.commands.event;

import java.util.Collection;
import us.zonix.practice.events.PracticeEvent;
import me.maiko.dexter.rank.Rank;
import org.apache.commons.lang.math.NumberUtils;
import java.util.function.Consumer;
import java.util.Objects;
import us.zonix.practice.util.Clickable;
import us.zonix.practice.events.EventState;
import me.maiko.dexter.util.CC;
import me.maiko.dexter.profile.Profile;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import us.zonix.practice.Practice;
import org.bukkit.command.Command;

public class HostCommand extends Command
{
    private final Practice plugin;
    
    public HostCommand() {
        super("host");
        this.plugin = Practice.getInstance();
        this.setDescription("Host an event.");
        this.setUsage(ChatColor.RED + "Usage: /host <event>");
    }
    
    public boolean execute(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player player = (Player)sender;
        if (args.length < 1) {
            player.openInventory(this.plugin.getInventoryManager().getEventsInventory().getCurrentPage());
            return true;
        }
        final String eventName = args[0];
        if (eventName == null) {
            return true;
        }
        final Profile profile = Profile.getByUuid(player.getUniqueId());
        final Rank rank = profile.getRank();
        if (eventName.equalsIgnoreCase("Parkour") && !sender.hasPermission("practice.events.parkour")) {
            player.sendMessage(CC.RED + "You cannot host the Parkour Event with " + rank.getGameColor() + rank.getId() + CC.RED + " rank.");
            return false;
        }
        if (eventName.equalsIgnoreCase("Sumo") && !sender.hasPermission("practice.events.sumo")) {
            player.sendMessage(CC.RED + "You cannot host the Sumo Event with " + rank.getGameColor() + rank.getId() + CC.RED + " rank.");
            return false;
        }
        if (eventName.equalsIgnoreCase("RedLightGreenLight") && !sender.hasPermission("practice.events.redlightgreenlight")) {
            player.sendMessage(CC.RED + "You cannot host the Red Light Green Light Event with " + rank.getGameColor() + rank.getId() + CC.RED + " rank.");
            return false;
        }
        if (eventName.equalsIgnoreCase("BlockParty") && !sender.hasPermission("practice.events.blockparty")) {
            player.sendMessage(CC.RED + "You cannot host the Block Party Event with " + rank.getGameColor() + rank.getId() + CC.RED + " rank.");
            return false;
        }
        if (eventName.equalsIgnoreCase("TNTTag") && !sender.hasPermission("practice.events.tnttag")) {
            player.sendMessage(CC.RED + "You cannot host the TnT Tag Event with " + rank.getGameColor() + rank.getId() + CC.RED + " rank.");
            return false;
        }
        if (this.plugin.getEventManager().getByName(eventName) == null) {
            player.sendMessage(ChatColor.RED + "That event doesn't exist.");
            return true;
        }
        if (System.currentTimeMillis() < this.plugin.getEventManager().getCooldown()) {
            player.sendMessage(ChatColor.RED + "There is a cooldown. Event can't start at this moment.");
            return true;
        }
        final PracticeEvent event = this.plugin.getEventManager().getByName(eventName);
        if (!event.isEnabled()) {
            player.sendMessage(ChatColor.RED + "That event is currently disabled.");
        }
        if (event.getState() != EventState.UNANNOUNCED) {
            player.sendMessage(ChatColor.RED + "There is currently an active event.");
            return true;
        }
        final boolean eventBeingHosted = this.plugin.getEventManager().getEvents().values().stream().anyMatch(e -> e.getState() != EventState.UNANNOUNCED);
        if (eventBeingHosted) {
            player.sendMessage(ChatColor.RED + "There is currently an active event.");
            return true;
        }
        final String toSend = ChatColor.RED.toString() + ChatColor.BOLD + "[Event] " + ChatColor.WHITE + "" + event.getName() + " is starting soon. " + ChatColor.GRAY + "[Join]";
        final String toSendDonor = ChatColor.GRAY + "[" + ChatColor.BOLD + "*" + ChatColor.GRAY + "] " + ChatColor.RED.toString() + ChatColor.BOLD + player.getName() + ChatColor.WHITE + " is hosting a " + ChatColor.WHITE.toString() + ChatColor.BOLD + event.getName() + " Event. " + ChatColor.GRAY + "[Join]";
        final Clickable message = new Clickable(player.hasPermission("practice.donator") ? toSendDonor : toSend, ChatColor.GRAY + "Click to join this event.", "/join " + event.getName());
        final Collection onlinePlayers = this.plugin.getServer().getOnlinePlayers();
        final Clickable clickable = message;
        Objects.requireNonNull(clickable);
        onlinePlayers.forEach(clickable::sendToPlayer);
        if (player.hasPermission("host.limit.50")) {
            event.setLimit(50);
        }
        else if (player.hasPermission("host.limit.45")) {
            event.setLimit(45);
        }
        else if (player.hasPermission("host.limit.40")) {
            event.setLimit(40);
        }
        else if (player.hasPermission("host.limit.35")) {
            event.setLimit(35);
        }
        else {
            event.setLimit(30);
        }
        if (args.length == 2 && player.isOp()) {
            if (!NumberUtils.isNumber(args[1])) {
                player.sendMessage(ChatColor.RED + "That's not a correct amount.");
                return true;
            }
            event.setLimit(Integer.parseInt(args[1]));
        }
        Practice.getInstance().getEventManager().hostEvent(event, player);
        return true;
    }
}
