package us.zonix.practice.ffa.killstreak.impl;

import java.util.Arrays;
import java.util.List;
import us.zonix.practice.util.PlayerUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.zonix.practice.ffa.killstreak.KillStreak;

public class DebuffKillStreak implements KillStreak
{
    private static final ItemStack SLOWNESS;
    private static final ItemStack POISON;
    
    @Override
    public void giveKillStreak(final Player player) {
        PlayerUtil.setFirstSlotOfType(player, Material.POTION, DebuffKillStreak.SLOWNESS.clone());
        PlayerUtil.setFirstSlotOfType(player, Material.POTION, DebuffKillStreak.POISON.clone());
    }
    
    @Override
    public List<Integer> getStreaks() {
        return Arrays.asList(7, 25);
    }
    
    static {
        SLOWNESS = new ItemStack(Material.POTION, 1, (short)16394);
        POISON = new ItemStack(Material.POTION, 1, (short)16388);
    }
}
