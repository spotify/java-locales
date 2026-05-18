/*-
 * -\-\-
 * locales-common
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

package com.spotify.i18n.locales.common.impl;

import static com.spotify.i18n.locales.common.model.LocaleAffinity.HIGH;
import static com.spotify.i18n.locales.common.model.LocaleAffinity.LOW;
import static com.spotify.i18n.locales.common.model.LocaleAffinity.MUTUALLY_INTELLIGIBLE;
import static com.spotify.i18n.locales.common.model.LocaleAffinity.SAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ibm.icu.impl.locale.LocaleDistance;
import com.ibm.icu.util.LocaleMatcher.FavorSubtag;
import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.LocalesResolver;
import com.spotify.i18n.locales.common.model.LocaleAffinity;
import com.spotify.i18n.locales.common.model.ResolvedLocale;
import com.spotify.i18n.locales.common.model.SupportedLocale;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LocalesResolverBaseImplTest {

  private static final ResolvedLocale DEFAULT_LOCALE = ResolvedLocale.fromLanguageTags("en", "en");

  @Test
  void whenBuildingWithMissingRequiredProperties_buildFails() {
    IllegalStateException thrown =
        assertThrows(IllegalStateException.class, () -> LocalesResolverBaseImpl.builder().build());

    assertEquals(
        thrown.getMessage(), "Missing required properties: supportedLocales defaultResolvedLocale");
  }

  @Test
  void whenBuildingWithAllRequiredProperties_handlesInvalidAndEdgeCasesProperly() {
    LocalesResolver resolver =
        LocalesResolverBaseImpl.builder()
            .supportedLocales(
                Set.of("fr", "ar", "zh-Hant", "ja").stream()
                    .map(SupportedLocale::fromLanguageTag)
                    .collect(Collectors.toSet()))
            .defaultResolvedLocale(DEFAULT_LOCALE)
            .build();

    assertThat(resolver.resolve("Invalid accept language value"), is(DEFAULT_LOCALE));

    // Edge cases
    assertThat(resolver.resolve(null), is(DEFAULT_LOCALE));
    assertThat(resolver.resolve("en"), is(DEFAULT_LOCALE));
  }

  @ParameterizedTest
  @MethodSource
  public void whenResolvingLocale_returnedValueMatches(
      final String acceptLanguage,
      final Set<SupportedLocale> supportedLocales,
      final ResolvedLocale expectedResolvedLocale) {

    LocalesResolver resolver =
        LocalesResolverBaseImpl.builder()
            .supportedLocales(supportedLocales)
            .defaultResolvedLocale(DEFAULT_LOCALE)
            .build();

    assertThat(resolver.resolve(acceptLanguage), is(expectedResolvedLocale));
  }

  public static Stream<Arguments> whenResolvingLocale_returnedValueMatches() {
    ResolvedLocale defaultLocale = ResolvedLocale.fromLanguageTags("en", "en");

    Set<SupportedLocale> supportedLocales =
        Set.of("fr", "ar", "zh-Hans", "zh-Hant", "ja").stream()
            .map(SupportedLocale::fromLanguageTag)
            .collect(Collectors.toSet());

    return Stream.of(
        // Edge cases
        Arguments.of("", supportedLocales, defaultLocale),
        Arguments.of("en", Set.of(), defaultLocale),
        Arguments.of("", supportedLocales, defaultLocale),
        // Single locale
        Arguments.of("fr-BE", supportedLocales, ResolvedLocale.fromLanguageTags("fr", "fr-BE")),
        // Multiple weighted locales
        Arguments.of(
            "fr-BE;q=0.1,    JA_jp, ZH_hk;q=0.5",
            supportedLocales,
            ResolvedLocale.fromLanguageTags("ja", "ja-JP")),
        // Single weighted locales
        Arguments.of(
            "ZH_hk;q=0.5",
            supportedLocales,
            ResolvedLocale.fromLanguageTags("zh-Hant", "zh-Hant-HK")),
        // parametrized locales are resolved correctly
        Arguments.of(
            "fr_BE@calendar=gregorian",
            supportedLocales,
            ResolvedLocale.fromLanguageTags("fr", "fr-BE")),
        Arguments.of(
            "en_SG@calendar=buddhist",
            supportedLocales,
            ResolvedLocale.fromLanguageTags("en", "en")),
        Arguments.of("ms-BN", supportedLocales, ResolvedLocale.fromLanguageTags("en", "en")),

        // Let's play with wildcards
        Arguments.of("*-FR", supportedLocales, ResolvedLocale.fromLanguageTags("fr", "fr-FR")),
        Arguments.of("*-BE", supportedLocales, ResolvedLocale.fromLanguageTags("fr", "fr-BE")),
        Arguments.of(
            "*-Hans-HK",
            supportedLocales,
            ResolvedLocale.fromLanguageTags("zh-Hans", "zh-Hans-HK")),
        Arguments.of("zh-*", supportedLocales, ResolvedLocale.fromLanguageTags("zh-Hans", "zh")),
        Arguments.of("zh-*-*", supportedLocales, ResolvedLocale.fromLanguageTags("zh-Hans", "zh")),
        Arguments.of(
            "*-Hant-*", supportedLocales, ResolvedLocale.fromLanguageTags("zh-Hant", "zh-Hant")),
        Arguments.of(
            "*-HK", supportedLocales, ResolvedLocale.fromLanguageTags("zh-Hant", "zh-Hant-HK")),
        Arguments.of(
            "*-CN", supportedLocales, ResolvedLocale.fromLanguageTags("zh-Hans", "zh-Hans-CN")),
        Arguments.of(
            "*-Hans", supportedLocales, ResolvedLocale.fromLanguageTags("zh-Hans", "zh-Hans")),

        // Edge cases with wildcards
        Arguments.of("*", supportedLocales, ResolvedLocale.fromLanguageTags("en", "en")),
        Arguments.of("*,*", supportedLocales, ResolvedLocale.fromLanguageTags("en", "en")),
        Arguments.of("*,**-JP", supportedLocales, ResolvedLocale.fromLanguageTags("en", "en")),
        Arguments.of("*,**-JP", supportedLocales, ResolvedLocale.fromLanguageTags("en", "en")),
        Arguments.of("*,*-JP", supportedLocales, ResolvedLocale.fromLanguageTags("ja", "ja-JP")),
        Arguments.of("**", supportedLocales, ResolvedLocale.fromLanguageTags("en", "en")),
        Arguments.of("****-", supportedLocales, ResolvedLocale.fromLanguageTags("en", "en")),
        Arguments.of("**-**", supportedLocales, ResolvedLocale.fromLanguageTags("en", "en")),
        Arguments.of("**-*", supportedLocales, ResolvedLocale.fromLanguageTags("en", "en")),
        Arguments.of("*-*-*", supportedLocales, ResolvedLocale.fromLanguageTags("en", "en")),
        Arguments.of("*-*-*", supportedLocales, ResolvedLocale.fromLanguageTags("en", "en")),
        Arguments.of("**-*--*", supportedLocales, ResolvedLocale.fromLanguageTags("en", "en")));
  }

  @ParameterizedTest
  @MethodSource
  public void whenResolvingUnsupportedLocale_returnBetterDefaultLocale(
      final String acceptLanguage,
      final Set<SupportedLocale> supportedLocales,
      final ResolvedLocale expectedResolvedLocale) {

    LocalesResolver resolver =
        LocalesResolverBaseImpl.builder()
            .supportedLocales(supportedLocales)
            .defaultResolvedLocale(DEFAULT_LOCALE)
            .build();

    assertThat(resolver.resolve(acceptLanguage), is(expectedResolvedLocale));
  }

  static Stream<Arguments> whenResolvingUnsupportedLocale_returnBetterDefaultLocale() {
    Set<SupportedLocale> supportedLocales =
        // We add 3 different variants of English to the list of supported locales
        Set.of("ar", "en", "en-GB", "en-IN", "fr", "hi", "hr", "id", "ja", "nb", "zh-Hant").stream()
            .map(SupportedLocale::fromLanguageTag)
            .collect(Collectors.toSet());

    return Stream.of(
        // Swedish (Sweden)
        Arguments.of(
            "sv-SE",
            supportedLocales,
            ResolvedLocale.fromLanguageTags("en-GB", List.of("en"), "en-SE")),
        // Estonian (Estonia)
        Arguments.of(
            "et-EE",
            supportedLocales,
            ResolvedLocale.fromLanguageTags("en-GB", List.of("en"), "en-EE")),
        // Dutch (Netherlands)
        Arguments.of(
            "nl-NL",
            supportedLocales,
            ResolvedLocale.fromLanguageTags("en-GB", List.of("en"), "en-NL")),
        // Malayalam (India)
        Arguments.of(
            "ml-IN",
            supportedLocales,
            ResolvedLocale.fromLanguageTags("en-IN", List.of("en"), "en-IN")),
        // Tamil (Malaysia)
        Arguments.of(
            "ta-MY",
            supportedLocales,
            ResolvedLocale.fromLanguageTags("en-GB", List.of("en"), "en-MY")),
        // Nama (Namibia)
        Arguments.of(
            "naq-NA",
            supportedLocales,
            ResolvedLocale.fromLanguageTags("en-GB", List.of("en"), "en-NA")),
        // Dzongkha (Bhutan)
        Arguments.of(
            "dz-BT",
            supportedLocales,
            ResolvedLocale.fromLanguageTags("en-GB", List.of("en"), "en-GB")),
        // Bosnian (Bosnia)
        Arguments.of("bs-BA", supportedLocales, ResolvedLocale.fromLanguageTags("hr", "hr-BA")),
        // Danish (Danemark), should NOT be matched with nb (Norwegian Bokmål)
        Arguments.of(
            "da-DK",
            supportedLocales,
            ResolvedLocale.fromLanguageTags("en-GB", List.of("en"), "en-DK")),
        // Malay (Malaysia), should NOT be matched with id (Indonesian)
        Arguments.of(
            "ms-MY",
            supportedLocales,
            ResolvedLocale.fromLanguageTags("en-GB", List.of("en"), "en-MY")));
  }

  private static final Set<SupportedLocale> SUPPORTED_LOCALES =
      Set.of(
              "af",
              "am",
              "ar",
              "ar-EG",
              "ar-MA",
              "ar-SA",
              "az-Latn",
              "bg",
              "bho",
              "bn",
              "bs",
              "ca",
              "cs",
              "da",
              "de",
              "el",
              "en",
              "en-GB",
              "es",
              "es-419",
              "es-AR",
              "es-MX",
              "et",
              "eu",
              "fa",
              "fi",
              "fil",
              "fr",
              "fr-CA",
              "gl",
              "gu",
              "he",
              "hi",
              "hr",
              "hu",
              "id",
              "is",
              "it",
              "ja",
              "kn",
              "ko",
              "lt",
              "lv",
              "mk",
              "ml",
              "mr",
              "ms",
              "nb",
              "ne",
              "nl",
              "or",
              "pa-Arab",
              "pa-Guru",
              "pl",
              "pt",
              "pt-PT",
              "ro",
              "ru",
              "sk",
              "sl",
              "sr-Latn",
              "sv",
              "sw",
              "ta",
              "te",
              "th",
              "tr",
              "uk",
              "ur",
              "vi",
              "zh-Hans",
              "zh-Hant",
              "zh-Hant-HK",
              "zu")
          .stream()
          .map(SupportedLocale::fromLanguageTag)
          .collect(Collectors.toSet());

  @ParameterizedTest
  @MethodSource
  public void whenResolvingEdgeCaseLocales_returnsExpectedLocale(
      final String givenLanguageTag,
      final String expectedLanguageTagForTranslations,
      final List<String> expectedFallbackLanguageTagsForTranslations,
      final String expectedLanguageTagForFormatting) {

    LocalesResolver resolver =
        LocalesResolverBaseImpl.builder()
            .supportedLocales(SUPPORTED_LOCALES)
            .defaultResolvedLocale(DEFAULT_LOCALE)
            .requiredLocaleAffinity(MUTUALLY_INTELLIGIBLE)
            .build();

    ResolvedLocale expectedResolvedLocale =
        ResolvedLocale.fromLanguageTags(
            expectedLanguageTagForTranslations,
            expectedFallbackLanguageTagsForTranslations,
            expectedLanguageTagForFormatting);
    assertThat(resolver.resolve(givenLanguageTag), is(expectedResolvedLocale));
  }

  static Stream<Arguments> whenResolvingEdgeCaseLocales_returnsExpectedLocale() {
    return Stream.of(
        Arguments.of("en-LK", "en-GB", List.of("en"), "en-GB"),
        Arguments.of("es-419", "es-419", List.of("es"), "es-419"),
        Arguments.of("ga-IE", "en-GB", List.of("en"), "en-IE"),
        Arguments.of("gn-PY", "es-419", List.of("es"), "es-PY"),
        Arguments.of("kk-KZ", "en-GB", List.of("en"), "en-GB"),
        Arguments.of("mn-MN", "en-GB", List.of("en"), "en-GB"),
        Arguments.of("nso-ZA", "en-GB", List.of("en"), "en-ZA"),
        Arguments.of("sq-XK", "en-GB", List.of("en"), "en-GB"),
        Arguments.of("sr-Latn-XK", "sr-Latn", Collections.emptyList(), "sr-Latn-XK"),
        Arguments.of("sr-Cyrl-XK", "sr-Latn", Collections.emptyList(), "sr-Latn-XK"),
        Arguments.of("sr-XK", "sr-Latn", Collections.emptyList(), "sr-Latn-XK"));
  }

  @Test
  public void whenCldrAncestorLocalesAreUnsupported_theyAreNotPresentAsFallbacks() {
    LocalesResolver resolver =
        LocalesResolverBaseImpl.builder()
            .supportedLocales(
                Set.of("en", "es", "es-MX").stream()
                    .map(SupportedLocale::fromLanguageTag)
                    .collect(Collectors.toSet()))
            .defaultResolvedLocale(DEFAULT_LOCALE)
            .build();

    String localeToResolve = "es-419";

    ResolvedLocale expectedResolvedLocale =
        ResolvedLocale.fromLanguageTags(
            "es-MX",
            // Only es should appear as fallback, not es-419
            List.of("es"),
            "es-419");
    assertThat(resolver.resolve(localeToResolve), is(expectedResolvedLocale));
  }

  @ParameterizedTest
  @MethodSource
  public void givenRequiredLocaleAffinity_whenResolvingLocale_returnsExpected(
      final LocaleAffinity requiredAffinity, String input, ResolvedLocale resolvedLocale) {
    final Set<SupportedLocale> supportedLocales =
        Set.of("es", "es-419", "hr", "nb", "id", "ru", "sr-Latn").stream()
            .map(SupportedLocale::fromLanguageTag)
            .collect(Collectors.toSet());

    final LocalesResolver resolver =
        LocalesResolverBaseImpl.builder()
            .supportedLocales(supportedLocales)
            .defaultResolvedLocale(DEFAULT_LOCALE)
            .requiredLocaleAffinity(requiredAffinity)
            .build();

    assertEquals(resolvedLocale, resolver.resolve(input));
  }

  static Stream<Arguments> givenRequiredLocaleAffinity_whenResolvingLocale_returnsExpected() {
    Set<SupportedLocale> supportedLocales =
        Set.of("es", "es-419", "hr", "nb", "id", "ru", "sr-Latn").stream()
            .map(SupportedLocale::fromLanguageTag)
            .collect(Collectors.toSet());

    return Stream.of(
        // SAME affinity
        Arguments.of(SAME, "bs", DEFAULT_LOCALE), // Bosnian should not resolve to Croatian
        Arguments.of(SAME, "da", DEFAULT_LOCALE), // Danish should not resolve to Norwegian Bokmål
        Arguments.of(SAME, "ms", DEFAULT_LOCALE), // Malay should not resolve to Indonesian
        Arguments.of(SAME, "kk", DEFAULT_LOCALE), // Kazakh should not resolve to Russian
        Arguments.of(SAME, "mn", DEFAULT_LOCALE), // Mongolian should not resolve to Russian
        Arguments.of(
            SAME,
            "es-PY",
            ResolvedLocale.fromLanguageTags(
                "es-419", List.of("es"), "es-PY")), // Spanish (Paraguay) should resolve to
        Arguments.of(
            SAME,
            "es-PT",
            ResolvedLocale.fromLanguageTags("es", "es-ES")), // Spanish (Paraguay) should resolve to
        Arguments.of(
            SAME,
            "sr-Cyrl-RS",
            ResolvedLocale.fromLanguageTags(
                "sr-Latn", "sr-Latn")), // Serbian (Cyrillic) should resolve to Serbian (Latin)

        // Mutually Intelligible
        Arguments.of(MUTUALLY_INTELLIGIBLE, "bs", ResolvedLocale.fromLanguageTags("hr", "hr-BA")),
        Arguments.of(
            MUTUALLY_INTELLIGIBLE,
            "da",
            DEFAULT_LOCALE), // Danish should not resolve to Norwegian Bokmål
        Arguments.of(
            MUTUALLY_INTELLIGIBLE, "ms", DEFAULT_LOCALE), // Malay should not resolve to Indonesian
        Arguments.of(
            MUTUALLY_INTELLIGIBLE, "kk", DEFAULT_LOCALE), // Kazakh should not resolve to Russian
        Arguments.of(
            MUTUALLY_INTELLIGIBLE, "mn", DEFAULT_LOCALE), // Mongolian should not resolve to Russian
        Arguments.of(MUTUALLY_INTELLIGIBLE, "eu-ES", DEFAULT_LOCALE),
        Arguments.of(MUTUALLY_INTELLIGIBLE, "ca-AD", DEFAULT_LOCALE),
        // Spanish (Paraguay) should resolve to Spanish (Latin America)
        Arguments.of(
            MUTUALLY_INTELLIGIBLE,
            "es-PY",
            ResolvedLocale.fromLanguageTags("es-419", List.of("es"), "es-PY")),
        // Spanish (Portugal) should resolve to Spanish
        Arguments.of(
            MUTUALLY_INTELLIGIBLE, "es-PT", ResolvedLocale.fromLanguageTags("es", "es-ES")),
        // Serbian (Cyrillic) should resolve to Serbian (Latin)
        Arguments.of(
            MUTUALLY_INTELLIGIBLE,
            "sr-Cyrl-RS",
            ResolvedLocale.fromLanguageTags("sr-Latn", "sr-Latn")),

        // HIGH affinity
        Arguments.of(HIGH, "da", ResolvedLocale.fromLanguageTags("nb", "nb-NO")),
        Arguments.of(HIGH, "da-DK", ResolvedLocale.fromLanguageTags("nb", "nb-NO")),

        // ⚠️ Logic for Malay differs from LocaleAffinityBiCalculatorBaseImpl, it should resolve to
        // Indonesian. This should be fixed somehow, but is considered as acceptable for now.
        Arguments.of(HIGH, "ms", DEFAULT_LOCALE),
        Arguments.of(HIGH, "ms-MY", DEFAULT_LOCALE),
        Arguments.of(HIGH, "mn-KZ", DEFAULT_LOCALE),
        Arguments.of(HIGH, "mn", DEFAULT_LOCALE),
        Arguments.of(HIGH, "eu-ES", DEFAULT_LOCALE),
        Arguments.of(HIGH, "ca-AD", DEFAULT_LOCALE),

        // LOW affinity
        Arguments.of(LOW, "da", ResolvedLocale.fromLanguageTags("nb", "nb-NO")),
        Arguments.of(LOW, "da-DK", ResolvedLocale.fromLanguageTags("nb", "nb-NO")),
        Arguments.of(LOW, "ms", ResolvedLocale.fromLanguageTags("id", "id-ID")),
        Arguments.of(LOW, "ms-MY", ResolvedLocale.fromLanguageTags("id", "id-ID")),
        Arguments.of(LOW, "mn-KZ", ResolvedLocale.fromLanguageTags("ru", "ru-KZ")),
        Arguments.of(LOW, "mn", ResolvedLocale.fromLanguageTags("ru", "ru-RU")),
        Arguments.of(LOW, "eu-ES", ResolvedLocale.fromLanguageTags("es", "es-ES")),
        Arguments.of(LOW, "ca-AD", ResolvedLocale.fromLanguageTags("es", "es-ES")));
  }

  @Test
  @Disabled
  public void testDistance() {
    System.out.println(
        LocaleDistance.INSTANCE.testOnlyDistance(
            ULocale.forLanguageTag("eu-ES"),
            ULocale.forLanguageTag("es"),
            LocaleDistance.INSTANCE.getDefaultScriptDistance(),
            FavorSubtag.LANGUAGE));
    System.out.println(
        LocaleDistance.INSTANCE.testOnlyDistance(
            ULocale.forLanguageTag("ca-AD"),
            ULocale.forLanguageTag("es"),
            LocaleDistance.INSTANCE.getDefaultScriptDistance(),
            FavorSubtag.LANGUAGE));
    System.out.println(
        LocaleDistance.INSTANCE.testOnlyDistance(
            ULocale.forLanguageTag("da"),
            ULocale.forLanguageTag("nb"),
            LocaleDistance.INSTANCE.getDefaultScriptDistance(),
            FavorSubtag.LANGUAGE));
    System.out.println(
        LocaleDistance.INSTANCE.testOnlyDistance(
            ULocale.forLanguageTag("ms"),
            ULocale.forLanguageTag("id"),
            LocaleDistance.INSTANCE.getDefaultScriptDistance(),
            FavorSubtag.LANGUAGE));
  }
}
