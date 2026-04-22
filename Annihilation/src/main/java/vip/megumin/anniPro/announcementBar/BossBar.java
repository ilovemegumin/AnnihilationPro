package vip.megumin.anniPro.announcementBar;

import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.EntityEnderDragon;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import vip.megumin.anniPro.main.AnnihilationMain;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class BossBar implements Bar, Listener
{
    private static final float MAX_HEALTH = 200.0F;

    private final Map<UUID, DragonBar> bars = new HashMap<>();

    BossBar()
    {
        Bukkit.getPluginManager().registerEvents(this, AnnihilationMain.getInstance());
    }

    @Override
    public void sendToPlayer(final Player player, final String message, final float percentOfTotal)
    {
        DragonBar bar = bars.get(player.getUniqueId());
        if (bar == null)
        {
            bar = new DragonBar(player, cleanMessage(message));
            bars.put(player.getUniqueId(), bar);
            sendPacket(player, bar.createSpawnPacket());
        }

        bar.update(player, cleanMessage(message), clampPercent(percentOfTotal));
        sendPacket(player, bar.createMetadataPacket());
        sendPacket(player, bar.createTeleportPacket(player.getLocation()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        removeBar(event.getPlayer());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event)
    {
        removeBar(event.getPlayer());
    }

    private static void sendPacket(Player player, Packet<?> packet)
    {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        entityPlayer.playerConnection.sendPacket(packet);
    }

    private static float clampPercent(float percent)
    {
        if (percent < 0.0F)
            return 0.0F;
        if (percent > 1.0F)
            return 1.0F;
        return percent;
    }

    private static String cleanMessage(String message)
    {
        if (message.length() > 64)
            return message.substring(0, 63);
        return message;
    }

    private void removeBar(Player player)
    {
        DragonBar bar = bars.remove(player.getUniqueId());
        if (bar != null && player.isOnline())
        {
            sendPacket(player, bar.createDestroyPacket());
        }
    }

    private static final class DragonBar
    {
        private final EntityEnderDragon dragon;
        private String message;
        private float percent;

        private DragonBar(Player player, String message)
        {
            WorldServer world = ((CraftWorld) player.getWorld()).getHandle();
            this.dragon = new EntityEnderDragon(world);
            this.message = message;
            this.percent = 1.0F;
            apply(player.getLocation());
        }

        private void update(Player player, String message, float percent)
        {
            this.message = message;
            this.percent = percent;
            apply(player.getLocation());
        }

        private void apply(Location playerLocation)
        {
            Location dragonLocation = getDragonLocation(playerLocation);
            dragon.setLocation(dragonLocation.getX(), dragonLocation.getY(), dragonLocation.getZ(), dragonLocation.getYaw(), dragonLocation.getPitch());
            dragon.setInvisible(true);
            dragon.setCustomName(message);
            dragon.setCustomNameVisible(true);
            dragon.setHealth(Math.max(1.0F, percent * MAX_HEALTH));
        }

        private PacketPlayOutSpawnEntityLiving createSpawnPacket()
        {
            return new PacketPlayOutSpawnEntityLiving(dragon);
        }

        private PacketPlayOutEntityMetadata createMetadataPacket()
        {
            DataWatcher watcher = dragon.getDataWatcher();
            return new PacketPlayOutEntityMetadata(dragon.getId(), watcher, true);
        }

        private PacketPlayOutEntityTeleport createTeleportPacket(Location playerLocation)
        {
            Location dragonLocation = getDragonLocation(playerLocation);
            dragon.setLocation(dragonLocation.getX(), dragonLocation.getY(), dragonLocation.getZ(), dragonLocation.getYaw(), dragonLocation.getPitch());
            return new PacketPlayOutEntityTeleport(dragon);
        }

        private PacketPlayOutEntityDestroy createDestroyPacket()
        {
            return new PacketPlayOutEntityDestroy(dragon.getId());
        }

        private static Location getDragonLocation(Location playerLocation)
        {
            return playerLocation.clone().add(0.0D, -300.0D, 0.0D);
        }
    }
}
