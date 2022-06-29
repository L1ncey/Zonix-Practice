package us.zonix.practice.board;

import org.bukkit.scoreboard.Scoreboard;
import java.util.List;
import org.bukkit.entity.Player;

public interface BoardAdapter
{
    List<String> getScoreboard(final Player p0, final Board p1);
    
    String getTitle(final Player p0);
    
    long getInterval();
    
    void onScoreboardCreate(final Player p0, final Scoreboard p1);
    
    void preLoop();
}
