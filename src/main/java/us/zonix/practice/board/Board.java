package us.zonix.practice.board;

import java.util.Iterator;
import java.util.Collection;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import us.zonix.practice.Practice;
import java.util.HashSet;
import java.util.ArrayList;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import java.util.Set;
import java.util.List;
import org.bukkit.entity.Player;

public class Board
{
    private final BoardAdapter adapter;
    private final Player player;
    private List<BoardEntry> entries;
    private Set<BoardTimer> timers;
    private Set<String> keys;
    private Scoreboard scoreboard;
    private Objective objective;
    
    public Board(final Player player, final BoardAdapter adapter) {
        this.entries = new ArrayList<BoardEntry>();
        this.timers = new HashSet<BoardTimer>();
        this.keys = new HashSet<String>();
        this.adapter = adapter;
        this.player = player;
        this.init();
    }
    
    private void init() {
        if (!this.player.getScoreboard().equals(Practice.getInstance().getServer().getScoreboardManager().getMainScoreboard())) {
            this.scoreboard = this.player.getScoreboard();
        }
        else {
            this.scoreboard = Practice.getInstance().getServer().getScoreboardManager().getNewScoreboard();
        }
        (this.objective = this.scoreboard.registerNewObjective("Default", "dummy")).setDisplaySlot(DisplaySlot.SIDEBAR);
        this.objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.adapter.getTitle(this.player)));
    }
    
    public String getNewKey(final BoardEntry entry) {
        for (final ChatColor color : ChatColor.values()) {
            String colorText = color + "" + ChatColor.WHITE;
            if (entry.getText().length() > 16) {
                final String sub = entry.getText().substring(0, 16);
                colorText += ChatColor.getLastColors(sub);
            }
            if (!this.keys.contains(colorText)) {
                this.keys.add(colorText);
                return colorText;
            }
        }
        throw new IndexOutOfBoundsException("No more keys available!");
    }
    
    public List<String> getBoardEntriesFormatted() {
        final List<String> toReturn = new ArrayList<String>();
        for (final BoardEntry entry : new ArrayList<BoardEntry>(this.entries)) {
            toReturn.add(entry.getText());
        }
        return toReturn;
    }
    
    public BoardEntry getByPosition(final int position) {
        for (int i = 0; i < this.entries.size(); ++i) {
            if (i == position) {
                return this.entries.get(i);
            }
        }
        return null;
    }
    
    public BoardTimer getCooldown(final String id) {
        for (final BoardTimer cooldown : this.getTimers()) {
            if (cooldown.getId().equals(id)) {
                return cooldown;
            }
        }
        return null;
    }
    
    public Set<BoardTimer> getTimers() {
        this.timers.removeIf(cooldown -> System.currentTimeMillis() >= cooldown.getEnd());
        return this.timers;
    }
    
    public BoardAdapter getAdapter() {
        return this.adapter;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
    public List<BoardEntry> getEntries() {
        return this.entries;
    }
    
    public Set<String> getKeys() {
        return this.keys;
    }
    
    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }
    
    public Objective getObjective() {
        return this.objective;
    }
}
