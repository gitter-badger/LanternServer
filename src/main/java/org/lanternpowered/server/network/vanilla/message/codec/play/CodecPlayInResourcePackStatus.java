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
package org.lanternpowered.server.network.vanilla.message.codec.play;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CodecException;
import org.lanternpowered.server.network.message.codec.Codec;
import org.lanternpowered.server.network.message.codec.CodecContext;
import org.lanternpowered.server.network.message.codec.serializer.Types;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayInResourcePackStatus;
import org.spongepowered.api.event.entity.living.humanoid.player.ResourcePackStatusEvent.ResourcePackStatus;

import java.util.Map;

public final class CodecPlayInResourcePackStatus implements Codec<MessagePlayInResourcePackStatus> {

    private final Map<Integer, ResourcePackStatus> status = ImmutableMap.<Integer, ResourcePackStatus>builder()
            .put(0, ResourcePackStatus.SUCCESSFULLY_LOADED)
            .put(1, ResourcePackStatus.DECLINED)
            .put(2, ResourcePackStatus.FAILED)
            .put(3, ResourcePackStatus.ACCEPTED)
            .build();

    @Override
    public ByteBuf encode(CodecContext context, MessagePlayInResourcePackStatus message) throws CodecException {
        throw new CodecException();
    }

    @Override
    public MessagePlayInResourcePackStatus decode(CodecContext context, ByteBuf buf) throws CodecException {
        String hash = context.read(buf, Types.STRING);
        int status0 = context.readVarInt(buf);
        ResourcePackStatus status = this.status.get(status0);
        if (status == null) {
            throw new CodecException("Unknown status: " + status0);
        }
        return new MessagePlayInResourcePackStatus(hash, status);
    }
}
