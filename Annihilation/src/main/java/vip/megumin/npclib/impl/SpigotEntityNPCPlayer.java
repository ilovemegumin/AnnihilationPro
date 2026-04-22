package vip.megumin.npclib.impl;

import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldSettings.EnumGamemode;
import vip.megumin.npclib.api.NPC;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

class SpigotEntityNPCPlayer extends EntityPlayer
{
    private NPC npc;

    SpigotEntityNPCPlayer(Player player, Location location, NPC npc)
    {
        super(SpigotNms.getServer(), SpigotNms.getHandle(location.getWorld()), ((CraftPlayer) player).getProfile(), new PlayerInteractManager(SpigotNms.getHandle(location.getWorld())));
        this.npc = npc;
        playerInteractManager.b(EnumGamemode.SURVIVAL);
        playerConnection = new SpigotNPCConnection(this);
        setPosition(location.getX(), location.getY(), location.getZ());
    }

    @Override
    public boolean damageEntity(DamageSource source, float damage)
    {
        Player player = null;
        if (source.getEntity() != null && source.getEntity().getBukkitEntity().getType() == EntityType.PLAYER)
            player = (Player) source.getEntity().getBukkitEntity();
        if (npc.onKill(player))
            npc = null;
        return false;
    }
}
