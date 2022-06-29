package us.zonix.practice.ffa.killstreak.impl;

import java.util.Arrays;
import java.util.List;
import us.zonix.practice.util.PlayerUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import us.zonix.practice.ffa.killstreak.KillStreak;

public class GappleKillStreak implements KillStreak
{
    @Override
    public void giveKillStreak(final Player player) {
        PlayerUtil.setFirstSlotOfType(player, Material.POTION, new ItemStack(Material.GOLDEN_APPLE, 3));
    }
    
    @Override
    public List<Integer> getStreaks() {
        return Arrays.asList(3, 15);
    }
}
