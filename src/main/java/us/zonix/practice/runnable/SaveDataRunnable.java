package us.zonix.practice.runnable;

import java.util.Iterator;
import us.zonix.practice.player.PlayerData;
import us.zonix.practice.Practice;

public class SaveDataRunnable implements Runnable
{
    private final Practice plugin;
    
    @Override
    public void run() {
        for (final PlayerData playerData : this.plugin.getPlayerManager().getAllData()) {
            this.plugin.getPlayerManager().saveData(playerData);
        }
    }
    
    public SaveDataRunnable() {
        this.plugin = Practice.getInstance();
    }
}
