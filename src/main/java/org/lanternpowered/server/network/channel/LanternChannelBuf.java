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
package org.lanternpowered.server.network.channel;

import static org.lanternpowered.server.network.message.codec.serializer.SimpleSerializerContext.DEFAULT;

import io.netty.buffer.ByteBuf;
import org.lanternpowered.server.network.message.codec.serializer.Types;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.network.ChannelBuf;

import java.nio.ByteOrder;
import java.util.UUID;

import javax.annotation.Nullable;

public class LanternChannelBuf implements ChannelBuf {

    private final ByteBuf buf;

    @Nullable
    private LanternChannelBuf opposite;

    public LanternChannelBuf(ByteBuf buf) {
        this.buf = buf;
    }

    public ByteBuf getDelegate() {
        return this.buf;
    }

    @Override
    public int getCapacity() {
        return this.buf.capacity();
    }

    @Override
    public int available() {
        return this.buf.readableBytes();
    }

    @Override
    public LanternChannelBuf order(ByteOrder order) {
        if (this.buf.order().equals(order)) {
            return this;
        } else {
            if (this.opposite == null) {
                this.opposite = new LanternChannelBuf(this.buf.order(order));
                this.opposite.opposite = this;
            }
            return this.opposite;
        }
    }

    @Override
    public ByteOrder getByteOrder() {
        return this.buf.order();
    }

    @Override
    public int readerIndex() {
        return this.buf.readerIndex();
    }

    @Override
    public LanternChannelBuf setReadIndex(int index) {
        this.buf.readerIndex(index);
        return this;
    }

    @Override
    public int writerIndex() {
        return this.buf.writerIndex();
    }

    @Override
    public LanternChannelBuf setWriteIndex(int index) {
        this.buf.writerIndex(index);
        return this;
    }

    @Override
    public LanternChannelBuf setIndex(int readIndex, int writeIndex) {
        this.buf.setIndex(readIndex, writeIndex);
        return this;
    }

    @Override
    public LanternChannelBuf clear() {
        this.buf.clear();
        return this;
    }

    @Override
    public LanternChannelBuf markRead() {
        this.buf.markReaderIndex();
        return this;
    }

    @Override
    public LanternChannelBuf markWrite() {
        this.buf.markWriterIndex();
        return this;
    }

    @Override
    public LanternChannelBuf resetRead() {
        this.buf.resetReaderIndex();
        return this;
    }

    @Override
    public LanternChannelBuf resetWrite() {
        this.buf.resetWriterIndex();
        return this;
    }

    @Override
    public LanternChannelBuf slice() {
        return new LanternChannelBuf(this.buf.slice());
    }

    @Override
    public LanternChannelBuf slice(int index, int length) {
        return new LanternChannelBuf(this.buf.slice(index, length));
    }

    @Override
    public byte[] array() {
        return this.buf.array();
    }

    @Override
    public LanternChannelBuf writeBoolean(boolean data) {
        this.buf.writeBoolean(data);
        return this;
    }

    @Override
    public LanternChannelBuf setBoolean(int index, boolean data) {
        this.buf.setBoolean(index, data);
        return this;
    }

    @Override
    public boolean readBoolean() {
        return this.buf.readBoolean();
    }

    @Override
    public boolean getBoolean(int index) {
        return this.buf.getBoolean(index);
    }

    @Override
    public LanternChannelBuf writeByte(byte data) {
        this.buf.writeByte(data);
        return this;
    }

    @Override
    public LanternChannelBuf setByte(int index, byte data) {
        this.buf.setByte(index, data);
        return this;
    }

    @Override
    public byte readByte() {
        return this.buf.readByte();
    }

    @Override
    public byte getByte(int index) {
        return this.buf.getByte(index);
    }

    @Override
    public LanternChannelBuf writeShort(short data) {
        this.buf.writeShort(data);
        return this;
    }

    @Override
    public LanternChannelBuf setShort(int index, short data) {
        this.buf.setShort(index, data);
        return this;
    }

