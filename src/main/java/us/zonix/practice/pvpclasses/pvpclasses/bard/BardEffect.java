package us.zonix.practice.pvpclasses.pvpclasses.bard;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.potion.PotionEffect;

public class BardEffect
{
    private PotionEffect potionEffect;
    private int energy;
    private Map<String, Long> lastMessageSent;
    
    public PotionEffect getPotionEffect() {
        return this.potionEffect;
    }
    
    public int getEnergy() {
        return this.energy;
    }
    
    public Map<String, Long> getLastMessageSent() {
        return this.lastMessageSent;
    }
    
    public static BardEffect fromPotion(final PotionEffect potionEffect) {
        return new BardEffect(potionEffect, -1);
    }
    
    public static BardEffect fromPotionAndEnergy(final PotionEffect potionEffect, final int energy) {
        return new BardEffect(potionEffect, energy);
    }
    
    public static BardEffect fromEnergy(final int energy) {
        return new BardEffect(null, energy);
    }
    
    private BardEffect(final PotionEffect potionEffect, final int energy) {
        this.lastMessageSent = new HashMap<String, Long>();
        this.potionEffect = potionEffect;
        this.energy = energy;
    }
}
