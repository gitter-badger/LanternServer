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
package org.lanternpowered.server.network.vanilla.message.type.play;

import static com.google.common.base.Preconditions.checkNotNull;

import io.netty.buffer.ByteBuf;
import org.lanternpowered.server.network.message.Message;

public final class MessagePlayInOutChannelPayload implements Message {

    private final ByteBuf content;
    private final String channel;

    /**
     * Creates a new custom payload message.
     * 
     * @param channel the channel
     * @param content the content
     */
    public MessagePlayInOutChannelPayload(String channel, ByteBuf content) {
        this.channel = checkNotNull(channel, "channel");
        this.content = checkNotNull(content, "content");
    }

    /**
     * Gets the channel the plugin message is using.
     * 
     * @return the channel
     */
    public String getChannel() {
        return this.channel;
    }

    /**
     * Gets the content of the plugin message.
     * 
     * @return the content
     */
    public ByteBuf getContent() {
        return this.content;
    }

}
