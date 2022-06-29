package us.zonix.practice.ffa.killstreak;

import java.util.List;
import org.bukkit.entity.Player;

public interface KillStreak
{
    void giveKillStreak(final Player p0);
    
    List<Integer> getStreaks();
}
