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

package com.spotify.i18n.translations.http.examples.handlers;

import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.ContextBasedSupportedLocalesSupplier;
import com.spotify.i18n.locales.common.model.SupportedLocale;
import com.sun.net.httpserver.HttpExchange;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/** Handler of requests to list supported locales */
public class SupportedLocalesHandler extends AbstractHttpHandler {

  private static final String TITLE_LINE = "List of supported locales:";
  private static final String EXPLICIT_SEPARATOR = "=".repeat(TITLE_LINE.length());

  private ContextBasedSupportedLocalesSupplier<HttpExchange> supportedLocalesSupplier;

  public SupportedLocalesHandler(
      final ContextBasedSupportedLocalesSupplier<HttpExchange> supportedLocalesSupplier) {
    this.supportedLocalesSupplier = supportedLocalesSupplier;
  }

  @Override
  public CompletionStage<Void> handleAsync(final HttpExchange exchange) {
    return supportedLocalesSupplier
        .get(exchange)
        .thenAccept(supportedLocales -> ok(exchange, supportedLocales));
  }

  private void ok(final HttpExchange exchange, final Set<SupportedLocale> supportedLocales) {
    StringBuilder b =
        new StringBuilder().append(TITLE_LINE).append(LS).append(EXPLICIT_SEPARATOR).append(LS);

    supportedLocales.stream()
        // Sort by ascending language tag
        .sorted(Comparator.comparing(l -> l.localeForTranslations().toLanguageTag()))
        .forEach(supportedLocale -> b.append(supportedLocaleDetails(supportedLocale)));

    writeResponse(exchange, b.toString());
  }

  private static String supportedLocaleDetails(SupportedLocale supportedLocale) {
    return new StringBuilder()
        .append(
            String.format(
                "- locale for translations: %s",
                supportedLocale.localeForTranslations().toLanguageTag()))
        .append(LS)
        .append(
            String.format(
                "- related locales for formatting: %s",
                supportedLocale.relatedLocalesForFormatting().stream()
                    .map(ULocale::toLanguageTag)
                    .sorted()
                    .collect(Collectors.joining(", "))))
        .append(LS)
        .append(EXPLICIT_SEPARATOR)
        .append(LS)
        .toString();
  }
}
