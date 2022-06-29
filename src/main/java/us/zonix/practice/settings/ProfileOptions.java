package us.zonix.practice.settings;

import us.zonix.practice.settings.item.ProfileOptionsItem;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import us.zonix.practice.settings.item.ProfileOptionsItemState;

public class ProfileOptions
{
    private boolean duelRequests;
    private boolean partyInvites;
    private boolean spectators;
    private ProfileOptionsItemState scoreboard;
    private ProfileOptionsItemState time;
    private ProfileOptionsItemState pingBased;
    private boolean visibility;
    
    public ProfileOptions() {
        this.duelRequests = true;
        this.partyInvites = true;
        this.spectators = true;
        this.scoreboard = ProfileOptionsItemState.SHOW_PING;
        this.time = ProfileOptionsItemState.DAY;
        this.pingBased = ProfileOptionsItemState.NO_RANGE;
        this.visibility = true;
    }
    
    public Inventory getInventory() {
        final Inventory toReturn = Bukkit.createInventory((InventoryHolder)null, 9, ChatColor.DARK_RED + "Settings");
        toReturn.setItem(0, ProfileOptionsItem.DUEL_REQUESTS.getItem(this.duelRequests ? ProfileOptionsItemState.ENABLED : ProfileOptionsItemState.DISABLED));
        toReturn.setItem(1, ProfileOptionsItem.PARTY_INVITES.getItem(this.partyInvites ? ProfileOptionsItemState.ENABLED : ProfileOptionsItemState.DISABLED));
        toReturn.setItem(2, ProfileOptionsItem.ALLOW_SPECTATORS.getItem(this.spectators ? ProfileOptionsItemState.ENABLED : ProfileOptionsItemState.DISABLED));
        toReturn.setItem(4, ProfileOptionsItem.TOGGLE_PING.getItem(this.pingBased));
        toReturn.setItem(6, ProfileOptionsItem.TOGGLE_SCOREBOARD.getItem(this.scoreboard));
        toReturn.setItem(7, ProfileOptionsItem.TOGGLE_VISIBILITY.getItem(this.visibility ? ProfileOptionsItemState.ENABLED : ProfileOptionsItemState.DISABLED));
        toReturn.setItem(8, ProfileOptionsItem.TOGGLE_TIME.getItem(this.time));
        return toReturn;
    }
    
    public boolean isDuelRequests() {
        return this.duelRequests;
    }
    
    public ProfileOptions setDuelRequests(final boolean duelRequests) {
        this.duelRequests = duelRequests;
        return this;
    }
    
    public boolean isPartyInvites() {
        return this.partyInvites;
    }
    
    public ProfileOptions setPartyInvites(final boolean partyInvites) {
        this.partyInvites = partyInvites;
        return this;
    }
    
    public boolean isSpectators() {
        return this.spectators;
    }
    
    public ProfileOptions setSpectators(final boolean spectators) {
        this.spectators = spectators;
        return this;
    }
    
    public ProfileOptionsItemState getScoreboard() {
        return this.scoreboard;
    }
    
    public ProfileOptions setScoreboard(final ProfileOptionsItemState scoreboard) {
        this.scoreboard = scoreboard;
        return this;
    }
    
    public ProfileOptionsItemState getTime() {
        return this.time;
    }
    
    public ProfileOptions setTime(final ProfileOptionsItemState time) {
        this.time = time;
        return this;
    }
    
    public ProfileOptionsItemState getPingBased() {
        return this.pingBased;
    }
    
    public ProfileOptions setPingBased(final ProfileOptionsItemState pingBased) {
        this.pingBased = pingBased;
        return this;
    }
    
    public boolean isVisibility() {
        return this.visibility;
    }
    
    public ProfileOptions setVisibility(final boolean visibility) {
        this.visibility = visibility;
        return this;
    }
}
