package vip.megumin.npclib.impl;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R3.WorldServer;
import vip.megumin.npclib.api.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import vip.megumin.npclib.api.NMS;

public class SpigotNms implements NMS
{
    @Override
    public void onDespawn(NPC npc)
    {
        sendPacketsTo(Bukkit.getOnlinePlayers(), new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, getHandle((Player) npc.getEntity())));
        WorldServer world = getHandle(npc.getLocation().getWorld());
        world.removeEntity(getHandle(npc.getEntity()));
    }

    @Override
    public Player spawnPlayer(Player realPlayer, Location location, NPC npc)
    {
        SpigotEntityNPCPlayer player = new SpigotEntityNPCPlayer(realPlayer, location, npc);
        sendPacketsTo(Bukkit.getOnlinePlayers(), new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, player));
        WorldServer world = getHandle(location.getWorld());
        world.addEntity(player);
        return player.getBukkitEntity();
    }

    public static Entity getHandle(org.bukkit.entity.Entity bukkitEntity)
    {
        if (!(bukkitEntity instanceof CraftEntity))
            return null;
        return ((CraftEntity) bukkitEntity).getHandle();
    }

    public static EntityHuman getHandle(HumanEntity bukkitHuman)
    {
        if (!(bukkitHuman instanceof CraftHumanEntity))
            return null;
        return ((CraftHumanEntity) bukkitHuman).getHandle();
    }

    public static EntityPlayer getHandle(Player bukkitPlayer)
    {
        if (!(bukkitPlayer instanceof CraftPlayer))
            return null;
        return ((CraftPlayer) bukkitPlayer).getHandle();
    }

    public static MinecraftServer getHandle(org.bukkit.Server bukkitServer)
    {
        if (bukkitServer instanceof CraftServer)
            return ((CraftServer) bukkitServer).getServer();
        return null;
    }

    public static WorldServer getHandle(org.bukkit.World bukkitWorld)
    {
        if (bukkitWorld instanceof CraftWorld)
            return ((CraftWorld) bukkitWorld).getHandle();
        return null;
    }

    public static MinecraftServer getServer()
    {
        return getHandle(Bukkit.getServer());
    }

    public void sendPacketsTo(Iterable<? extends Player> recipients, Packet... packets)
    {
        Iterable<EntityPlayer> nmsRecipients = Iterables.transform(recipients, new Function<Player, EntityPlayer>()
        {
            @Override
            public EntityPlayer apply(Player input)
            {
                return getHandle(input);
            }
        });
        for (EntityPlayer recipient : nmsRecipients)
        {
            if (recipient == null)
                continue;
            for (Packet packet : packets)
            {
                if (packet == null)
                    continue;
                recipient.playerConnection.sendPacket(packet);
            }
        }
    }
}
