package vip.megumin.npclib.impl;

import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.EventLoop;

import java.net.SocketAddress;

class NullChannel extends AbstractChannel
{
    NullChannel()
    {
        super(null);
    }

    @Override
    public ChannelConfig config()
    {
        return null;
    }

    @Override
    public boolean isActive()
    {
        return false;
    }

    @Override
    public boolean isOpen()
    {
        return false;
    }

    @Override
    public ChannelMetadata metadata()
    {
        return null;
    }

    @Override
    protected void doBeginRead()
    {
    }

    @Override
    protected void doBind(SocketAddress localAddress)
    {
    }

    @Override
    protected void doClose()
    {
    }

    @Override
    protected void doDisconnect()
    {
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer outboundBuffer)
    {
    }

    @Override
    protected boolean isCompatible(EventLoop eventLoop)
    {
        return false;
    }

    @Override
    protected SocketAddress localAddress0()
    {
        return null;
    }

    @Override
    protected AbstractUnsafe newUnsafe()
    {
        return null;
    }

    @Override
    protected SocketAddress remoteAddress0()
    {
        return null;
    }
}
