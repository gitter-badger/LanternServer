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
package org.lanternpowered.server.text;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.spongepowered.api.text.serializer.TextSerializerFactory;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.concurrent.ExecutionException;

public final class LanternTextSerializerFactory implements TextSerializerFactory {

    private final LoadingCache<Character, FormattingCodeTextSerializer> cache = CacheBuilder.newBuilder()
            .maximumSize(53)
            .build(new CacheLoader<Character, FormattingCodeTextSerializer>() {
                @Override
                public FormattingCodeTextSerializer load(Character key) throws Exception {
                    return new FormattingCodeTextSerializer(key);
                }
            });

    @Override
    public org.spongepowered.api.text.serializer.FormattingCodeTextSerializer getFormattingCodeTextSerializer(char formattingChar) {
        if (formattingChar == TextSerializers.LEGACY_FORMATTING_CODE.getCharacter()) {
            return TextSerializers.LEGACY_FORMATTING_CODE;
        } else if (formattingChar == TextSerializers.FORMATTING_CODE.getCharacter()) {
            return TextSerializers.FORMATTING_CODE;
        } else {
            try {
                return cache.get(formattingChar);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
