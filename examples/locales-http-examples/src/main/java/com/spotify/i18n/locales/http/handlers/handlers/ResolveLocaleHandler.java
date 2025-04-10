/*-
 * -\-\-
 * locales-http-examples
 * --
 * Copyright (C) 2016 - 2024 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.i18n.locales.http.handlers.handlers;

import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.ContextBasedLocalesResolver;
import com.spotify.i18n.locales.common.model.ResolvedLocale;
import com.sun.net.httpserver.HttpExchange;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/** Handler of requests to display the corresponding resolved locale details */
public class ResolveLocaleHandler extends AbstractHttpHandler {

  private final ContextBasedLocalesResolver<HttpExchange> localesResolver;

  public ResolveLocaleHandler(final ContextBasedLocalesResolver<HttpExchange> localesResolver) {
    this.localesResolver = localesResolver;
  }

  @Override
  public CompletionStage<Void> handleAsync(HttpExchange exchange) {
    return localesResolver
        .resolve(exchange)
        .thenAccept(resolvedLocale -> ok(exchange, resolvedLocale));
  }

  private void ok(HttpExchange exchange, ResolvedLocale resolvedLocale) {
    writeResponse(
        exchange,
        new StringBuilder()
            .append("Resolved locale details:")
            .append(LS)
            .append(
                String.format(
                    "- locale for translations: %s",
                    resolvedLocale.localeForTranslations().toLanguageTag()))
            .append(LS)
            .append(
                String.format(
                    "- fallback locales for translations: %s",
                    resolvedLocale.localeForTranslationsFallbacks().stream()
                        .map(ULocale::toLanguageTag)
                        .collect(Collectors.joining(", "))))
            .append(LS)
            .append(
                String.format(
                    "- locale for formatting: %s",
                    resolvedLocale.localeForFormatting().toLanguageTag()))
            .append(LS)
            .toString());
  }
}
