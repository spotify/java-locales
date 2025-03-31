/*-
 * -\-\-
 * locales-common
 * --
 * Copyright (C) 2016 - 2025 Spotify AB
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

package com.spotify.i18n.locales.common.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.LocalesMatcher;
import com.spotify.i18n.locales.common.model.LocalesMatcherResult;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LocalesMatcherBaseImplTest {

  @Test
  void whenBuildingWithMissingRequiredProperties_buildFails() {
    final IllegalStateException thrown =
        assertThrows(IllegalStateException.class, () -> LocalesMatcherBaseImpl.builder().build());

    assertEquals(thrown.getMessage(), "Missing required properties: supportedLocales");
  }

  @Test
  void whenBuildingWithRootAsPartOfSupportedLocales_buildFails() {
    final IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () -> LocalesMatcherBaseImpl.builder().supportedLocales(Set.of(ULocale.ROOT)).build());

    assertEquals(thrown.getMessage(), "The supported locales cannot contain the root.");
  }

  @ParameterizedTest
  @MethodSource(value = "whenMatching_returnsExpectedResult")
  void whenMatchingAgainstEmptySetOfSupportedLocales_returnsExpectedResult(String languageTag) {
    final LocalesMatcher matcher =
        LocalesMatcherBaseImpl.builder().supportedLocales(Collections.emptySet()).build();

    assertEquals(0, matcher.match(languageTag).matchingScore());
  }

  @ParameterizedTest
  @MethodSource
  void whenMatching_returnsExpectedResult(final String languageTag, final int expectedScore) {
    final LocalesMatcher matcher =
        LocalesMatcherBaseImpl.builder()
            .supportedLocales(
                Set.of("ar", "bs", "es", "fr", "ja", "pt", "sr-Latn", "zh-Hant").stream()
                    .map(ULocale::forLanguageTag)
                    .collect(Collectors.toSet()))
            .build();

    assertThat(
        matcher.match(languageTag),
        is(LocalesMatcherResult.builder().matchingScore(expectedScore).build()));
  }

  public static Stream<Arguments> whenMatching_returnsExpectedResult() {
    return Stream.of(
        // Edge cases
        Arguments.of(" Invalid language tag ", 0),
        Arguments.of(null, 0),

        // Catalan should be matched, since we support Spanish
        Arguments.of("ca", 28),
        Arguments.of("ca-ES", 28), // Higher score for Spain than other countries
        Arguments.of("ca-AD", 14),

        // No english should be matched
        Arguments.of("en", 0),
        Arguments.of("en-GB", 0),
        Arguments.of("en-US", 0),

        // Spanish in Europe should be ranked higher
        Arguments.of("es-419", 82),
        Arguments.of("es-GB", 85),
        Arguments.of("es-US", 82),

        // Basque should be matched, since we support Spanish
        Arguments.of("eu", 28),

        // French
        Arguments.of("fr", 100),
        Arguments.of("fr-BE", 85),
        Arguments.of("fr-CA", 85),
        Arguments.of("fr-FR", 99),

        // Galician should be matched, since we support Spanish
        Arguments.of("gl", 28),

        // Hindi shouldn't be matched
        Arguments.of("hi", 0),

        // Croatian should be nicely matched with Bosnian
        Arguments.of("hr-HR", 71),

        // Serbian Cyrillic should be matched, although only Latin script is supported
        Arguments.of("sr", 82),
        Arguments.of("sr-Latn", 100),

        // Portuguese
        Arguments.of("pt", 100),
        Arguments.of("pt-BR", 99),
        Arguments.of("pt-SE", 82),
        Arguments.of("pt-US", 85),

        // Only Traditional Chinese should be matched, not Simplified
        Arguments.of("zh-CN", 0),
        Arguments.of("zh-TW", 98),
        Arguments.of("zh-HK", 82));
  }
}
