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
package org.lanternpowered.server.data.value.mutable;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.lanternpowered.server.data.value.immutable.ImmutableLanternSetValue;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableSetValue;
import org.spongepowered.api.data.value.mutable.SetValue;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class LanternSetValue<E> extends LanternCollectionValue<E, Set<E>, SetValue<E>, ImmutableSetValue<E>> implements SetValue<E> {

    public LanternSetValue(Key<? extends BaseValue<Set<E>>> key) {
        this(key, Sets.newHashSet());
    }

    public LanternSetValue(Key<? extends BaseValue<Set<E>>> key, Set<E> actualValue) {
        this(key, Sets.newHashSet(), actualValue);
    }

    public LanternSetValue(Key<? extends BaseValue<Set<E>>> key, Set<E> defaultSet, Set<E> actualValue) {
        super(key, Sets.newHashSet(defaultSet), Sets.newHashSet(actualValue));
    }

    @Override
    public SetValue<E> transform(Function<Set<E>, Set<E>> function) {
        this.actualValue = Sets.newHashSet(checkNotNull(checkNotNull(function).apply(this.actualValue)));
        return this;
    }

    @Override
    public SetValue<E> filter(Predicate<? super E> predicate) {
        final Set<E> set = Sets.newHashSet();
        for (E element : this.actualValue) {
            if (checkNotNull(predicate).test(element)) {
                set.add(element);
            }
        }
        return new LanternSetValue<E>(getKey(), set);
    }

    @Override
    public Set<E> getAll() {
        return Sets.newHashSet(this.actualValue);
    }

    @Override
    public ImmutableSetValue<E> asImmutable() {
        return new ImmutableLanternSetValue<E>(getKey(), ImmutableSet.copyOf(this.actualValue));
    }
}
