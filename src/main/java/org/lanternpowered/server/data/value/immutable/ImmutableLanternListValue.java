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
package org.lanternpowered.server.data.value.immutable;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.lanternpowered.server.data.value.mutable.LanternListValue;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.data.value.mutable.ListValue;

import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.function.Predicate;

public class ImmutableLanternListValue<E> extends ImmutableLanternCollectionValue<E, List<E>, ImmutableListValue<E>, ListValue<E>>
    implements ImmutableListValue<E> {

    public ImmutableLanternListValue(Key<? extends BaseValue<List<E>>> key, ImmutableList<E> actualValue) {
        super(key, ImmutableList.<E>of(), actualValue);
    }

    @Override
    public ImmutableListValue<E> with(List<E> value) {
        return new ImmutableLanternListValue<E>(getKey(), ImmutableList.copyOf(checkNotNull(value)));
    }

    @Override
    public ImmutableListValue<E> transform(Function<List<E>, List<E>> function) {
        return new ImmutableLanternListValue<E>(getKey(), ImmutableList.copyOf(checkNotNull(checkNotNull(function).apply(this.actualValue))));
    }

    @Override
    public ListValue<E> asMutable() {
        final List<E> list = Lists.newArrayList();
        list.addAll(this.actualValue);
        return new LanternListValue<E>(getKey(), list);
    }

    @Override
    public ImmutableListValue<E> withElement(E elements) {
        return new ImmutableLanternListValue<E>(getKey(), ImmutableList.<E>builder().addAll(this.actualValue).add(elements).build());
    }

    @Override
    public ImmutableListValue<E> withAll(Iterable<E> elements) {
        return new ImmutableLanternListValue<E>(getKey(), ImmutableList.<E>builder().addAll(this.actualValue).addAll(elements).build());
    }

    @Override
    public ImmutableListValue<E> without(E element) {
        final ImmutableList.Builder<E> builder = ImmutableList.builder();
        for (E existingElement : this.actualValue) {
            if (!existingElement.equals(element)) {
                builder.add(existingElement);
            }
        }
        return new ImmutableLanternListValue<E>(getKey(), builder.build());
    }

    @Override
    public ImmutableListValue<E> withoutAll(Iterable<E> elements) {
        final ImmutableList.Builder<E> builder = ImmutableList.builder();
        for (E existingElement : this.actualValue) {
            if (!Iterables.contains(elements, existingElement)) {
                builder.add(existingElement);
            }
        }
        return new ImmutableLanternListValue<E>(getKey(), builder.build());
    }

    @Override
    public ImmutableListValue<E> withoutAll(Predicate<E> predicate) {
        final ImmutableList.Builder<E> builder = ImmutableList.builder();
        for (E existing : this.actualValue) {
            if (checkNotNull(predicate).test(existing)) {
                builder.add(existing);
            }
        }
        return new ImmutableLanternListValue<E>(getKey(), builder.build());
    }

    @Override
    public List<E> getAll() {
        final List<E> list = Lists.newArrayList();
        list.addAll(this.actualValue);
        return list;
    }

    @Override
    public E get(int index) {
        return this.actualValue.get(index);
    }

    @Override
    public ImmutableListValue<E> with(int index, E value) {
        final ImmutableList.Builder<E> builder = ImmutableList.builder();
        for (final ListIterator<E> iterator = this.actualValue.listIterator(); iterator.hasNext(); ) {
            if (iterator.nextIndex() - 1 == index) {
                builder.add(checkNotNull(value));
                iterator.next();
            } else {
                builder.add(iterator.next());
            }
        }
        return new ImmutableLanternListValue<E>(getKey(), builder.build());
    }

    @Override
    public ImmutableListValue<E> with(int index, Iterable<E> values) {
        final ImmutableList.Builder<E> builder = ImmutableList.builder();
        for (final ListIterator<E> iterator = this.actualValue.listIterator(); iterator.hasNext(); ) {
            if (iterator.nextIndex() -1 == index) {
                builder.addAll(values);
            }
            builder.add(iterator.next());
        }
        return new ImmutableLanternListValue<E>(getKey(), builder.build());
    }

    @Override
    public ImmutableListValue<E> without(int index) {
        final ImmutableList.Builder<E> builder = ImmutableList.builder();
        for (final ListIterator<E> iterator = this.actualValue.listIterator(); iterator.hasNext(); ) {
            if (iterator.nextIndex() - 1 != index) {
                builder.add(iterator.next());
            }
        }
        return new ImmutableLanternListValue<E>(getKey(), builder.build());
    }

    @Override
    public ImmutableListValue<E> set(int index, E element) {
        final ImmutableList.Builder<E> builder = ImmutableList.builder();
        for (final ListIterator<E> iterator = this.actualValue.listIterator(); iterator.hasNext(); ) {
            if (iterator.nextIndex() -1 == index) {
                builder.add(checkNotNull(element));
                iterator.next();
            } else {
                builder.add(iterator.next());
            }
        }
        return new ImmutableLanternListValue<E>(getKey(), builder.build());
    }

    @Override
    public int indexOf(E element) {
        return this.actualValue.indexOf(element);
    }
}
