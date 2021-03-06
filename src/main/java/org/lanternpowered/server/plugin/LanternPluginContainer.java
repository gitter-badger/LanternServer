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
package org.lanternpowered.server.plugin;

import com.google.inject.Injector;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;

import javax.annotation.Nullable;

public final class LanternPluginContainer implements PluginContainer {

    private final String id;
    private final String name;
    private final String version;

    // The instance of the plugin
    @Nullable private Object instance;
    @Nullable private Injector injector;

    LanternPluginContainer(String id, String name, String version) {
        this.version = version;
        this.name = name;
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public Optional<Object> getInstance() {
        return Optional.ofNullable(this.instance);
    }

    @Nullable
    public Injector getInjector() {
        return this.injector;
    }

    void setInjector(Injector injector) {
        if (this.injector != null) {
            throw new IllegalStateException("Injector for (" + this.getId() + ") can only be set once!");
        }
        this.injector = injector;
    }

    void setInstance(Object instance) {
        if (this.instance != null) {
            throw new IllegalStateException("Instance for (" + this.getId() + ") can only be set once!");
        }
        this.instance = instance;
    }
}
