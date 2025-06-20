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

import static com.spotify.i18n.locales.common.model.LocaleAffinity.LOW;
import static com.spotify.i18n.locales.common.model.LocaleAffinity.MUTUALLY_INTELLIGIBLE;
import static com.spotify.i18n.locales.common.model.LocaleAffinity.NONE;
import static com.spotify.i18n.locales.common.model.LocaleAffinity.SAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import com.ibm.icu.impl.locale.LSR;
import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.LocaleAffinityCalculator;
import com.spotify.i18n.locales.common.model.LocaleAffinity;
import com.spotify.i18n.locales.common.model.LocaleAffinityResult;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LocaleAffinityCalculatorBaseImplTest {

  public static final LocaleAffinityCalculator CALCULATOR_AGAINST_EMPTY_SET =
      LocaleAffinityCalculatorBaseImpl.builder().againstLocales(Collections.emptySet()).build();

  public static final LocaleAffinityCalculator CALCULATOR_AGAINST_TEST_SET_OF_LOCALES =
      LocaleAffinityCalculatorBaseImpl.builder()
          .againstLocales(
              Set.of("ar", "bs-Cyrl", "es", "fr", "ja", "pt", "sr-Latn", "zh-Hant").stream()
                  .map(ULocale::forLanguageTag)
                  .collect(Collectors.toSet()))
          .build();

  @Test
  void whenBuildingWithMissingRequiredProperties_buildFails() {
    final IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class, () -> LocaleAffinityCalculatorBaseImpl.builder().build());

    assertEquals(thrown.getMessage(), "Property \"againstLocales\" has not been set");
  }

  @Test
  void whenBuildingWithRootAsPartOfAgainstLocales_buildFails() {
    final IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                LocaleAffinityCalculatorBaseImpl.builder()
                    .againstLocales(Set.of(ULocale.ROOT))
                    .build());

    assertEquals(
        thrown.getMessage(),
        "The locales against which affinity needs to be calculated cannot contain the root.");
  }

  @Test
  void whenBuildingWithUnavailableLocale_buildSucceeds() {
    LocaleAffinityCalculatorBaseImpl built =
        (LocaleAffinityCalculatorBaseImpl)
            LocaleAffinityCalculatorBaseImpl.builder()
                .againstLocales(
                    Set.of(ULocale.forLanguageTag("apples"), ULocale.forLanguageTag("English")))
                .build();

    assertTrue(built.againstLocales().isEmpty());
    assertTrue(built.againstSpokenLocales().isEmpty());
    assertTrue(built.againstMaximizedLSRs().isEmpty());
  }

  @Test
  void whenBuildingForHappyPath_buildSucceedsAndStructuresArePrepared() {
    final Set<ULocale> againstLocales =
        Set.of(
            ULocale.FRENCH,
            ULocale.CANADA_FRENCH,
            ULocale.ENGLISH,
            ULocale.CHINESE,
            ULocale.TRADITIONAL_CHINESE,
            ULocale.GERMANY,
            ULocale.forLanguageTag("de-AT"),
            ULocale.forLanguageTag("de-CH"),
            ULocale.JAPAN);

    LocaleAffinityCalculatorBaseImpl built =
        (LocaleAffinityCalculatorBaseImpl)
            LocaleAffinityCalculatorBaseImpl.builder().againstLocales(againstLocales).build();

    assertEquals(againstLocales, built.againstLocales());
    assertEquals(
        Set.of(
            ULocale.forLanguageTag("de"),
            ULocale.forLanguageTag("fr"),
            ULocale.forLanguageTag("en"),
            ULocale.forLanguageTag("ja"),
            ULocale.forLanguageTag("zh-Hans"),
            ULocale.forLanguageTag("zh-Hant")),
        built.againstSpokenLocales());

    assertEquals(
        Set.of(
            "de-Latn-AT",
            "de-Latn-CH",
            "de-Latn-DE",
            "en-Latn-US",
            "fr-Latn-CA",
            "fr-Latn-FR",
            "ja-Jpan-JP",
            "zh-Hans-CN",
            "zh-Hant-TW"),
        built.againstMaximizedLSRs().stream().map(LSR::toString).collect(Collectors.toSet()));
  }

  @ParameterizedTest
  @MethodSource(value = "whenCalculating_returnsExpectedAffinity")
  void whenCalculatingAgainstEmptySetOfLocales_alwaysReturnsAffinityNone(final String languageTag) {
    assertEquals(NONE, CALCULATOR_AGAINST_EMPTY_SET.calculate(languageTag).affinity());
  }

  @ParameterizedTest
  @MethodSource
  void whenCalculating_returnsExpectedAffinity(
      final String languageTag, final LocaleAffinity expectedAffinity) {
    assertThat(
        CALCULATOR_AGAINST_TEST_SET_OF_LOCALES.calculate(languageTag),
        is(LocaleAffinityResult.builder().affinity(expectedAffinity).build()));
  }

  public static Stream<Arguments> whenCalculating_returnsExpectedAffinity() {
    return Stream.of(
        // Edge cases
        Arguments.of(" Invalid language tag ", NONE),
        Arguments.of("ok-junk", NONE),
        Arguments.of("apples-and-bananas", NONE),
        Arguments.of("", NONE),
        Arguments.of(null, NONE),

        // Catalan should be matched, since we support Spanish
        Arguments.of("ca", LOW),
        Arguments.of("ca-ES", LOW),
        Arguments.of("ca-AD", LOW),

        // Bosnian should be matched for all scripts and regions, since we support Bosnian
        Arguments.of("bs", SAME),
        Arguments.of("bs-Latn", SAME),
        Arguments.of("bs-Cyrl", SAME),
        Arguments.of("bs-BA", SAME),
        Arguments.of("bs-Latn-BA", SAME),
        Arguments.of("bs-Cyrl-BA", SAME),

        // No english should be matched
        Arguments.of("en", NONE),
        Arguments.of("en-GB", NONE),
        Arguments.of("en-US", NONE),

        // Spanish in Europe should be ranked higher
        Arguments.of("es-419", SAME),
        Arguments.of("es-GB", SAME),
        Arguments.of("es-US", SAME),

        // Basque should be matched, since we support Spanish
        Arguments.of("eu", LOW),

        // French
        Arguments.of("fr", SAME),
        Arguments.of("fr-BE", SAME),
        Arguments.of("fr-CA", SAME),
        Arguments.of("fr-FR", SAME),

        // Galician should be matched, since we support Spanish
        Arguments.of("gl", LOW),

        // Hindi shouldn't be matched
        Arguments.of("hi", NONE),

        // Croatian should be nicely matched with Bosnian
        Arguments.of("hr-HR", MUTUALLY_INTELLIGIBLE),

        // Serbian Cyrillic should be matched, although only Latin script is supported
        Arguments.of("sr", SAME),
        Arguments.of("sr-Latn", SAME),
        Arguments.of("sr-Cyrl-ME", SAME),

        // Portuguese
        Arguments.of("pt", SAME),
        Arguments.of("pt-BR", SAME),
        Arguments.of("pt-SE", SAME),
        Arguments.of("pt-US", SAME),

        // Only Traditional Chinese should be matched, not Simplified
        Arguments.of("zh-CN", NONE),
        Arguments.of("zh-TW", SAME),
        Arguments.of("zh-HK", SAME));
  }

  @Test
  void whenCalculatingAffinityForSwedishAgainstBokmaalNorwegianAndDanish_returnsNone() {
    final LocaleAffinityCalculator matcher =
        LocaleAffinityCalculatorBaseImpl.builder()
            .againstLocales(
                Set.of("da", "nb", "no").stream()
                    .map(ULocale::forLanguageTag)
                    .collect(Collectors.toSet()))
            .build();

    assertThat(matcher.calculate("sv"), is(LocaleAffinityResult.builder().affinity(NONE).build()));
  }
}
