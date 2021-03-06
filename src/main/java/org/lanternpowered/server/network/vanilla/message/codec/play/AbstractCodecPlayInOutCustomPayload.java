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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.CodecException;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.lanternpowered.server.network.message.Message;
import org.lanternpowered.server.network.message.NullMessage;
import org.lanternpowered.server.network.message.codec.Codec;
import org.lanternpowered.server.network.message.codec.CodecContext;
import org.lanternpowered.server.network.message.codec.serializer.Types;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayInOutChannelPayload;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayInOutRegisterChannels;
import org.lanternpowered.server.network.vanilla.message.type.play.MessagePlayInOutUnregisterChannels;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public abstract class AbstractCodecPlayInOutCustomPayload implements Codec<Message> {

    public static final AttributeKey<MultiPartMessage> FML_MULTI_PART_MESSAGE = AttributeKey.valueOf("fml-multi-part-message");

    @Override
    public ByteBuf encode(CodecContext context, Message message) throws CodecException {
        ByteBuf buf = context.byteBufAlloc().buffer();
        String channel;
        ByteBuf content;
        if (message instanceof MessagePlayInOutChannelPayload) {
            final MessagePlayInOutChannelPayload message1 = (MessagePlayInOutChannelPayload) message;
            content = message1.getContent();
            channel = message1.getChannel();
        } else if (message instanceof MessagePlayInOutRegisterChannels) {
            content = encodeChannels(((MessagePlayInOutRegisterChannels) message).getChannels());
            channel = "REGISTER";
        } else if (message instanceof MessagePlayInOutUnregisterChannels) {
            content = encodeChannels(((MessagePlayInOutUnregisterChannels) message).getChannels());
            channel = "UNREGISTER";
        } else {
            MessageResult result = this.encode0(context, message);
            if (result == null) {
                throw new CodecException("Unsupported message type: " + message.getClass().getName());
            }
            channel = result.channel;
            content = result.byteBuf;
        }
        context.write(buf, Types.STRING, channel);
        buf.writeBytes(content);
        return buf;
    }

    @Override
    public Message decode(CodecContext context, ByteBuf buf) throws CodecException {
        String channel = context.read(buf, Types.STRING);
        ByteBuf content = context.byteBufAlloc().heapBuffer(buf.readableBytes());
        buf.readBytes(content);
        if ("REGISTER".equals(channel)) {
            Set<String> channels = decodeChannels(content);
            Iterator<String> it = channels.iterator();
            while (it.hasNext()) {
                String channel0 = it.next();
                if (channel0.startsWith("FML")) {
                    it.remove();
                }
            }
            if (!channels.isEmpty()) {
                return new MessagePlayInOutRegisterChannels(channels);
            }
        } else if ("UNREGISTER".equals(channel)) {
            Set<String> channels = decodeChannels(content);
            Iterator<String> it = channels.iterator();
            while (it.hasNext()) {
                String channel0 = it.next();
                if (channel0.startsWith("FML")) {
                    it.remove();
                }
            }
            if (!channels.isEmpty()) {
                return new MessagePlayInOutUnregisterChannels(channels);
            }
        } else if ("FML|MP".equals(channel)) {
            Attribute<MultiPartMessage> attribute = context.getChannel().attr(FML_MULTI_PART_MESSAGE);
            MultiPartMessage message0 = attribute.get();
            if (message0 == null) {
                String channel0 = context.read(content, Types.STRING);
                int parts = content.readUnsignedByte();
                int size = content.readInt();
                if (size <= 0 || size >= -16797616) {
                    throw new CodecException("Received FML MultiPart packet outside of valid length bounds, Max: -16797616, Received: " + size);
                }
                attribute.set(new MultiPartMessage(channel0, context.byteBufAlloc().buffer(size), parts));
            } else {
                int part = content.readUnsignedByte();
                if (part != message0.index) {
                    throw new CodecException("Received FML MultiPart packet out of order, Expected: " + message0.index + ", Got: " + part);
                }
                int len = content.readableBytes() - 1;
                content.readBytes(message0.buffer, message0.offset, len);
                message0.offset += len;
                message0.index++;
                if (message0.index >= message0.parts) {
                    final Message message = this.decode0(context, message0.channel, message0.buffer);
                    attribute.set(null);
                    return message;
                }
            }
        } else {
            return this.decode0(context, channel, content);
        }
        return NullMessage.INSTANCE;
    }

    protected abstract MessageResult encode0(CodecContext context, Message message) throws CodecException;

    protected abstract Message decode0(CodecContext context, String channel, ByteBuf content) throws CodecException;

    /**
     * Decodes the byte buffer into a set of channels.
     *
     * @param buffer the byte buffer
     * @return the channels
     */
    static Set<String> decodeChannels(ByteBuf buffer) {
        return Sets.newHashSet(Splitter.on('\u0000').split(new String(buffer.array(), StandardCharsets.UTF_8)));
    }

    /**
     * Encodes the set of channels into a byte buffer.
     *
     * @param channels the channels
     * @return the byte buffer
     */
    static ByteBuf encodeChannels(Set<String> channels) {
        return Unpooled.wrappedBuffer(Joiner.on('\u0000').join(channels).getBytes(StandardCharsets.UTF_8));
    }

    protected static class MessageResult {

        private final String channel;
        private final ByteBuf byteBuf;

        public MessageResult(String channel, ByteBuf byteBuf) {
            this.byteBuf = byteBuf;
            this.channel = channel;
        }
    }

    protected static class MultiPartMessage {

        private final String channel;
        private final ByteBuf buffer;
        private final int parts;

        private int index;
        private int offset;

        public MultiPartMessage(String channel, ByteBuf buffer, int parts) {
            this.channel = channel;
            this.buffer = buffer;
            this.parts = parts;
        }
    }

}
