package us.zonix.practice.bots;

import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.npc.NPC;
import java.util.Iterator;
import org.bukkit.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.util.Vector;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.potion.Potion;
import org.bukkit.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import net.citizensnpcs.util.PlayerAnimation;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import us.zonix.practice.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import us.zonix.practice.kit.Kit;
import org.bukkit.plugin.Plugin;
import us.zonix.practice.Practice;
import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.List;
import java.util.Random;
import org.bukkit.scheduler.BukkitRunnable;

public class BotMechanics extends BukkitRunnable
{
    private ZonixBot zonixBot;
    private ZonixBot.BotDifficulty difficulty;
    private Random random;
    private List<UUID> players;
    private boolean kit;
    private boolean navigation;
    private boolean selfHealing;
    private double attackRange;
    private double swingRangeModifier;
    private Player target;
    
    public BotMechanics(final ZonixBot zonixBot, final List<UUID> players, final ZonixBot.BotDifficulty difficulty) {
        this.random = new Random();
        this.target = null;
        this.setupZonixBot(zonixBot, players, difficulty);
    }
    
    private void setupZonixBot(final ZonixBot zonixBot, final List<UUID> players, final ZonixBot.BotDifficulty difficulty) {
        this.zonixBot = zonixBot;
        this.players = players;
        this.difficulty = difficulty;
        this.startupBotMechanics();
    }
    
    private void startupBotMechanics() {
        int delay = 2;
        this.attackRange = 3.2;
        if (this.difficulty == ZonixBot.BotDifficulty.EASY) {
            this.attackRange *= 0.8;
            this.swingRangeModifier = -0.5;
        }
        else if (this.difficulty == ZonixBot.BotDifficulty.HARD) {
            this.attackRange *= 2.0;
            this.swingRangeModifier = 2.0;
        }
        else if (this.difficulty == ZonixBot.BotDifficulty.EXPERT) {
            this.attackRange *= 2.6;
            this.swingRangeModifier = 3.0;
            delay = 1;
        }
        this.runTaskTimerAsynchronously((Plugin)Practice.getInstance(), 60L, (long)delay);
    }
    
    private void giveKit(final Kit kit) {
        kit.applyToNPC(this.zonixBot);
        this.kit = true;
    }
    
    private void attemptToHeal() {
        if (this.selfHealing) {
            return;
        }
        final Damageable d = (Damageable)this.zonixBot.getBukkitEntity();
        if (d.getHealth() <= 13.0 && this.random.nextBoolean() && !this.splashPotion() && !this.useSoupRefill()) {
            this.useGoldenApple();
        }
    }
    
