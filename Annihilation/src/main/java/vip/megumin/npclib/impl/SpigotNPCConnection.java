package vip.megumin.npclib.impl;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PlayerConnection;

class SpigotNPCConnection extends PlayerConnection
{
    SpigotNPCConnection(SpigotEntityNPCPlayer npc)
    {
        super(SpigotNms.getServer(), new SpigotNPCNetworkManager(), npc);
    }

    @Override
    public void sendPacket(Packet packet)
    {
    }
}
