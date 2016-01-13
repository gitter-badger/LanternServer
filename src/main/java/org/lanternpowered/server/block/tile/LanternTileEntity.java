/*
 * This file is part of LanternServer, licensed under the MIT License (MIT).
 *
 * Copyright (c) LanternPowered <https://github.com/LanternPowered/LanternServer>
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
package org.lanternpowered.server.block.tile;

import com.google.common.base.Preconditions;
import org.lanternpowered.server.data.LanternDataHolder;
import org.lanternpowered.server.data.property.AbstractPropertyHolder;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public abstract class LanternTileEntity extends LanternDataHolder implements TileEntity, AbstractPropertyHolder {

    private Location<World> location;

    public LanternTileEntity(DataView data, Location<World> location) {
        super(data);
        this.location = Preconditions.checkNotNull(location);
    }

    @Override
    public boolean isValid() {
        return false; //TODO: Implement
    }

    @Override
    public void setValid(boolean valid) {
        //TODO: Implement
    }

    @Override
    public Location<World> getLocation() {
        return this.location;
    }

    @Override
    public BlockState getBlock() {
        // TODO Auto-generated method stub
        return null;
    }

    public void loadData(DataView data) {
        //TODO: Implement
    }
}
