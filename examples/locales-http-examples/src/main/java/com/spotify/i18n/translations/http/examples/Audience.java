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

package com.spotify.i18n.translations.http.examples;

import com.spotify.i18n.locales.common.SupportedLocalesSupplier;
import com.spotify.i18n.locales.common.impl.SupportedLocalesSupplierBaseImpl;
import com.spotify.i18n.locales.common.model.SupportedLocale;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Audiences to which {@link SupportedLocale}s can be exposed.
 *
 * <p>An audience defines a set of target users, that can be exposed to a certain set of {@link
 * SupportedLocale}. Different audiences may be exposed to different sets of supported locales.
 *
 * @author Eric Fjøsne
 */
public enum Audience {

  // Default audience (equivalent to production)
  DEFAULT("en", "es", "es-419", "fr", "nl"),
  // Audience consisting of users responsible for ensuring the quality of the localized experience
  QUALITY_ASSURANCE(
      "en", "en-GB", "es", "es-419", "es-CL", "es-PY", "fr", "fr-CA", "ja", "nl", "sv");

  private final SupportedLocalesSupplier supportedLocalesSupplier;

  /**
   * Constructor, which generates a {@link SupportedLocalesSupplier} based on the given {@code
   * supportedLanguageTags}.
   *
   * @param supportedLanguageTags list of supported language tags for this audience
   */
  Audience(String... supportedLanguageTags) {
    this.supportedLocalesSupplier =
        SupportedLocalesSupplierBaseImpl.builder()
            .supportedLocales(
                Arrays.stream(supportedLanguageTags)
                    .map(SupportedLocale::fromLanguageTag)
                    .collect(Collectors.toSet()))
            .build();
  }

  /** Returns the {@link SupportedLocalesSupplier} for this audience */
  public SupportedLocalesSupplier getSupportedLocalesSupplier() {
    return supportedLocalesSupplier;
  }
}
