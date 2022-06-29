package us.zonix.practice.player;

import net.minecraft.server.v1_8_R3.PacketPlayInArmAnimation;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.plugin.Plugin;
import java.util.Iterator;
import org.bukkit.scheduler.BukkitRunnable;
import us.zonix.practice.Practice;
import net.edater.spigot.handler.PacketHandler;

public class CPSHandler implements PacketHandler
{
    private final Practice plugin;
    
    public CPSHandler(final Practice plugin) {
        this.plugin = plugin;
        new BukkitRunnable() {
            public void run() {
                for (final PlayerData current : plugin.getPlayerManager().getAllData()) {
                    current.setCps(current.getCurrentCps());
                    current.setCurrentCps(0);
                }
            }
        }.runTaskTimerAsynchronously((Plugin)plugin, 0L, 20L);
    }
    
    public void handleReceivedPacket(final PlayerConnection playerConnection, final Packet packet) {
        if (packet instanceof PacketPlayInArmAnimation) {
            final PlayerData playerData = this.plugin.getPlayerManager().getPlayerData(playerConnection.getPlayer().getUniqueId());
            playerData.setCurrentCps(playerData.getCurrentCps() + 1);
        }
    }
    
    public void handleSentPacket(final PlayerConnection playerConnection, final Packet packet) {
    }
}
