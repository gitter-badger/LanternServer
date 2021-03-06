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
package org.lanternpowered.server.network.objects;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.text.Text;

import java.util.Locale;

/**
 * Represents a {@link Text} object that will be translated (if needed) for
 * the specified locale.
 */
public final class LocalizedText {

    /**
     * A equivalent of the localized text for {@link Text#EMPTY}.
     */
    public static final LocalizedText EMPTY = new LocalizedText(Text.EMPTY);

    private final Locale locale;
    private final Text text;

    /**
     * Creates a new localized text object for the specified text
     * and locale.
     *
     * @param text the text
     */
    public LocalizedText(Text text, Locale locale) {
        this.locale = checkNotNull(locale, "locale");
        this.text = checkNotNull(text, "text");
    }

    /**
     * Creates a new localized text object for the specified text
     * and default locale.
     *
     * @param text the text
     */
    public LocalizedText(Text text) {
        this(text, Locale.ENGLISH);
    }

    /**
     * Gets the locale.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return this.locale;
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    public Text getText() {
        return this.text;
    }

}
