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
package org.lanternpowered.server.network.forge.message.codec.handshake;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CodecException;
import org.lanternpowered.server.network.forge.handshake.ForgeClientHandshakePhase;
import org.lanternpowered.server.network.forge.handshake.ForgeServerHandshakePhase;
import org.lanternpowered.server.network.forge.message.type.handshake.MessageForgeHandshakeInOutAck;
import org.lanternpowered.server.network.forge.message.type.handshake.MessageForgeHandshakeInOutHello;
import org.lanternpowered.server.network.forge.message.type.handshake.MessageForgeHandshakeInOutModList;
import org.lanternpowered.server.network.forge.message.type.handshake.MessageForgeHandshakeOutReset;
import org.lanternpowered.server.network.message.Message;
import org.lanternpowered.server.network.message.codec.CodecContext;
import org.lanternpowered.server.network.message.codec.serializer.Types;
import org.lanternpowered.server.network.vanilla.message.codec.play.AbstractCodecPlayInOutCustomPayload;

import java.util.Map;

public final class CodecPlayInOutCustomPayload extends AbstractCodecPlayInOutCustomPayload {

    static final int FML_HANDSHAKE_SERVER_HELLO = 0;
    static final int FML_HANDSHAKE_CLIENT_HELLO = 1;
    static final int FML_HANDSHAKE_MOD_LIST = 2;
    static final int FML_HANDSHAKE_REGISTRY_DATA = 3;
    static final int FML_HANDSHAKE_ACK = -1;
    static final int FML_HANDSHAKE_RESET = -2;

    // We will still use protocol 1, so we don't have to send
    // the overridden dimension id (maybe once we add support for that)
    // we could change it to 2 or higher future versions
    private final static int FORGE_PROTOCOL = 1;

    @Override
    protected MessageResult encode0(CodecContext context, Message message) throws CodecException {
        if (message instanceof MessageForgeHandshakeInOutAck) {
            return new MessageResult("FML|HS", context.byteBufAlloc()
                    .buffer(2)
                    .writeByte(FML_HANDSHAKE_ACK)
                    // Only the server state should be send to the client
                    .writeByte(((ForgeServerHandshakePhase) ((MessageForgeHandshakeInOutAck) message).getPhase()).ordinal()));
        } else if (message instanceof MessageForgeHandshakeInOutHello) {
            return new MessageResult("FML|HS", context.byteBufAlloc()
                    .buffer(2)
                    .writeByte(FML_HANDSHAKE_SERVER_HELLO)
                    .writeByte(FORGE_PROTOCOL));
        } else if (message instanceof MessageForgeHandshakeInOutModList) {
            Map<String, String> entries = ((MessageForgeHandshakeInOutModList) message).getEntries();
            ByteBuf buf = context.byteBufAlloc().buffer();
            buf.writeByte(FML_HANDSHAKE_MOD_LIST);
            context.writeVarInt(buf, entries.size());
            for (Map.Entry<String, String> en : entries.entrySet()) {
                context.write(buf, Types.STRING, en.getKey());
                context.write(buf, Types.STRING, en.getValue());
            }
            return new MessageResult("FML|HS", buf);
        } else if (message instanceof MessageForgeHandshakeOutReset) {
            return new MessageResult("FML|HS", context.byteBufAlloc()
                    .buffer(1).writeByte(FML_HANDSHAKE_RESET));
        }
        return null;
    }

    @Override
    protected Message decode0(CodecContext context, String channel, ByteBuf content) throws CodecException {
        if ("FML|HS".equals(channel)) {
            int type = content.readByte();
            switch (type) {
                case FML_HANDSHAKE_RESET:
                    // server -> client message: ignore
                    break;
                case FML_HANDSHAKE_ACK:
                    ForgeClientHandshakePhase phase = ForgeClientHandshakePhase.values()[content.readByte()];
                    return new MessageForgeHandshakeInOutAck(phase);
                case FML_HANDSHAKE_SERVER_HELLO:
                    // server -> client message: ignore
                    break;
                case FML_HANDSHAKE_CLIENT_HELLO:
                    content.readByte(); // The forge protocol version on the client
                    return new MessageForgeHandshakeInOutHello();
                case FML_HANDSHAKE_MOD_LIST:
                    int size = context.readVarInt(content);
                    Map<String, String> entries = Maps.newHashMapWithExpectedSize(size);
                    for (int i = 0; i < size; i++) {
                        entries.put(context.read(content, Types.STRING), context.read(content, Types.STRING));
                    }
                    return new MessageForgeHandshakeInOutModList(entries);
                case FML_HANDSHAKE_REGISTRY_DATA:
                    // server -> client message: ignore
                    break;
                default:
                    throw new CodecException("Unknown forge handshake message with opcode: " + type);
            }
            throw new CodecException("Received an unexpected forge message with opcode: " + type);
        } else {
            throw new CodecException("Received an unexpected message with channel: " + channel);
        }
    }

}
