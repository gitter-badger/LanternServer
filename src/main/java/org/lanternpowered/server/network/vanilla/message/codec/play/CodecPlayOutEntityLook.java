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

import static org.lanternpowered.server.network.vanilla.message.codec.play.CodecUtils.wrapAngle;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CodecException;
import org.lanternpowered.server.network.message.codec.Codec;
import org.lanternpowered.server.network.message.codec.CodecContext;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayOutEntityLook;

public final class CodecPlayOutEntityLook implements Codec<MessagePlayOutEntityLook> {

    @Override
    public ByteBuf encode(CodecContext context, MessagePlayOutEntityLook message) throws CodecException {
        ByteBuf buf = context.byteBufAlloc().buffer();
        context.writeVarInt(buf, message.getEntityId());
        buf.writeByte(wrapAngle(message.getYaw()));
        buf.writeByte(wrapAngle(message.getPitch()));
        buf.writeBoolean(message.isOnGround());
        return buf;
    }

    @Override
    public MessagePlayOutEntityLook decode(CodecContext context, ByteBuf buf) throws CodecException {
        throw new CodecException();
    }
}
