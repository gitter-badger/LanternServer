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
package org.lanternpowered.server.world.difficulty;

import org.lanternpowered.server.catalog.SimpleLanternCatalogType;
import org.lanternpowered.server.game.LanternGame;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.world.difficulty.Difficulty;

public final class LanternDifficulty extends SimpleLanternCatalogType implements Difficulty {

    private final Translation translation;
    private final byte internalId;

    public LanternDifficulty(String identifier, int internalId) {
        super(identifier);
        this.translation = LanternGame.get().getRegistry().getTranslationManager().get(
                "options.difficulty." + identifier);
        this.internalId = (byte) internalId;
    }

    public byte getInternalId() {
        return this.internalId;
    }

    @Override
    public Translation getTranslation() {
        return this.translation;
    }

}
