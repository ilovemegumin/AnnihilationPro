package vip.megumin.npclib.impl;

import net.minecraft.server.v1_8_R3.EnumProtocolDirection;
import net.minecraft.server.v1_8_R3.NetworkManager;
import vip.megumin.npclib.util.ReflectUtil;

import java.lang.reflect.Field;

class SpigotNPCNetworkManager extends NetworkManager
{
    SpigotNPCNetworkManager()
    {
        super(EnumProtocolDirection.CLIENTBOUND);
        Field channel = ReflectUtil.makeField(NetworkManager.class, "k");
        Field address = ReflectUtil.makeField(NetworkManager.class, "l");
        ReflectUtil.setField(channel, this, new NullChannel());
        ReflectUtil.setField(address, this, new NullSocketAddress());
    }
}
