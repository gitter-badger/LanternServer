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
package org.lanternpowered.server.service.pagination;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.command.CommandMessageFormatting.error;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ProxySource;
import org.spongepowered.api.service.pagination.PaginationBuilder;
import org.spongepowered.api.service.pagination.PaginationCalculator;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

class LanternPaginationBuilder implements PaginationBuilder {

    private final LanternPaginationService service;
    private Iterable<Text> contents;
    @Nullable private Text title;
    @Nullable private Text header;
    @Nullable private Text footer;
    private String paginationSpacer = "=";

    public LanternPaginationBuilder(LanternPaginationService service) {
        this.service = service;
    }

    @Override
    public PaginationBuilder contents(Iterable<Text> contents) {
        this.contents = contents;
        return this;
    }

    @Override
    public PaginationBuilder contents(Text... contents) {
        this.contents = ImmutableList.copyOf(contents);
        return this;
    }

    @Override
    public PaginationBuilder title(@Nullable Text title) {
        this.title = title;
        return this;
    }

    @Override
    public PaginationBuilder header(@Nullable Text header) {
        this.header = header;
        return this;
    }

    @Override
    public PaginationBuilder footer(@Nullable Text footer) {
        this.footer = footer;
        return this;
    }

    @Override
    public PaginationBuilder paddingString(String padding) {
        this.paginationSpacer = padding;
        return this;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void sendTo(final CommandSource source) {
        checkNotNull(this.contents, "contents");
        checkNotNull(source, "source");
        this.service.registerCommandOnce();

        CommandSource realSource = source;
        while (realSource instanceof ProxySource) {
            realSource = ((ProxySource) realSource).getOriginalSource();
        }
        @SuppressWarnings("unchecked")
        PaginationCalculator<CommandSource> calculator = (PaginationCalculator) this.service.calculators.get(realSource.getClass());
        if (calculator == null) {
            calculator = this.service.getUnpaginatedCalculator(); // TODO: or like 50 lines?
        }
        final PaginationCalculator<CommandSource> finalCalculator = calculator;
        Iterable<Map.Entry<Text, Integer>> counts = Iterables.transform(this.contents, new Function<Text, Map.Entry<Text, Integer>>() {
            @Nullable
            @Override
            public Map.Entry<Text, Integer> apply(@Nullable Text input) {
                int lines = finalCalculator.getLines(source, input);
                return Maps.immutableEntry(input, lines);
            }
        });

        Text title = this.title;
        if (title != null) {
            title = calculator.center(source, title, this.paginationSpacer);
        }

        ActivePagination pagination;
        if (this.contents instanceof List) { // If it started out as a list, it's probably reasonable to copy it to another list
            pagination = new ListPagination(source, calculator, ImmutableList.copyOf(counts), title, this.header, this.footer, this.paginationSpacer);
        } else {
            pagination = new IterablePagination(source, calculator, counts, title, this.header, this.footer, this.paginationSpacer);
        }

        this.service.getPaginationState(source, true).put(pagination);
        try {
            pagination.nextPage();
        } catch (CommandException e) {
            source.sendMessage(error(e.getText()));
        }

    }
}
