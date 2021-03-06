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
package org.lanternpowered.server.world.gen;

import com.google.common.base.MoreObjects;
import org.lanternpowered.server.catalog.LanternPluginCatalogType;
import org.lanternpowered.server.game.LanternGame;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.world.gen.PopulatorType;

import javax.annotation.Nullable;

public final class LanternPopulatorType extends LanternPluginCatalogType implements PopulatorType {

    private final Translation translation;

    public LanternPopulatorType(String name) {
        this("minecraft", name);
    }

    public LanternPopulatorType(String pluginId, String name) {
        super(pluginId, name);
        this.translation = LanternGame.get().getRegistry().getTranslationManager().get("populator." + name + ".name");
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final LanternPopulatorType other = (LanternPopulatorType) obj;
        return this.getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", this.getId())
                .add("name", this.getName())
                .add("pluginId", this.getPluginId())
                .toString();
    }

    @Override
    public Translation getTranslation() {
        return this.translation;
    }

}
