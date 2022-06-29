package us.zonix.practice.settings.item;

import us.zonix.practice.util.inventory.UtilItem;
import org.bukkit.Material;
import us.zonix.practice.util.ItemBuilder;
import org.apache.commons.lang3.StringEscapeUtils;
import java.util.Collection;
import org.bukkit.ChatColor;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public enum ProfileOptionsItem
{
    DUEL_REQUESTS(UtilItem.createItem(Material.LEASH, 1, (short)0, ChatColor.RED.toString() + ChatColor.BOLD + "Duel Requests"), "Do you want to accept duel requests?"), 
    PARTY_INVITES(UtilItem.createItem(Material.PAPER, 1, (short)0, ChatColor.RED.toString() + ChatColor.BOLD + "Party Invites"), "Do you want to accept party invitations?"), 
    TOGGLE_SCOREBOARD(UtilItem.createItem(Material.BOOKSHELF, 1, (short)0, ChatColor.RED.toString() + ChatColor.BOLD + "Toggle Scoreboard"), "Toggle your scoreboard"), 
    ALLOW_SPECTATORS(UtilItem.createItem(Material.COMPASS, 1, (short)0, ChatColor.RED.toString() + ChatColor.BOLD + "Allow Spectators"), "Allow players to spectate your matches?"), 
    TOGGLE_TIME(UtilItem.createItem(Material.SLIME_BALL, 1, (short)0, ChatColor.RED.toString() + ChatColor.BOLD + "Toggle Time"), "Toggle between day, sunset & night"), 
    TOGGLE_PING(UtilItem.createItem(Material.FLINT_AND_STEEL, 1, (short)0, ChatColor.RED.toString() + ChatColor.BOLD + "Toggle Ping Matchmaking"), "Toggle between -1, 25, 50, 75, 100 ping ranges"), 
    TOGGLE_VISIBILITY(UtilItem.createItem(Material.WATCH, 1, (short)0, ChatColor.RED.toString() + ChatColor.BOLD + "Toggle Visibility"), "Toggle your visibility");
    
    private ItemStack item;
    private List<String> description;
    
    private ProfileOptionsItem(final ItemStack item, final String description) {
        this.item = item;
        (this.description = new ArrayList<String>()).add(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------");
        String parts = "";
        for (int i = 0; i < description.split(" ").length; ++i) {
            final String part = description.split(" ")[i];
            parts = parts + part + " ";
            if (i == 4 || i + 1 == description.split(" ").length) {
                this.description.add(ChatColor.GRAY + parts.trim());
                parts = "";
            }
        }
        this.description.add(" ");
    }
    
    public ItemStack getItem(final ProfileOptionsItemState state) {
        if (this == ProfileOptionsItem.DUEL_REQUESTS || this == ProfileOptionsItem.PARTY_INVITES || this == ProfileOptionsItem.ALLOW_SPECTATORS || this == ProfileOptionsItem.TOGGLE_VISIBILITY) {
            final List<String> lore = new ArrayList<String>(this.description);
            lore.add("  " + ((state == ProfileOptionsItemState.ENABLED) ? (ChatColor.GREEN + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.ENABLED));
            lore.add("  " + ((state == ProfileOptionsItemState.DISABLED) ? (ChatColor.RED + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.DISABLED));
            lore.add(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------");
            return new ItemBuilder(this.item).lore(lore).build();
        }
        if (this == ProfileOptionsItem.TOGGLE_TIME) {
            final List<String> lore = new ArrayList<String>(this.description);
            lore.add("  " + ((state == ProfileOptionsItemState.DAY) ? (ChatColor.YELLOW + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.DAY));
            lore.add("  " + ((state == ProfileOptionsItemState.SUNSET) ? (ChatColor.GOLD + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.SUNSET));
            lore.add("  " + ((state == ProfileOptionsItemState.NIGHT) ? (ChatColor.BLUE + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.NIGHT));
            lore.add(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------");
            return new ItemBuilder(this.item).lore(lore).build();
        }
        if (this == ProfileOptionsItem.TOGGLE_SCOREBOARD) {
            final List<String> lore = new ArrayList<String>(this.description);
            lore.add("  " + ((state == ProfileOptionsItemState.ENABLED) ? (ChatColor.GREEN + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.ENABLED));
            lore.add("  " + ((state == ProfileOptionsItemState.SHOW_PING) ? (ChatColor.YELLOW + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.SHOW_PING));
            lore.add("  " + ((state == ProfileOptionsItemState.DISABLED) ? (ChatColor.RED + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.DISABLED));
            lore.add(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------");
            return new ItemBuilder(this.item).lore(lore).build();
        }
        if (this == ProfileOptionsItem.TOGGLE_PING) {
            final List<String> lore = new ArrayList<String>(this.description);
            lore.add("  " + ((state == ProfileOptionsItemState.NO_RANGE) ? (ChatColor.RED + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.NO_RANGE));
            lore.add("  " + ((state == ProfileOptionsItemState.RANGE_25) ? (ChatColor.YELLOW + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.RANGE_25));
            lore.add("  " + ((state == ProfileOptionsItemState.RANGE_50) ? (ChatColor.YELLOW + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.RANGE_50));
            lore.add("  " + ((state == ProfileOptionsItemState.RANGE_75) ? (ChatColor.YELLOW + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.RANGE_75));
            lore.add("  " + ((state == ProfileOptionsItemState.RANGE_100) ? (ChatColor.YELLOW + StringEscapeUtils.unescapeHtml4("&#9658;") + " ") : "  ") + ChatColor.GRAY + this.getOptionDescription(ProfileOptionsItemState.RANGE_100));
            lore.add(ChatColor.DARK_GRAY.toString() + ChatColor.STRIKETHROUGH + "------------------------");
            return new ItemBuilder(this.item).lore(lore).build();
        }
        return this.getItem(ProfileOptionsItemState.DISABLED);
    }
    
    public String getOptionDescription(final ProfileOptionsItemState state) {
        if (this == ProfileOptionsItem.DUEL_REQUESTS || this == ProfileOptionsItem.PARTY_INVITES || this == ProfileOptionsItem.ALLOW_SPECTATORS || this == ProfileOptionsItem.TOGGLE_VISIBILITY) {
            if (state == ProfileOptionsItemState.ENABLED) {
                return "Enable";
            }
            if (state == ProfileOptionsItemState.DISABLED) {
                return "Disable";
            }
        }
        else if (this == ProfileOptionsItem.TOGGLE_TIME) {
            if (state == ProfileOptionsItemState.DAY) {
                return "Day";
            }
            if (state == ProfileOptionsItemState.SUNSET) {
                return "Sunset";
            }
            if (state == ProfileOptionsItemState.NIGHT) {
                return "Night";
            }
        }
        else if (this == ProfileOptionsItem.TOGGLE_SCOREBOARD) {
            if (state == ProfileOptionsItemState.ENABLED) {
                return "Enable";
            }
            if (state == ProfileOptionsItemState.SHOW_PING) {
                return "Show Ping";
            }
            if (state == ProfileOptionsItemState.DISABLED) {
                return "Disable";
            }
        }
        else if (this == ProfileOptionsItem.TOGGLE_PING) {
            if (state == ProfileOptionsItemState.NO_RANGE) {
                return "No Ping Range";
            }
            if (state == ProfileOptionsItemState.RANGE_25) {
                return "25ms Range";
            }
            if (state == ProfileOptionsItemState.RANGE_50) {
                return "50ms Range";
            }
            if (state == ProfileOptionsItemState.RANGE_75) {
                return "75ms Range";
            }
            if (state == ProfileOptionsItemState.RANGE_100) {
                return "100ms Range";
            }
        }
        return this.getOptionDescription(ProfileOptionsItemState.DISABLED);
    }
    
    public static ProfileOptionsItem fromItem(final ItemStack itemStack) {
        for (final ProfileOptionsItem item : values()) {
            for (final ProfileOptionsItemState state : ProfileOptionsItemState.values()) {
                if (item.getItem(state).isSimilar(itemStack)) {
                    return item;
                }
            }
        }
        return null;
    }
}
