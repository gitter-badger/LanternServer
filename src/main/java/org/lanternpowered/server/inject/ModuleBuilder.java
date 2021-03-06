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
package org.lanternpowered.server.inject;

import java.util.function.Supplier;

public interface ModuleBuilder {

    <T> ModuleBuilder bindInstantiator(Class<T> type, Supplier<? extends T> supplier);

    /**
     * Binds the method spec.
     * 
     * @param methodSpec the method spec
     * @return the module builder
     */
    <T> ModuleBuilder bind(MethodSpec<T> methodSpec);

    /**
     * Binds the parameter spec to the specified provider.
     * 
     * @param spec the parameter spec
     * @param provider the provider
     * @return the module builder for chaining
     */
    <T> ModuleBuilder bind(ParameterSpec<T> spec, Provider<? extends T> provider);

    /**
     * Binds the parameter spec to the specified supplier.
     * 
     * @param spec the parameter spec
     * @param supplier the supplier
     * @return the module builder for chaining
     */
    <T> ModuleBuilder bind(ParameterSpec<T> spec, Supplier<? extends T> supplier);

    /**
     * Binds the parameter spec to the specified instance.
     * 
     * @param spec the parameter spec
     * @param instance the instance
     * @return the module builder for chaining
     */
    <T, V extends T> ModuleBuilder bindInstance(ParameterSpec<T> spec, V instance);

    /**
     * Builds to the module.
     * 
     * @return the module
     */
    Module build();
}
