package vip.megumin.anniPro.anniGame.autoRespawn;

import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand.EnumClientCommand;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class SpigotRespawnPacket implements RespawnPacket
{
    private final PacketPlayInClientCommand packet;

    public SpigotRespawnPacket()
    {
        packet = new PacketPlayInClientCommand(EnumClientCommand.PERFORM_RESPAWN);
    }

    @Override
    public void sendToPlayer(final Player player)
    {
        CraftPlayer p = (CraftPlayer) player;
        p.getHandle().playerConnection.a(packet);
    }
}
