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

import com.flowpowered.math.vector.Vector3i;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CodecException;
import org.lanternpowered.server.network.message.codec.Codec;
import org.lanternpowered.server.network.message.codec.CodecContext;
import org.lanternpowered.server.network.message.codec.serializer.Types;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayInChangeSign;
import org.spongepowered.api.text.serializer.TextSerializers;

public final class CodecPlayInChangeSign implements Codec<MessagePlayInChangeSign> {

    @Override
    public ByteBuf encode(CodecContext context, MessagePlayInChangeSign message) throws CodecException {
        throw new CodecException();
    }

    @Override
    public MessagePlayInChangeSign decode(CodecContext context, ByteBuf buf) throws CodecException {
        Vector3i position = context.read(buf, Types.POSITION);
        String[] lines = new String[4];
        for (int i = 0; i < lines.length; i++) {
            // In the current protocol version are the lines send in json format,
            // this will change in 1.9
            // TODO: Limit length
            lines[i] = TextSerializers.PLAIN.serialize(TextSerializers.JSON.deserializeUnchecked(context.read(buf, Types.STRING)));
        }
        return new MessagePlayInChangeSign(position, lines);
    }
}
