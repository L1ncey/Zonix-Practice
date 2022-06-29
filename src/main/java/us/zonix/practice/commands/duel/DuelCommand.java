package us.zonix.practice.commands.duel;

import us.zonix.practice.party.Party;
import us.zonix.practice.player.PlayerData;
import us.zonix.practice.util.StringUtil;
import us.zonix.practice.player.PlayerState;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import us.zonix.practice.Practice;
import org.bukkit.command.Command;

public class DuelCommand extends Command
{
    private final Practice plugin;
    
    public DuelCommand() {
        super("duel");
        this.plugin = Practice.getInstance();
        this.setDescription("Duel a player.");
        this.setUsage(ChatColor.RED + "Usage: /duel <player>");
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
        if (this.plugin.getTournamentManager().getTournament(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "You are currently in a tournament.");
            return true;
        }
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (playerData.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(ChatColor.RED + "Cannot execute this command in your current state.");
            return true;
        }
        final Player target = this.plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(String.format(StringUtil.PLAYER_NOT_FOUND, args[0]));
            return true;
        }
        if (this.plugin.getTournamentManager().getTournament(target.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "That player is currently in a tournament.");
            return true;
        }
        final Party party = this.plugin.getPartyManager().getParty(player.getUniqueId());
        final Party targetParty = this.plugin.getPartyManager().getParty(target.getUniqueId());
        if (player.getName().equals(target.getName())) {
            player.sendMessage(ChatColor.RED + "You can't duel yourself.");
            return true;
        }
        if (party != null && targetParty != null && party == targetParty) {
            player.sendMessage(ChatColor.RED + "You can't duel yourself.");
            return true;
        }
        if (party != null && !this.plugin.getPartyManager().isLeader(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are not the leader fo the party.");
            return true;
        }
        final PlayerData targetData = this.plugin.getPlayerManager().getPlayerData(target.getUniqueId());
        if (targetData.getPlayerState() != PlayerState.SPAWN) {
            player.sendMessage(ChatColor.RED + "That player is currently busy.");
            return true;
        }
        if (!targetData.getOptions().isDuelRequests()) {
            player.sendMessage(ChatColor.RED + "That player has ignored duel requests.");
            return true;
        }
        if (party == null && targetParty != null) {
            player.sendMessage(ChatColor.RED + "That player is currently in a party.");
            return true;
        }
        if (party != null && targetParty == null) {
            player.sendMessage(ChatColor.RED + "You are currently in a party.");
            return true;
        }
        if (this.plugin.getMatchManager().getMatchRequest(target.getUniqueId(), player.getUniqueId()) != null) {
            player.performCommand("accept " + target.getName());
            return true;
        }
        playerData.setDuelSelecting(target.getUniqueId());
        player.openInventory(this.plugin.getInventoryManager().getDuelInventory().getCurrentPage());
        return true;
    }
}
