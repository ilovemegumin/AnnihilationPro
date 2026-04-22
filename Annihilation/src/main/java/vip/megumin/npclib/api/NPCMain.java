package vip.megumin.npclib.api;

import vip.megumin.anniPro.anniGame.AnniPlayer;
import vip.megumin.anniPro.anniGame.Game;
import vip.megumin.anniPro.main.AnnihilationMain;
import vip.megumin.anniPro.main.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NPCMain implements Listener
{
    private static NPCMain instance;
    public static void registerLogoutPrevention(Plugin plugin)
    {
        if(instance == null)
        {
            instance = new NPCMain();
            Bukkit.getPluginManager().registerEvents(instance,plugin);
        }
    }

    private final Map<UUID,LogoutTag> taggedPlayers;
    private NPCMain()
    {
        taggedPlayers = new HashMap<>();
    }

    public void tagPlayer(Player player)
    {
        if(!isTagged(player.getUniqueId()))
        {
            LogoutTag tag = new LogoutTag(player);
            taggedPlayers.put(player.getUniqueId(),tag);
        }
    }

    public boolean isTagged(UUID id)
    {
        return taggedPlayers.containsKey(id);
    }

    public void removeTag(UUID id)
    {
        this.taggedPlayers.remove(id).close();
    }
    @EventHandler(priority = EventPriority.LOW)
    public void onLogin(PlayerLoginEvent event)
    {
        if(isTagged(event.getPlayer().getUniqueId()))
        {
            Boolean b = taggedPlayers.get(event.getPlayer().getUniqueId()).wasKilled();
            if(b == null)
            {
                event.setResult(Result.KICK_OTHER);
                event.setKickMessage(Lang.NPCALIVE.toString());
            }
        }
    }
    @EventHandler(priority = EventPriority.LOW)
    public void onLogin(PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();
        if(isTagged(player.getUniqueId()))
        {
            final LogoutTag tag = taggedPlayers.get(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(AnnihilationMain.getInstance(), new Runnable()
            {
                @Override
                public void run()
                {
                    AnniPlayer p = AnniPlayer.getPlayer(player.getUniqueId());
                    if(p.getTeam().isTeamDead() || tag.wasKilled())
                    {
                        player.getInventory().clear();
                        player.getInventory().setArmorContents(null);
                        player.setHealth(0);
                        player.sendMessage(Lang.NPCDEATH.toString());
                    }
                    else
                    {
                        player.teleport(p.getTeam().getRandomSpawn());
                        player.sendMessage(Lang.NPCSURVIVE.toString());
                    }
                    removeTag(player.getUniqueId());
                }
            },5);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent event)
    {
        playerLeft(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerKickEvent event)
    {
        playerLeft(event.getPlayer());
    }

    private void playerLeft(Player player)
    {
        if(Game.isGameRunning())
        {
            AnniPlayer p = AnniPlayer.getPlayer(player.getUniqueId());
            if(p != null && p.getTeam() != null) //They are in a team
            {
                p.getKit().cleanup(player);
                tagPlayer(player);
            }
        }
    }

}