    private boolean useGoldenApple() {
        ItemStack gapple = null;
        for (final ItemStack is : this.zonixBot.getBukkitEntity().getInventory().getContents()) {
            if (is != null && is.getType() == Material.GOLDEN_APPLE) {
                gapple = is.clone();
            }
        }
        if (gapple != null) {
            this.selfHealing = true;
            final ItemStack finalGapple = gapple;
            ItemStack hand = null;
            for (int i = 0; i < 9; ++i) {
                if (this.zonixBot.getBukkitEntity().getInventory().getItem(i) != null && this.zonixBot.getBukkitEntity().getInventory().getItem(i).equals((Object)gapple)) {
                    hand = this.zonixBot.getBukkitEntity().getInventory().getItem(i);
                    this.zonixBot.getBukkitEntity().getInventory().setHeldItemSlot(i);
                    break;
                }
            }
            if (hand == null) {
                this.zonixBot.getBukkitEntity().getInventory().setHeldItemSlot(1);
                ItemUtil.removeItems((Inventory)this.zonixBot.getBukkitEntity().getInventory(), finalGapple, 1);
                for (int i = 9; i < 36; ++i) {
                    if (this.zonixBot.getBukkitEntity().getInventory().getItem(i) == null || this.zonixBot.getBukkitEntity().getInventory().getItem(i).getType() == Material.AIR) {
                        this.zonixBot.getBukkitEntity().getInventory().setItem(i, this.zonixBot.getBukkitEntity().getItemInHand());
                        break;
                    }
                }
                this.zonixBot.getBukkitEntity().setItemInHand(gapple);
            }
            new BukkitRunnable() {
                public void run() {
                    BotMechanics.this.zonixBot.getBukkitEntity().setItemInHand(finalGapple);
                    try {
                        final Class<?> clz = PlayerAnimation.class;
                        clz.getField("START_USE_MAINHAND_ITEM");
                        try {
                            PlayerAnimation.START_USE_MAINHAND_ITEM.play(BotMechanics.this.zonixBot.getBukkitEntity());
                        }
                        catch (NoSuchFieldError noSuchFieldError) {}
                        clz.getField("EAT_FOOD");
                        try {
                            PlayerAnimation.EAT_FOOD.play(BotMechanics.this.zonixBot.getBukkitEntity());
                        }
                        catch (NoSuchFieldError noSuchFieldError2) {}
                    }
                    catch (Exception ex) {}
                    new BukkitRunnable() {
                        public void run() {
                            if (BotMechanics.this.zonixBot.getBukkitEntity() != null) {
                                BotMechanics.this.zonixBot.getNpc().getNavigator().setPaused(true);
                                BotMechanics.this.zonixBot.getBukkitEntity().setItemInHand(new ItemStack(Material.AIR));
                                finalGapple.setAmount(1);
                                if (finalGapple.getDurability() == 0) {
                                    BotMechanics.this.zonixBot.getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 1));
                                    BotMechanics.this.zonixBot.getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 1));
                                }
                                else {
                                    BotMechanics.this.zonixBot.getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 4));
                                    BotMechanics.this.zonixBot.getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 1));
                                    BotMechanics.this.zonixBot.getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 6000, 0));
                                    BotMechanics.this.zonixBot.getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 6000, 0));
                                }
                                ItemUtil.removeItems((Inventory)BotMechanics.this.zonixBot.getBukkitEntity().getInventory(), finalGapple, finalGapple.getAmount());
                                new BukkitRunnable() {
                                    public void run() {
                                        if (BotMechanics.this.zonixBot.getNpc() != null && BotMechanics.this.zonixBot.isSpawned() && BotMechanics.this.zonixBot.getNpc().getNavigator() != null) {
                                            BotMechanics.this.zonixBot.getNpc().getNavigator().setPaused(false);
                                            BotMechanics.this.zonixBot.getBukkitEntity().getInventory().setHeldItemSlot(0);
                                            BotMechanics.this.selfHealing = false;
                                        }
                                    }
                                }.runTaskLater((Plugin)Practice.getInstance(), (long)(BotMechanics.this.random.nextInt(4) + 2));
                            }
                        }
                    }.runTaskLater((Plugin)Practice.getInstance(), 35L);
                }
            }.runTaskLater((Plugin)Practice.getInstance(), (long)(this.random.nextInt(2) + 1));
            return true;
        }
        return false;
    }
    
    private boolean useSoupRefill() {
        ItemStack soup = null;
        for (final ItemStack is : this.zonixBot.getBukkitEntity().getInventory().getContents()) {
            if (is != null && is.getType() == Material.MUSHROOM_SOUP) {
                soup = is.clone();
            }
        }
        if (soup != null) {
            this.selfHealing = true;
            this.zonixBot.getNpc().getNavigator().setPaused(true);
            final ItemStack finalSoup = soup;
            ItemStack hand = null;
            for (int i = 0; i < 9; ++i) {
                if (this.zonixBot.getBukkitEntity().getInventory().getItem(i) != null && this.zonixBot.getBukkitEntity().getInventory().getItem(i).equals((Object)soup)) {
                    hand = this.zonixBot.getBukkitEntity().getInventory().getItem(i);
                    this.zonixBot.getBukkitEntity().getInventory().setHeldItemSlot(i);
                    break;
                }
            }
            if (hand == null) {
                this.zonixBot.getBukkitEntity().getInventory().setHeldItemSlot(1);
                ItemUtil.removeItems((Inventory)this.zonixBot.getBukkitEntity().getInventory(), finalSoup, 1);
                for (int i = 9; i < 36; ++i) {
                    if (this.zonixBot.getBukkitEntity().getInventory().getItem(i) == null || this.zonixBot.getBukkitEntity().getInventory().getItem(i).getType() == Material.AIR) {
                        this.zonixBot.getBukkitEntity().getInventory().setItem(i, this.zonixBot.getBukkitEntity().getItemInHand());
                        break;
                    }
                }
                this.zonixBot.getBukkitEntity().setItemInHand(soup);
            }
            this.zonixBot.getBukkitEntity().setItemInHand(finalSoup);
            new BukkitRunnable() {
                public void run() {
                    if (BotMechanics.this.zonixBot == null || BotMechanics.this.zonixBot.getBukkitEntity() == null || BotMechanics.this.zonixBot.getBukkitEntity().isDead() || finalSoup == null) {
                        return;
                    }
                    ItemUtil.removeItems((Inventory)BotMechanics.this.zonixBot.getBukkitEntity().getInventory(), finalSoup, finalSoup.getAmount());
                    if (!BotMechanics.this.zonixBot.getBukkitEntity().isDead()) {
                        final Damageable d = (Damageable)BotMechanics.this.zonixBot.getBukkitEntity();
                        d.setHealth((d.getHealth() < 13.0) ? (d.getHealth() + 7.0) : 20.0);
                        final Class<?> clz = PlayerAnimation.class;
                        try {
                            clz.getField("START_USE_MAINHAND_ITEM");
                            try {
                                PlayerAnimation.START_USE_MAINHAND_ITEM.play(BotMechanics.this.zonixBot.getBukkitEntity());
                            }
                            catch (NoSuchFieldError noSuchFieldError) {}
                        }
                        catch (Exception ex) {}
                        BotMechanics.this.zonixBot.getBukkitEntity().setItemInHand(new ItemStack(Material.BOWL));
                        new BukkitRunnable() {
                            public void run() {
                                if (BotMechanics.this.zonixBot.getNpc() != null && BotMechanics.this.zonixBot.isSpawned() && BotMechanics.this.zonixBot.getNpc().getNavigator() != null) {
                                    BotMechanics.this.zonixBot.getNpc().getNavigator().setPaused(false);
                                    final ItemStack is = BotMechanics.this.zonixBot.getBukkitEntity().getItemInHand();
                                    if (is != null) {
                                        BotMechanics.this.zonixBot.getBukkitEntity().setItemInHand(new ItemStack(Material.AIR));
                                        BotMechanics.this.zonixBot.getBukkitEntity().getInventory().setHeldItemSlot(0);
                                        BotMechanics.this.selfHealing = false;
                                    }
                                }
                            }
                        }.runTaskLater((Plugin)Practice.getInstance(), (long)(BotMechanics.this.random.nextInt(1) + 1));
                    }
                }
            }.runTaskLater((Plugin)Practice.getInstance(), (long)(this.random.nextInt(1) + 1));
            return true;
        }
        return false;
    }
    
    private boolean splashPotion() {
        if (!this.zonixBot.getBukkitEntity().isOnGround() && this.zonixBot.getBukkitEntity().getLocation().getY() - this.zonixBot.getBukkitEntity().getLocation().getBlockY() > 0.35 && this.random.nextInt(3) == 0) {
            return false;
        }
        ItemStack pot = null;
        for (final ItemStack is : this.zonixBot.getBukkitEntity().getInventory().getContents()) {
            if (is != null && ((is.getType() == Material.POTION && (is.getDurability() == 16421 || is.getDurability() == 16453)) || is.getDurability() == 438)) {
                pot = is.clone();
                break;
            }
        }
        if (pot != null) {
            this.selfHealing = true;
            final ItemStack finalPot = pot;
            ItemStack hand = null;
            for (int i = 0; i < 9; ++i) {
                if (this.zonixBot.getBukkitEntity().getInventory().getItem(i) != null && this.zonixBot.getBukkitEntity().getInventory().getItem(i).equals((Object)pot)) {
                    hand = this.zonixBot.getBukkitEntity().getInventory().getItem(i);
                    this.zonixBot.getBukkitEntity().getInventory().setHeldItemSlot(i);
                    break;
                }
            }
            if (hand == null) {
                this.zonixBot.getBukkitEntity().getInventory().setHeldItemSlot(1);
                ItemUtil.removeItems((Inventory)this.zonixBot.getBukkitEntity().getInventory(), finalPot, 1);
                for (int i = 9; i < 36; ++i) {
                    if (this.zonixBot.getBukkitEntity().getInventory().getItem(i) == null || this.zonixBot.getBukkitEntity().getInventory().getItem(i).getType() == Material.AIR) {
                        this.zonixBot.getBukkitEntity().getInventory().setItem(i, this.zonixBot.getBukkitEntity().getItemInHand());
                        this.zonixBot.getBukkitEntity().getInventory().setHeldItemSlot(1);
                        break;
                    }
                }
                this.zonixBot.getBukkitEntity().setItemInHand(pot);
            }
            this.zonixBot.getBukkitEntity().getInventory().setHeldItemSlot(this.random.nextInt(8) + 1);
            this.zonixBot.getBukkitEntity().setItemInHand(finalPot);
            final Location behind = this.zonixBot.getBukkitEntity().getLocation().add(this.zonixBot.getBukkitEntity().getLocation().getDirection().normalize().multiply(-5)).subtract(0.0, 10.0, 0.0);
            this.zonixBot.getNpc().getNavigator().setTarget(behind);
            new BukkitRunnable() {
                int targetCounter = BotMechanics.this.random.nextInt(5) + 5;
                int counter = this.targetCounter;
                
                public void run() {
                    if (BotMechanics.this.zonixBot.getNpc() != null && BotMechanics.this.zonixBot.isSpawned() && BotMechanics.this.zonixBot.getNpc().getNavigator() != null) {
                        --this.counter;
                        if (this.counter == 0 || Math.abs(BotMechanics.this.zonixBot.getBukkitEntity().getLocation().getPitch() - 90.0f) < 50.0f) {
                            this.cancel();
                            BotMechanics.this.zonixBot.swing();
                            final ThrownPotion thrownPotion = BotMechanics.this.getThrownPotion(finalPot);
                            new BukkitRunnable() {
                                public void run() {
                                    if (BotMechanics.this.selfHealing && thrownPotion != null && BotMechanics.this.zonixBot.getNpc() != null && BotMechanics.this.zonixBot.getNpc().isSpawned() && !BotMechanics.this.zonixBot.getBukkitEntity().isDead() && !thrownPotion.isDead()) {
                                        BotMechanics.this.zonixBot.getNpc().getNavigator().setTarget(thrownPotion.getLocation());
                                    }
                                    else {
                                        this.cancel();
                                    }
                                }
                            }.runTaskTimer((Plugin)Practice.getInstance(), 1L, 1L);
                            BotMechanics.this.zonixBot.getBukkitEntity().setItemInHand(new ItemStack(Material.AIR));
                            ItemUtil.removeItems((Inventory)BotMechanics.this.zonixBot.getBukkitEntity().getInventory(), finalPot, 1);
                            final Damageable d = (Damageable)BotMechanics.this.zonixBot.getBukkitEntity();
                            if (d.getHealth() < 12.0) {
                                ItemStack pot = null;
                                for (final ItemStack is : BotMechanics.this.zonixBot.getBukkitEntity().getInventory().getContents()) {
                                    if (is != null && is.getType() == Material.POTION && (is.getDurability() == 16421 || is.getDurability() == 16453)) {
                                        pot = is.clone();
                                        break;
                                    }
                                }
                                if (pot != null) {
                                    BotMechanics.this.zonixBot.swing();
                                    BotMechanics.this.getThrownPotion(pot);
                                    ItemUtil.removeItems((Inventory)BotMechanics.this.zonixBot.getBukkitEntity().getInventory(), pot, 1);
                                }
                            }
                            new BukkitRunnable() {
                                public void run() {
                                    if (BotMechanics.this.zonixBot.getNpc() != null && BotMechanics.this.zonixBot.isSpawned() && BotMechanics.this.zonixBot.getNpc().getNavigator() != null) {
                                        BotMechanics.this.zonixBot.getBukkitEntity().getInventory().setHeldItemSlot(0);
                                        BotMechanics.this.selfHealing = false;
                                    }
                                }
                            }.runTaskLater((Plugin)Practice.getInstance(), (long)(BotMechanics.this.random.nextInt(12) + 8));
                        }
                    }
                    else {
                        this.cancel();
                    }
                }
            }.runTaskTimer((Plugin)Practice.getInstance(), 1L, 1L);
            return true;
        }
        return this.zonixBot.isDestroyed();
    }
    
    private ThrownPotion getThrownPotion(final ItemStack potion) {
        final ThrownPotion thrownPotion = (ThrownPotion)this.zonixBot.getBukkitEntity().getWorld().spawnEntity(this.zonixBot.getBukkitEntity().getLocation(), EntityType.SPLASH_POTION);
        thrownPotion.getEffects().addAll(Potion.fromItemStack(potion).getEffects());
        thrownPotion.setShooter((ProjectileSource)this.zonixBot.getBukkitEntity());
        thrownPotion.setItem(potion);
        final Vector vec = this.zonixBot.getBukkitEntity().getLocation().getDirection();
        if (vec.getY() == 0.0) {
            vec.setY(-this.random.nextInt(2) + 1 + this.random.nextDouble() / 10.0);
        }
        thrownPotion.setVelocity(vec);
        return thrownPotion;
    }
    
    public void run() {
        if (this.players == null || this.players.isEmpty() || this.zonixBot.isDestroyed()) {
            this.cancel();
            return;
        }
        if (this.zonixBot.isSpawned() && this.zonixBot.getBukkitEntity() != null) {
            if (!this.kit) {
                this.zonixBot.getNpc().setProtected(false);
                this.giveKit(this.zonixBot.getKit());
            }
            if (!this.navigation) {
                this.navigation = true;
                if (this.difficulty == ZonixBot.BotDifficulty.HARD) {
                    this.zonixBot.getNpc().getNavigator().getLocalParameters().speedModifier(1.33f);
                }
                else if (this.difficulty == ZonixBot.BotDifficulty.EXPERT) {
                    this.zonixBot.getNpc().getNavigator().getLocalParameters().speedModifier(1.66f);
                }
                this.zonixBot.getNpc().getNavigator().getLocalParameters().attackRange(this.attackRange);
                this.zonixBot.getNpc().getNavigator().getLocalParameters().stuckAction((npc, navigationgator) -> false);
            }
            if (!this.zonixBot.getBukkitEntity().isDead() && this.zonixBot.getBukkitEntity().getLocation().getBlockY() < 0) {
                this.zonixBot.getBukkitEntity().setHealth(0.0);
                return;
            }
            if (this.zonixBot.getBukkitEntity().getVelocity().getY() < 0.1 && this.zonixBot.getBukkitEntity().getVelocity().getY() > -0.0784) {
                final Vector v = this.zonixBot.getNpc().getEntity().getVelocity();
                this.zonixBot.getNpc().getEntity().setVelocity(v.setY(-0.0784));
            }
            double distance = (this.target != null && this.target.getWorld().getName().equals(this.zonixBot.getBukkitEntity().getWorld().getName())) ? this.target.getLocation().distanceSquared(this.zonixBot.getBukkitEntity().getLocation()) : 22500.0;
            if (this.zonixBot.getNpc().getNavigator().getTargetAsLocation() == null || this.random.nextInt(10) == 0) {
                for (final UUID uuid : this.players) {
                    final Player pl = Bukkit.getPlayer(uuid);
                    if (pl != null && pl.getWorld().getName().equals(this.zonixBot.getBukkitEntity().getWorld().getName())) {
                        final double dis = this.zonixBot.getBukkitEntity().getLocation().distanceSquared(pl.getLocation());
                        if (dis >= distance) {
                            continue;
                        }
                        this.target = pl;
                        distance = dis;
                    }
                }
            }
            if (this.target != null && !this.selfHealing) {
                if (distance <= this.attackRange * this.attackRange * 1.5 && this.random.nextDouble() > 0.2) {
                    this.zonixBot.getNpc().getNavigator().setTarget((Entity)this.target, true);
                }
                else {
                    this.zonixBot.getNpc().getNavigator().setTarget(this.target.getLocation());
                }
                this.zonixBot.getNpc().getNavigator().setPaused(false);
            }
            if (this.zonixBot.getNpc().getNavigator().getTargetAsLocation() != null) {
                this.zonixBot.getBukkitEntity().setSprinting(true);
            }
            final double x = this.attackRange + this.swingRangeModifier + this.random.nextDouble() * 3.0;
            if (distance < x * x && !this.zonixBot.getNpc().getNavigator().isPaused() && !this.selfHealing) {
                this.zonixBot.swing();
            }
            if (!this.zonixBot.getBukkitEntity().isDead()) {
                this.attemptToHeal();
            }
        }
    }
}
