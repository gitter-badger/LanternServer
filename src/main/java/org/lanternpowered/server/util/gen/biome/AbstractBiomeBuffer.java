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
package org.lanternpowered.server.util.gen.biome;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.MoreObjects;
import org.lanternpowered.server.util.VecHelper;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.BiomeArea;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.extent.StorageType;

/**
 * Base class for biome areas. This class provides methods for retrieving the
 * size and for range checking.
 */
@NonnullByDefault
public abstract class AbstractBiomeBuffer implements BiomeArea {

    protected Vector2i start;
    protected Vector2i size;
    protected Vector2i end;
    private final int xLine;

    protected AbstractBiomeBuffer(Vector2i start, Vector2i size) {
        this.start = start;
        this.size = size;
        this.end = start.add(size).sub(Vector2i.ONE);
        this.xLine = size.getX();
    }

    protected final void checkRange(int x, int z) {
        if (!VecHelper.inBounds(x, z, this.start, this.end)) {
            throw new PositionOutOfBoundsException(new Vector2i(x, z), this.start, this.end);
        }
    }

    protected int index(int x, int y) {
        return (y - this.start.getY()) * this.xLine + (x - this.start.getX());
    }

    @Override
    public BiomeType getBiome(Vector2i position) {
        return this.getBiome(position.getX(), position.getY());
    }

    @Override
    public Vector2i getBiomeMin() {
        return this.start;
    }

    @Override
    public Vector2i getBiomeMax() {
        return this.end;
    }

    @Override
    public Vector2i getBiomeSize() {
        return this.size;
    }

    @Override
    public boolean containsBiome(Vector2i position) {
        return this.containsBiome(position.getX(), position.getY());
    }

    @Override
    public boolean containsBiome(int x, int z) {
        return VecHelper.inBounds(x, z, this.start, this.end);
    }

    @Override
    public MutableBiomeArea getBiomeCopy() {
        return this.getBiomeCopy(StorageType.STANDARD);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("min", this.getBiomeMin())
            .add("max", this.getBiomeMax())
            .toString();
    }
}
