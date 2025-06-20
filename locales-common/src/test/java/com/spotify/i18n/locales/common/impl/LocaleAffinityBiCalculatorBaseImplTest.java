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

import static com.spotify.i18n.locales.common.model.LocaleAffinity.HIGH;
import static com.spotify.i18n.locales.common.model.LocaleAffinity.LOW;
import static com.spotify.i18n.locales.common.model.LocaleAffinity.MUTUALLY_INTELLIGIBLE;
import static com.spotify.i18n.locales.common.model.LocaleAffinity.NONE;
import static com.spotify.i18n.locales.common.model.LocaleAffinity.SAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.LocaleAffinityBiCalculator;
import com.spotify.i18n.locales.common.LocaleAffinityCalculator;
import com.spotify.i18n.locales.common.model.LocaleAffinity;
import com.spotify.i18n.locales.common.model.LocaleAffinityResult;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LocaleAffinityBiCalculatorBaseImplTest {

  private static final LocaleAffinityBiCalculator BI_CALCULATOR =
      LocaleAffinityBiCalculatorBaseImpl.builder().build();

  @ParameterizedTest
  @MethodSource
  void whenCalculating_returnsExpectedAffinity(
      final String languageTag1, final String languageTag2, final LocaleAffinity expectedAffinity) {
    assertThat(
        BI_CALCULATOR.calculate(languageTag1, languageTag2),
        is(LocaleAffinityResult.builder().affinity(expectedAffinity).build()));
  }

  public static Stream<Arguments> whenCalculating_returnsExpectedAffinity() {
    return Stream.of(
        // Edge cases
        Arguments.of("What is this?", " An invalid language tag!", NONE),
        Arguments.of("ok-gargabe", "ok-junk", NONE),
        Arguments.of("apples-and-bananas", "oranges-and-pears", NONE),
        Arguments.of("   ", "", NONE),
        Arguments.of(null, null, NONE),
        Arguments.of("an", " An invalid language tag!", NONE),
        Arguments.of("ok", "ok-junk", NONE),
        Arguments.of("oranges", "oranges-and-pears", NONE),

        // Catalan should be matched with Spanish, both ways
        Arguments.of("es", "ca", LOW),
        Arguments.of("ca-ES", "es-AD", LOW),
        Arguments.of("ca-AD", "es-419", LOW),

        // Bosnian should be matched for all scripts and regions
        Arguments.of("bs-Cyrl", "bs", SAME),
        Arguments.of("bs-Cyrl", "bs-Latn", SAME),
        Arguments.of("bs-Cyrl-MK", "bs-Cyrl", SAME),
        Arguments.of("bs-Cyrl", "bs-BA", SAME),
        Arguments.of("bs", "bs-Latn-BA", SAME),
        Arguments.of("bs-Latn-US", "bs-Cyrl-BA", SAME),

        // German and Swiss German
        Arguments.of("de-DE", "gsw-AT", MUTUALLY_INTELLIGIBLE),
        Arguments.of("de-CH", "gsw-CH", MUTUALLY_INTELLIGIBLE),
        Arguments.of("gsw-CH", "de-CH", MUTUALLY_INTELLIGIBLE),

        // English
        Arguments.of("en", "en-AU", SAME),
        Arguments.of("en-GB", "fr", NONE),
        Arguments.of("en-US", "nb", NONE),

        // Spanish in Europe should be matched with Spanish Latin America
        Arguments.of("es-150", "es-419", SAME),
        Arguments.of("es-ES", "es-GB", SAME),
        Arguments.of("es-US", "es-AR", SAME),

        // Basque should be matched with Spanish
        Arguments.of("es", "eu", LOW),
        Arguments.of("eu", "es", LOW),

        // French
        Arguments.of("fr", "fr-CH", SAME),
        Arguments.of("fr-BE", "fr-HI", SAME),
        Arguments.of("fr-CA", "fr-US", SAME),
        Arguments.of("fr-FR", "fr-JP", SAME),

        // Galician should be matched, since we support Spanish
        Arguments.of("gl", "es", LOW),
        Arguments.of("es", "gl", LOW),

        // Hindi shouldn't be matched with Tamil
        Arguments.of("hi", "ta", NONE),

        // Hindi (Latin) and English should be matched
        Arguments.of("hi-Latn", "en-GB", NONE),

        // Croatian should be nicely matched with Bosnian
        Arguments.of("bs-Latn", "hr-HR", MUTUALLY_INTELLIGIBLE),
        Arguments.of("hr-BA", "bs-Cyrl", MUTUALLY_INTELLIGIBLE),

        // Serbian Cyrillic should be matched with Serbian Latin
        Arguments.of("sr-Latn", "sr", SAME),
        Arguments.of("sr", "sr-Latn", SAME),
        Arguments.of("sr-Latn-MK", "sr-Cyrl-ME", SAME),

        // Portuguese
        Arguments.of("pt-PT", "pt", SAME),
        Arguments.of("pt-BR", "pt-PT", SAME),
        Arguments.of("pt-SE", "pt-JP", SAME),
        Arguments.of("pt-US", "pt-CL", SAME),

        // Norwegian, Norwegian Bokmål, Nynorst
        Arguments.of("nb", "da", HIGH),
        Arguments.of("nn", "nb", SAME),
        Arguments.of("no", "nb", SAME),

        // Uzbek should be matched as SAME for all scripts
        Arguments.of("uz-Arab", "uz-Cyrl", SAME),
        Arguments.of("uz-Cyrl", "uz", SAME),
        Arguments.of("uz", "uz-Arab", SAME),

        // Traditional Chinese shouldn't be matched with Simplified
        Arguments.of("zh-Hant", "zh-CN", NONE),
        Arguments.of("zh-Hant", "zh", NONE),
        Arguments.of("zh-MK", "zh-CN", SAME),
        Arguments.of("zh-FR", "zh-CN", SAME),
        Arguments.of("zh-TW", "zh-US", SAME));
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
