package us.zonix.practice.managers;

import us.zonix.practice.player.PlayerData;
import us.zonix.practice.player.PlayerState;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.HashMap;
import us.zonix.practice.util.TtlHashMap;
import java.util.concurrent.TimeUnit;
import us.zonix.practice.party.Party;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import us.zonix.practice.Practice;

public class PartyManager
{
    private final Practice plugin;
    private Map<UUID, List<UUID>> partyInvites;
    private Map<UUID, Party> parties;
    private Map<UUID, UUID> partyLeaders;
    
    public PartyManager() {
        this.plugin = Practice.getInstance();
        this.partyInvites = new TtlHashMap<UUID, List<UUID>>(TimeUnit.SECONDS, 15L);
        this.parties = new HashMap<UUID, Party>();
        this.partyLeaders = new HashMap<UUID, UUID>();
    }
    
    public boolean isLeader(final UUID uuid) {
        return this.parties.containsKey(uuid);
    }
    
    public void removePartyInvites(final UUID uuid) {
        this.partyInvites.remove(uuid);
    }
    
    public boolean hasPartyInvite(final UUID player, final UUID other) {
        return this.partyInvites.get(player) != null && this.partyInvites.get(player).contains(other);
    }
    
    public void createPartyInvite(final UUID requester, final UUID requested) {
        this.partyInvites.computeIfAbsent(requested, k -> new ArrayList()).add(requester);
    }
    
    public boolean isInParty(final UUID player, final Party party) {
        final Party targetParty = this.getParty(player);
        return targetParty != null && targetParty.getLeader() == party.getLeader();
    }
    
    public Party getParty(final UUID player) {
        if (this.parties.containsKey(player)) {
            return this.parties.get(player);
        }
        if (this.partyLeaders.containsKey(player)) {
            final UUID leader = this.partyLeaders.get(player);
            return this.parties.get(leader);
        }
        return null;
    }
    
    public void createParty(final Player player) {
        final Party party = new Party(player.getUniqueId());
        this.parties.put(player.getUniqueId(), party);
        this.plugin.getInventoryManager().addParty(player);
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
        player.sendMessage(ChatColor.YELLOW + "You have created a party.");
    }
    
    private void disbandParty(final Party party, final boolean tournament) {
        this.plugin.getInventoryManager().removeParty(party);
        this.parties.remove(party.getLeader());
        party.broadcast(ChatColor.YELLOW + "Your party has been disbanded.");
        final PlayerData memberData;
        party.members().forEach(member -> {
            memberData = this.plugin.getPlayerManager().getPlayerData(member.getUniqueId());
            if (this.partyLeaders.get(memberData.getUniqueId()) != null) {
                this.partyLeaders.remove(memberData.getUniqueId());
            }
            if (memberData.getPlayerState() == PlayerState.SPAWN) {
                this.plugin.getPlayerManager().sendToSpawnAndReset(member);
            }
        });
    }
    
    public void leaveParty(final Player player) {
        final Party party = this.getParty(player.getUniqueId());
        if (party == null) {
            return;
        }
        final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        if (this.parties.containsKey(player.getUniqueId())) {
            this.disbandParty(party, false);
        }
        else if (this.plugin.getTournamentManager().getTournament(player.getUniqueId()) != null) {
            this.disbandParty(party, true);
        }
        else {
            party.broadcast(ChatColor.RED.toString() + ChatColor.BOLD + "[-] " + ChatColor.YELLOW + player.getName() + " left the party.");
            party.removeMember(player.getUniqueId());
            party.getBards().remove(player.getUniqueId());
            party.getArchers().remove(player.getUniqueId());
            this.partyLeaders.remove(player.getUniqueId());
            this.plugin.getInventoryManager().updateParty(party);
        }
        switch (playerData.getPlayerState()) {
            case FIGHTING: {
                this.plugin.getMatchManager().removeFighter(player, playerData, false);
                break;
            }
            case SPECTATING: {
                if (this.plugin.getEventManager().getSpectators().containsKey(player.getUniqueId())) {
                    this.plugin.getEventManager().removeSpectator(player);
                    break;
                }
                this.plugin.getMatchManager().removeSpectator(player);
                break;
            }
        }
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
    }
    
    public void joinParty(final UUID leader, final Player player) {
        final Party party = this.getParty(leader);
        if (this.plugin.getTournamentManager().getTournament(leader) != null) {
            player.sendMessage(ChatColor.RED + "That player is in a tournament.");
            return;
        }
        this.partyLeaders.put(player.getUniqueId(), leader);
        party.addMember(player.getUniqueId());
        this.plugin.getInventoryManager().updateParty(party);
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
        party.broadcast(ChatColor.GREEN.toString() + ChatColor.BOLD + "[+] " + ChatColor.YELLOW + player.getName() + " joined the party.");
    }
}
