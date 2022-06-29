package us.zonix.practice.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import us.zonix.practice.util.ItemBuilder;
import org.bukkit.Server;
import java.util.stream.Stream;
import us.zonix.practice.party.Party;
import us.zonix.practice.player.PlayerData;
import java.util.function.Predicate;
import java.util.function.Function;
import java.util.Objects;
import java.util.Collection;
import java.util.UUID;
import java.util.ArrayList;
import us.zonix.practice.util.Clickable;
import us.zonix.practice.util.StringUtil;
import us.zonix.practice.player.PlayerState;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import java.util.List;
import java.util.Collections;
import org.bukkit.ChatColor;
import us.zonix.practice.Practice;
import org.bukkit.command.Command;

public class PartyCommand extends Command
{
    private static final String NOT_LEADER;
    private static final String[] HELP_MESSAGE;
    private final Practice plugin;
    private static final String ARROW = "\u2192";
    
    public PartyCommand() {
        super("party");
        this.plugin = Practice.getInstance();
        this.setDescription("Party Command.");
        this.setUsage(ChatColor.RED + "Usage: /party <subcommand> [player]");
        this.setAliases((List)Collections.singletonList("p"));
    }
    
    public boolean execute(final CommandSender sender, final String alias, final String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        final Player player = (Player)sender;
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        final Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
        final String subCommand = (args.length < 1) ? "help" : args[0];
        final String lowerCase = subCommand.toLowerCase();
        switch (lowerCase) {
            case "hcteams": {
                if (party == null) {
                    player.sendMessage(ChatColor.RED + "You're not in a party.");
                    break;
                }
                if (!party.getLeader().equals(player.getUniqueId())) {
                    sender.sendMessage(PartyCommand.NOT_LEADER);
                    return true;
                }
                this.openSelectionMenu(player);
                return true;
            }
            case "create": {
                if (party != null) {
                    player.sendMessage(ChatColor.RED + "You are already in a party.");
                    break;
                }
                if (playerData.getPlayerState() != PlayerState.SPAWN) {
                    player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
                    break;
                }
                this.plugin.getPartyManager().createParty(player);
                break;
            }
            case "leave": {
                if (party == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a party.");
                    break;
                }
                if (playerData.getPlayerState() != PlayerState.SPAWN) {
                    player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
                    break;
                }
                this.plugin.getPartyManager().leaveParty(player);
                break;
            }
            case "inv":
            case "invite": {
                if (party == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a party.");
                    break;
                }
                if (!this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You are not the leader of the party.");
                    break;
                }
                if (this.plugin.getTournamentManager().getTournament(player.getUniqueId()) != null) {
                    player.sendMessage(ChatColor.RED + "You are currently in a tournament.");
                    break;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party invite (player)");
                    break;
                }
                if (party.isOpen()) {
                    player.sendMessage(ChatColor.RED + "This party is open, so anyone can join.");
                    break;
                }
                if (party.getMembers().size() >= party.getLimit()) {
                    player.sendMessage(ChatColor.RED + "Party size has reached it's limit");
                    break;
                }
                if (party.getLeader() != player.getUniqueId()) {
                    player.sendMessage(PartyCommand.NOT_LEADER);
                    return true;
                }
                final Player target = this.plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[1]));
                    return true;
                }
                final PlayerData targetData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
                if (target.getUniqueId() == player.getUniqueId()) {
                    player.sendMessage(ChatColor.RED + "You can't invite yourself.");
                }
                else if (this.plugin.getPartyManager().getParty(target.getUniqueId()) != null) {
                    player.sendMessage(ChatColor.RED + "That player is already in a party.");
                }
                else if (targetData.getPlayerState() != PlayerState.SPAWN) {
                    player.sendMessage(ChatColor.RED + "That player is currently busy.");
                }
                else if (this.plugin.getPartyManager().hasPartyInvite(target.getUniqueId(), player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You have already sent a party invitation to this player, please wait.");
                }
                else {
                    this.plugin.getPartyManager().createPartyInvite(player.getUniqueId(), target.getUniqueId());
                    final Clickable partyInvite = new Clickable(ChatColor.GREEN + sender.getName() + ChatColor.YELLOW + " has invited you to their party! " + ChatColor.GRAY + "[Click to Accept]", ChatColor.GRAY + "Click to accept", "/party accept " + sender.getName());
                    partyInvite.sendToPlayer(target);
                    party.broadcast(ChatColor.GREEN.toString() + ChatColor.BOLD + "[*] " + ChatColor.YELLOW + target.getName() + " has been invited to the party.");
                }
                break;
            }
            case "accept": {
                if (party != null) {
                    player.sendMessage(ChatColor.RED + "You are already in a party.");
                    break;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party accept <player>.");
                    break;
                }
                if (playerData.getPlayerState() != PlayerState.SPAWN) {
                    player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
                    break;
                }
                final Player target = this.plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[1]));
                    return true;
                }
                final Party targetParty = this.plugin.getPartyManager().getParty(target.getUniqueId());
                if (targetParty == null) {
                    player.sendMessage(ChatColor.RED + "That player is not in a party.");
                }
                else if (targetParty.getMembers().size() >= targetParty.getLimit()) {
                    player.sendMessage(ChatColor.RED + "Party size has reached it's limit");
                }
                else if (!this.plugin.getPartyManager().hasPartyInvite(player.getUniqueId(), targetParty.getLeader())) {
                    player.sendMessage(ChatColor.RED + "You do not have any pending requests.");
                }
                else {
                    this.plugin.getPartyManager().joinParty(targetParty.getLeader(), player);
                }
                break;
            }
            case "join": {
                if (party != null) {
                    player.sendMessage(ChatColor.RED + "You are already in a party.");
                    break;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party join <player>.");
                    break;
                }
                if (playerData.getPlayerState() != PlayerState.SPAWN) {
                    player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
                    break;
                }
                final Player target = this.plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[1]));
                    return true;
                }
                final Party targetParty = this.plugin.getPartyManager().getParty(target.getUniqueId());
                if (targetParty == null || !targetParty.isOpen() || targetParty.getMembers().size() >= targetParty.getLimit()) {
                    player.sendMessage(ChatColor.RED + "You can't join this party.");
                }
                else {
                    this.plugin.getPartyManager().joinParty(targetParty.getLeader(), player);
                }
                break;
            }
            case "kick": {
                if (party == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a party.");
                    break;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party kick <player>.");
                    break;
                }
                if (party.getLeader() != player.getUniqueId()) {
                    player.sendMessage(PartyCommand.NOT_LEADER);
                    return true;
                }
                final Player target = this.plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[1]));
                    return true;
                }
                final Party targetParty = this.plugin.getPartyManager().getParty(target.getUniqueId());
                if (targetParty == null || targetParty.getLeader() != party.getLeader()) {
                    player.sendMessage(ChatColor.RED + "That player is not in your party.");
                }
                else {
                    this.plugin.getPartyManager().leaveParty(target);
                }
                break;
            }
            case "bard": {
                if (party == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a party.");
                    break;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party bard <player>.");
                    break;
                }
                if (party.getLeader() != player.getUniqueId()) {
                    player.sendMessage(PartyCommand.NOT_LEADER);
                    return true;
                }
                final Player target = this.plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[1]));
                    return true;
                }
                final Party targetParty = this.plugin.getPartyManager().getParty(target.getUniqueId());
                if (targetParty == null || targetParty.getLeader() != party.getLeader()) {
                    player.sendMessage(ChatColor.RED + "That player is not in your party.");
                }
                else {
                    if (party.getArchers().size() >= party.getMaxBards()) {
                        player.sendMessage(ChatColor.RED + String.format("Your party has already reached the limit of %s bard.", party.getMaxBards()));
                        return true;
                    }
                    if (party.getBards().contains(target.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + String.format("%s's role is already bard.", target.getName()));
                        return true;
                    }
                    party.addBard(target);
                    player.sendMessage(ChatColor.GREEN + target.getName() + "'s role is now bard.");
                }
                break;
            }
            case "archer": {
                if (party == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a party.");
                    break;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party archer <player>.");
                    break;
                }
                if (party.getLeader() != player.getUniqueId()) {
                    player.sendMessage(PartyCommand.NOT_LEADER);
                    return true;
                }
                final Player target = this.plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[1]));
                    return true;
                }
                final Party targetParty = this.plugin.getPartyManager().getParty(target.getUniqueId());
                if (targetParty == null || targetParty.getLeader() != party.getLeader()) {
                    player.sendMessage(ChatColor.RED + "That player is not in your party.");
                }
                else {
                    if (party.getArchers().size() >= party.getMaxArchers()) {
                        player.sendMessage(ChatColor.RED + String.format("Your party has already reached the limit of %s archers.", party.getMaxArchers()));
                        return true;
                    }
                    if (party.getArchers().contains(target.getUniqueId())) {
                        player.sendMessage(ChatColor.RED + String.format("%s's role is already archer.", target.getName()));
                        return true;
                    }
                    party.addArcher(target);
                    player.sendMessage(ChatColor.GREEN + target.getName() + "'s role is now archer.");
                }
                break;
            }
            case "setlimit": {
                if (party == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a party.");
                    break;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party setlimit <amount>.");
                    break;
                }
                if (party.getLeader() != player.getUniqueId()) {
                    player.sendMessage(PartyCommand.NOT_LEADER);
                    return true;
                }
                try {
                    final int limit = Integer.parseInt(args[1]);
                    if (limit < 2 || limit > 50) {
                        player.sendMessage(ChatColor.RED + "That is not a valid limit.");
                    }
                    else {
                        party.setLimit(limit);
                        player.sendMessage(ChatColor.GREEN + "You have set the party player limit to " + ChatColor.YELLOW + limit + " players.");
                    }
                }
                catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "That is not a number.");
                }
                break;
            }
            case "open":
            case "lock": {
                if (party == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a party.");
                    break;
                }
                if (party.getLeader() != player.getUniqueId()) {
                    player.sendMessage(PartyCommand.NOT_LEADER);
                    return true;
                }
                party.setOpen(!party.isOpen());
                party.broadcast(ChatColor.GREEN.toString() + ChatColor.BOLD + "[*] " + ChatColor.YELLOW + "Your party is now " + ChatColor.BOLD + (party.isOpen() ? "OPEN" : "LOCKED"));
                break;
            }
            case "info": {
                if (party == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a party.");
                    break;
                }
                final List<UUID> members = new ArrayList<UUID>(party.getMembers());
                members.remove(party.getLeader());
                final StringBuilder builder = new StringBuilder(ChatColor.GOLD + "Members (" + party.getMembers().size() + "): ");
                final Stream<Object> stream = members.stream();
                final Server server = this.plugin.getServer();
                Objects.requireNonNull(server);
                stream.map((Function<? super Object, ?>)server::getPlayer).filter(Objects::nonNull).forEach(member -> builder.append(ChatColor.GRAY).append(member.getName()).append(","));
                final String[] information = { ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------", ChatColor.RED + "Party Information:", ChatColor.GOLD + "Leader: " + ChatColor.GRAY + this.plugin.getServer().getPlayer(party.getLeader()).getName(), ChatColor.GOLD + builder.toString(), ChatColor.GOLD + "Party State: " + ChatColor.GRAY + (party.isOpen() ? "Open" : "Locked"), ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------" };
                player.sendMessage(information);
                break;
            }
            default: {
                player.sendMessage(PartyCommand.HELP_MESSAGE);
                break;
            }
        }
        return true;
    }
    
    private void openSelectionMenu(final Player sender) {
        Party.CLASS_SELECTION_MENU.open(sender);
    }
    
    private ItemBuilder getItemBuilder(final PlayerRole role) {
        return new ItemBuilder(role.getMaterial()).lore(ChatColor.GREEN + "\u2192" + role.getFormattedName()).lore(ChatColor.GRAY + "next " + role.next().getFormattedName());
    }
    
    static {
        NOT_LEADER = ChatColor.RED + "You are not the leader of the party!";
        HELP_MESSAGE = new String[] { ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------", ChatColor.RED + "Party Commands:", ChatColor.GRAY + "(*) /party help " + ChatColor.WHITE + "- Displays the help menu", ChatColor.GRAY + "(*) /party create " + ChatColor.WHITE + "- Creates a party instance", ChatColor.GRAY + "(*) /party leave " + ChatColor.WHITE + "- Leave your current party", ChatColor.GRAY + "(*) /party info " + ChatColor.WHITE + "- Displays your party information", ChatColor.GRAY + "(*) /party join (player) " + ChatColor.WHITE + "- Join a party (invited or unlocked)", "", ChatColor.RED + "Leader Commands:", ChatColor.GRAY + "(*) /party open " + ChatColor.WHITE + "- Open your party for others to join", ChatColor.GRAY + "(*) /party lock " + ChatColor.WHITE + "- Lock your party for others to join", ChatColor.GRAY + "(*) /party setlimit (amount) " + ChatColor.WHITE + "- Set a limit to your party", ChatColor.GRAY + "(*) /party invite (player) " + ChatColor.WHITE + "- Invites a player to your party", ChatColor.GRAY + "(*) /party kick (player) " + ChatColor.WHITE + "- Kicks a player from your party", ChatColor.GRAY + "(*) /party bard (player) " + ChatColor.WHITE + "- Gives a player the bard role for HCTeams", ChatColor.GRAY + "(*) /party archer (player) " + ChatColor.WHITE + "- Gives a player the archer role for HCTeams", ChatColor.GRAY + "(*) /party hcteams " + ChatColor.WHITE + "- Opens a role selection menu for HCTeams", ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "----------------------------------------------------" };
    }
    
    private enum PlayerRole
    {
        DIAMOND(Material.DIAMOND_HELMET), 
        BARD(Material.GOLD_HELMET), 
        ARCHER(Material.LEATHER_HELMET);
        
        private final Material material;
        
        public String getFormattedName() {
            return StringUtils.capitalize(this.toString().toLowerCase());
        }
        
        public PlayerRole next() {
            return values()[(this.ordinal() + 1) % values().length];
        }
        
        private PlayerRole(final Material material) {
            this.material = material;
        }
        
        public Material getMaterial() {
            return this.material;
        }
    }
    
    public static class HCTeamsCommand extends Command
    {
        public HCTeamsCommand() {
            super("hcteams");
        }
        
        public boolean execute(final CommandSender commandSender, final String s, final String[] strings) {
            ((Player)commandSender).chat("/party hcteams");
            return true;
        }
    }
}
