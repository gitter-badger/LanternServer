/*
 * This file is part of LanternServer, licensed under the MIT License (MIT).
 *
 * Copyright (c) LanternPowered <https://github.com/LanternPowered>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.lanternpowered.server.network.rcon;

import com.google.common.collect.Maps;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;

public class RconServer extends BaseRconService {

    private final Map<String, RconSource> sourcesByHostname = Maps.newConcurrentMap();

    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private InetSocketAddress address;

    public RconServer(String password) {
        super(password);
    }

    /**
     * Initializes the rcon server.
     * 
     * @param address the address to bind to
     * @return the channel future
     */
    public ChannelFuture bind(final InetSocketAddress address) {
        if (this.bootstrap != null) {
            throw new IllegalStateException("The rcon server is already active.");
        }

        this.address = address;
        this.bootstrap = new ServerBootstrap();
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();

        return this.bootstrap
                .group(this.bossGroup, this.workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new RconFramingHandler())
                                .addLast(new RconHandler(RconServer.this, password));
                    }

                })
                .bind(address);
    }

    /**
     * Shut the Rcon server down.
     */
    public void shutdown() {
        if (this.bootstrap == null) {
            throw new IllegalStateException("The rcon server is not active.");
        }
        this.workerGroup.shutdownGracefully();
        this.bossGroup.shutdownGracefully();
        this.workerGroup = null;
        this.bossGroup = null;
        this.bootstrap = null;
    }

    /**
     * Creates a new rcon source.
     * 
     * @param channel the channel
     * @return the rcon source
     */
    public RconSource newSource(Channel channel) {
        return new RconSource(new RconConnection((InetSocketAddress) channel.remoteAddress(), this.address));
    }

    /**
     * Called when the channel becomes active.
     * 
     * @param channel the channel
     * @param source the source
     */
    public void onChannelActive(Channel channel, RconSource source) {
        this.sourcesByHostname.put(source.getConnection().getAddress().getHostName(), source);
    }

    /**
     * Called when the channel becomes inactive.
     * 
     * @param channel the channel
     * @param source the source
     */
    public void onChannelInactive(Channel channel, RconSource source) {
        this.sourcesByHostname.remove(source.getConnection().getAddress().getHostName());
    }

    public Optional<RconSource> getByHostName(String hostname) {
        return Optional.ofNullable(this.sourcesByHostname.get(hostname));
    }

    @Override
    public boolean isRconEnabled() {
        return this.bootstrap != null;
    }

}
