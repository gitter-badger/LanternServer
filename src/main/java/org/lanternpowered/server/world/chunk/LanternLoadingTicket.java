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
package org.lanternpowered.server.world.chunk;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableSet;
import org.lanternpowered.server.game.LanternGame;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.util.GuavaCollectors;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Nullable;

class LanternLoadingTicket implements ChunkLoadingTicket {

    private final ConcurrentLinkedQueue<Vector2i> queue = new ConcurrentLinkedQueue<>();
    private final LanternChunkManager chunkManager;
    private final String plugin;

    // The extra data, can be attached by mods, just keep it safe until
    // sponge decides to add a api for it
    @Nullable DataContainer extraData;

    // The maximum amount of chunks that can be loaded by this ticket
    private final int maxChunks;

    // The amount of chunks that may be loaded by this ticket
    private volatile int numChunks;

    // Whether the ticket is released and may not be used again
    private volatile boolean released;

    LanternLoadingTicket(String plugin, LanternChunkManager chunkManager,
            int maxChunks) {
        this(plugin, chunkManager, maxChunks, maxChunks);
    }

    LanternLoadingTicket(String plugin, LanternChunkManager chunkManager,
            int maxChunks, int numChunks) {
        this.numChunks = Math.min(numChunks, maxChunks);
        this.chunkManager = chunkManager;
        this.maxChunks = maxChunks;
        this.plugin = plugin;
    }

    /**
     * Gets whether there no entries are inside the ticket.
     * 
     * @return is empty
     */
    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    @Override
    public boolean setNumChunks(int numChunks) {
        checkArgument(numChunks >= 0, "numChunks may not be negative");
        if (numChunks > this.maxChunks) {
            return false;
        }
        // Remove the oldest chunks that cannot be loaded anymore
        if (!this.released && numChunks < this.numChunks) {
            int size = this.queue.size();

            if (numChunks < size) {
                for (int i = 0; i < size - numChunks; i++) {
                    this.chunkManager.unforce(this, this.queue.poll());
                }
            }
        }
        this.numChunks = numChunks;
        return true;
    }

    @Override
    public int getNumChunks() {
        return this.numChunks;
    }

    @Override
    public int getMaxNumChunks() {
        return this.maxChunks;
    }

    @Override
    public String getPlugin() {
        return this.plugin;
    }

    @Override
    public ImmutableSet<Vector3i> getChunkList() {
        if (this.released) {
            return ImmutableSet.of();
        }
        return this.queue.stream().map(v -> new Vector3i(v.getX(), 0, v.getY()))
                .collect(GuavaCollectors.toImmutableSet());
    }

    @Override
    public void forceChunk(Vector3i chunk) {
        if (this.released) {
            LanternGame.log().warn("The plugin {} attempted to force load a chunk with an invalid ticket. "
                    + "This is not permitted.", this.plugin);
            return;
        }
        Vector2i chunk0 = checkNotNull(chunk, "chunk").toVector2(true);
        // Only force if not done before
        if (!this.queue.contains(chunk0)) {
            // Remove the oldest chunk if necessary
            if (this.queue.size() >= this.numChunks) {
                this.chunkManager.unforce(this, this.queue.poll());
            }
            this.queue.add(chunk0);
            this.chunkManager.force(this, chunk0);
        }
    }

    @Override
    public void unforceChunk(Vector3i chunk) {
        if (this.released) {
            return;
        }
        final Vector2i chunk0 = checkNotNull(chunk, "chunk").toVector2(true);
        if (this.queue.remove(chunk0)) {
            this.chunkManager.unforce(this, chunk0);
        }
    }

    @Override
    public void unforceChunks() {
        if (this.released) {
            return;
        }
        while (!this.queue.isEmpty()) {
            this.chunkManager.unforce(this, this.queue.poll());
        }
    }

    @Override
    public void prioritizeChunk(Vector3i chunk) {
        if (this.released) {
            return;
        }
        final Vector2i chunk0 = checkNotNull(chunk, "chunk").toVector2(true);
        // Move the chunk to the bottom of the queue if found
        if (this.queue.remove(chunk0)) {
            this.queue.add(chunk0);
        }
    }

    @Override
    public void release() {
        this.unforceChunks();
        this.chunkManager.release(this);
        this.released = true;
    }
}