    @Override
    public short readShort() {
        return this.buf.readShort();
    }

    @Override
    public short getShort(int index) {
        return this.buf.getShort(index);
    }

    @Override
    public LanternChannelBuf writeChar(char data) {
        this.buf.writeChar(data);
        return this;
    }

    @Override
    public LanternChannelBuf setChar(int index, char data) {
        this.buf.setChar(index, data);
        return this;
    }

    @Override
    public char readChar() {
        return this.buf.readChar();
    }

    @Override
    public char getChar(int index) {
        return this.buf.getChar(index);
    }

    @Override
    public LanternChannelBuf writeInteger(int data) {
        this.buf.writeInt(data);
        return this;
    }

    @Override
    public LanternChannelBuf setInteger(int index, int data) {
        this.buf.setInt(index, data);
        return this;
    }

    @Override
    public int readInteger() {
        return this.buf.readInt();
    }

    @Override
    public int getInteger(int index) {
        return this.buf.getInt(index);
    }

    @Override
    public LanternChannelBuf writeLong(long data) {
        this.buf.writeLong(data);
        return this;
    }

    @Override
    public LanternChannelBuf setLong(int index, long data) {
        this.buf.setLong(index, data);
        return this;
    }

    @Override
    public long readLong() {
        return this.buf.readLong();
    }

    @Override
    public long getLong(int index) {
        return this.buf.getLong(index);
    }

    @Override
    public LanternChannelBuf writeFloat(float data) {
        this.buf.writeFloat(data);
        return this;
    }

    @Override
    public LanternChannelBuf setFloat(int index, float data) {
        this.buf.setFloat(index, data);
        return this;
    }

    @Override
    public float readFloat() {
        return this.buf.readFloat();
    }

    @Override
    public float getFloat(int index) {
        return this.buf.getFloat(index);
    }

    @Override
    public LanternChannelBuf writeDouble(double data) {
        this.buf.writeDouble(data);
        return this;
    }

    @Override
    public LanternChannelBuf setDouble(int index, double data) {
        this.buf.setDouble(index, data);
        return this;
    }

    @Override
    public double readDouble() {
        return this.buf.readDouble();
    }

    @Override
    public double getDouble(int index) {
        return this.buf.getDouble(index);
    }

    @Override
    public LanternChannelBuf writeString(String data) {
        DEFAULT.write(this.buf, Types.STRING, data);
        return this;
    }

    @Override
    public LanternChannelBuf setString(int index, String data) {
        DEFAULT.writeAt(this.buf, index, Types.STRING, data);
        return this;
    }

    @Override
    public String readString() {
        return DEFAULT.read(this.buf, Types.STRING);
    }

    @Override
    public String getString(int index) {
        return DEFAULT.readAt(this.buf, index, Types.STRING);
    }

    @Override
    public LanternChannelBuf writeUniqueId(UUID data) {
        DEFAULT.write(this.buf, Types.UNIQUE_ID, data);
        return this;
    }

    @Override
    public LanternChannelBuf setUniqueId(int index, UUID data) {
        DEFAULT.writeAt(this.buf, index, Types.UNIQUE_ID, data);
        return this;
    }

    @Override
    public UUID readUniqueId() {
        return DEFAULT.read(this.buf, Types.UNIQUE_ID);
    }

    @Override
    public UUID getUniqueId(int index) {
        return DEFAULT.readAt(this.buf, index, Types.UNIQUE_ID);
    }

    @Override
    public LanternChannelBuf writeDataView(DataView data) {
        DEFAULT.write(this.buf, Types.DATA_VIEW, data);
        return this;
    }

    @Override
    public LanternChannelBuf setDataView(int index, DataView data) {
        DEFAULT.writeAt(this.buf, index, Types.DATA_VIEW, data);
        return this;
    }

    @Override
    public DataView readDataView() {
        return DEFAULT.read(this.buf, Types.DATA_VIEW);
    }

    @Override
    public DataView getDataView(int index) {
        return DEFAULT.readAt(this.buf, index, Types.DATA_VIEW);
    }
}
