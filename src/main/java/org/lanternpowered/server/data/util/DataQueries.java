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
package org.lanternpowered.server.data.util;

import static org.spongepowered.api.data.DataQuery.of;

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public final class DataQueries {

    private DataQueries() {
    }

    // Sponge Data
    public static final DataQuery SPONGE_DATA = of("SpongeData");
    public static final DataQuery SPONGE_UUID_MOST = of("uuid_most");
    public static final DataQuery SPONGE_UUID_LEAST = of("uuid_most");
    public static final DataQuery SPONGE_ENTITY_CREATOR = of("Creator");
    public static final DataQuery SPONGE_BLOCK_POS_TABLE = of("BlockPosTable");
    public static final DataQuery SPONGE_PLAYER_UUID_TABLE = of("PlayerIdTable");
    public static final DataQuery CUSTOM_MANIPULATOR_TAG_LIST = of("CustomManipulators");

    public static final DataQuery ENTITY_ID = of("id");

    // General DataQueries
    public static final DataQuery UNSAFE_NBT = of("UnsafeData");
    public static final DataQuery DATA_MANIPULATORS = of("Data");
    public static final DataQuery DATA_CLASS = of("DataClass");
    public static final DataQuery INTERNAL_DATA = of("ManipulatorData");

    // Snapshots
    public static final DataQuery SNAPSHOT_WORLD_POSITION = of("Position");

    // Blocks
    public static final DataQuery BLOCK_STATE = of("BlockState");
    public static final DataQuery BLOCK_EXTENDED_STATE = of("BlockExtendedState");
    public static final DataQuery BLOCK_TYPE = of("BlockType");
    public static final DataQuery BLOCK_STATE_UNSAFE_META = of("UnsafeMeta");

    // TileEntities
    public static final DataQuery BLOCK_ENTITY_TILE_TYPE = of("TileType");
    public static final DataQuery BLOCK_ENTITY_CUSTOM_NAME = of("CustomName");
    public static final DataQuery BLOCK_ENTITY_BREWING_TIME = of("BrewTime");
    public static final DataQuery BLOCK_ENTITY_LOCK_CODE = of("Lock");
    public static final DataQuery BLOCK_ENTITY_ITEM_CONTENTS = of("Contents");
    public static final DataQuery BLOCK_ENTITY_SLOT = of("SlotId");
    public static final DataQuery BLOCK_ENTITY_SLOT_ITEM = of("Item");

    // Entities
    public static final DataQuery ENTITY_CLASS = of("EntityClass");
    public static final DataQuery ENTITY_SNAPSHOT_ID = of("EntityUniqueId");
    public static final DataQuery ENTITY_TYPE = of("EntityType");
    public static final DataQuery ENTITY_ROTATION = of("Rotation");
    public static final DataQuery ENTITY_SCALE = of("Scale");

    // ItemStacks
    public static final DataQuery ITEM_COUNT = of("Count");
    public static final DataQuery ITEM_TYPE = of("ItemType");
    public static final DataQuery ITEM_DAMAGE_VALUE = of("UnsafeDamage");
}
