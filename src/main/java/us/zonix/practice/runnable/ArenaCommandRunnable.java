package us.zonix.practice.runnable;

import us.zonix.practice.arena.StandaloneArena;
import us.zonix.practice.CustomLocation;
import us.zonix.practice.arena.Arena;
import us.zonix.practice.Practice;

public class ArenaCommandRunnable implements Runnable
{
    private final Practice plugin;
    private final Arena copiedArena;
    private int times;
    
    @Override
    public void run() {
        this.duplicateArena(this.copiedArena, 10000, 10000);
    }
    
    private void duplicateArena(final Arena arena, final int offsetX, final int offsetZ) {
        new DuplicateArenaRunnable(this.plugin, arena, offsetX, offsetZ, 500, 500) {
            @Override
            public void onComplete() {
                final double minX = arena.getMin().getX() + this.getOffsetX();
                final double minZ = arena.getMin().getZ() + this.getOffsetZ();
                final double maxX = arena.getMax().getX() + this.getOffsetX();
                final double maxZ = arena.getMax().getZ() + this.getOffsetZ();
                final double aX = arena.getA().getX() + this.getOffsetX();
                final double aZ = arena.getA().getZ() + this.getOffsetZ();
                final double bX = arena.getB().getX() + this.getOffsetX();
                final double bZ = arena.getB().getZ() + this.getOffsetZ();
                final CustomLocation min = new CustomLocation(minX, arena.getMin().getY(), minZ, arena.getMin().getYaw(), arena.getMin().getPitch());
                final CustomLocation max = new CustomLocation(maxX, arena.getMax().getY(), maxZ, arena.getMax().getYaw(), arena.getMax().getPitch());
                final CustomLocation a = new CustomLocation(aX, arena.getA().getY(), aZ, arena.getA().getYaw(), arena.getA().getPitch());
                final CustomLocation b = new CustomLocation(bX, arena.getB().getY(), bZ, arena.getB().getYaw(), arena.getB().getPitch());
                final StandaloneArena standaloneArena = new StandaloneArena(a, b, min, max);
                arena.addStandaloneArena(standaloneArena);
                arena.addAvailableArena(standaloneArena);
                if (--ArenaCommandRunnable.this.times > 0) {
                    ArenaCommandRunnable.this.plugin.getServer().getLogger().info("Placed a standalone arena of " + arena.getName() + " at " + (int)minX + ", " + (int)minZ + ". " + ArenaCommandRunnable.this.times + " arenas remaining.");
                    ArenaCommandRunnable.this.duplicateArena(arena, (int)Math.round(maxX), (int)Math.round(maxZ));
                }
                else {
                    ArenaCommandRunnable.this.plugin.getServer().getLogger().info("Finished pasting " + ArenaCommandRunnable.this.copiedArena.getName() + "'s standalone arenas.");
                    ArenaCommandRunnable.this.plugin.getArenaManager().setGeneratingArenaRunnables(ArenaCommandRunnable.this.plugin.getArenaManager().getGeneratingArenaRunnables() - 1);
                    this.getPlugin().getArenaManager().reloadArenas();
                }
            }
        }.run();
    }
    
    public Practice getPlugin() {
        return this.plugin;
    }
    
    public Arena getCopiedArena() {
        return this.copiedArena;
    }
    
    public int getTimes() {
        return this.times;
    }
    
    public ArenaCommandRunnable(final Practice plugin, final Arena copiedArena, final int times) {
        this.plugin = plugin;
        this.copiedArena = copiedArena;
        this.times = times;
    }
}
