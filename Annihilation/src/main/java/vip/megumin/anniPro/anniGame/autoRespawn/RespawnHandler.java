package vip.megumin.anniPro.anniGame.autoRespawn;

import vip.megumin.anniPro.main.AnnihilationMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class RespawnHandler implements Listener
{
    private static RespawnHandler instance;
    public static void register(Plugin plugin)
    {
        if(instance == null)
        {
            instance = new RespawnHandler();
            Bukkit.getPluginManager().registerEvents(instance,plugin);
        }
    }

    private RespawnPacket packet;
    private RespawnHandler()
    {
        packet = new SpigotRespawnPacket();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void autoRespawn(PlayerDeathEvent event)
    {
        new AutoRespawnTask(event.getEntity()).runTaskLater(AnnihilationMain.getInstance(), 2L);
    }

    private class AutoRespawnTask extends BukkitRunnable
    {
        private Player player;
        public AutoRespawnTask(Player player)
        {
            this.player = player;
        }

        @Override
        public void run()
        {
            packet.sendToPlayer(player);
            player = null;
        }
    }
}
