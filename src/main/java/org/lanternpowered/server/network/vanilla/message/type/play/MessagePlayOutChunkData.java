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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.lanternpowered.server.network.message.Message;

import javax.annotation.Nullable;

public final class MessagePlayOutChunkData implements Message {

    private final int x;
    private final int z;

    private final Section[] sections;
    @Nullable private final byte[] biomes;
    private final boolean skylight;

    public MessagePlayOutChunkData(int x, int z, boolean skylight, Section[] sections, @Nullable byte[] biomes) {
        checkNotNull(sections, "sections");
        for (Section section : sections) {
            if (section != null) {
                checkArgument((section.skyLight == null) == skylight,
                        "Skylight must be present in every section if skylight is to true, and absent if false.");
            }
        }
        this.skylight = skylight;
        this.sections = sections;
        this.biomes = biomes;
        this.x = x;
        this.z = z;
    }

    public Section[] getSections() {
        return this.sections;
    }

    public boolean hasSkyLight() {
        return this.skylight;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    @Nullable
    public byte[] getBiomes() {
        return this.biomes;
    }

    /**
     * Represents the data of chunk section.
     */
    public static class Section {

        private final short[] blockTypes;
        private final byte[] blockLight;
        @Nullable private final byte[] skyLight;

        public Section(short[] blockTypes, byte[] blockLight, @Nullable byte[] skyLight) {
            this.blockTypes = blockTypes;
            this.blockLight = blockLight;
            this.skyLight = skyLight;
        }

        public short[] getBlockTypes() {
            return this.blockTypes;
        }

        public byte[] getBlockLight() {
            return this.blockLight;
        }

        @Nullable
        public byte[] getSkyLight() {
            return this.skyLight;
        }
    }

}
