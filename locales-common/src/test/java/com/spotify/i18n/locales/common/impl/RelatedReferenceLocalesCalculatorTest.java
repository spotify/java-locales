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
import static com.spotify.i18n.locales.common.model.LocaleAffinity.SAME_OR_INTERCHANGEABLE;
import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isSameLocale;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.ULocale.Builder;
import com.spotify.i18n.locales.common.RelatedReferenceLocalesCalculator;
import com.spotify.i18n.locales.common.model.LocaleAffinity;
import com.spotify.i18n.locales.common.model.RelatedReferenceLocale;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RelatedReferenceLocalesCalculatorTest {

  public static final ULocale EN_US_POSIX = ULocale.forLanguageTag("en-US-POSIX");

  public static Stream<Arguments> validateReferenceLocaleAffinityScoreRanges() {
    return Arrays.stream(ULocale.getAvailableLocales())
        .filter(l -> !isSameLocale(l, EN_US_POSIX))
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  public void validateReferenceLocaleAffinityScoreRanges(final ULocale input) {
    RelatedReferenceLocalesCalculator oneWayAffinity =
        RelatedReferenceLocalesCalculatorBaseImpl.builder().build();
    assertTrue(
        oneWayAffinity
            .getBestMatchingReferenceLocaleForTargetLocale(input.toLanguageTag())
            .isPresent());

    List<RelatedReferenceLocale> relatedReferenceLocales =
        oneWayAffinity.getRelatedReferenceLocalesForSupportedLocale(input.toLanguageTag());

    ULocale inputLS =
        new Builder()
            .setLocale(ULocale.addLikelySubtags(input))
            .setRegion(null)
            .clearExtensions()
            .build();

    for (RelatedReferenceLocale relatedReferenceLocale : relatedReferenceLocales) {
      ULocale referenceLocale = relatedReferenceLocale.referenceLocale();
      LocaleAffinity affinity = relatedReferenceLocale.affinity();

      ULocale referenceLS =
          new Builder()
              .setLocale(ULocale.addLikelySubtags(referenceLocale))
              .setRegion(null)
              .clearExtensions()
              .build();

      if (isSameLocale(inputLS, referenceLS)) {
        assertEquals(
            SAME_OR_INTERCHANGEABLE,
            affinity,
            String.format(
                "Reference locale [%s] for input locale [%s] share the same language & script [%s], Yet, affinity is %s.",
                referenceLocale.toLanguageTag(),
                input.toLanguageTag(),
                inputLS.toLanguageTag(),
                affinity));
      } else if (areKnownInterchangeableLocales(inputLS, referenceLS)) {
        assertEquals(
            SAME_OR_INTERCHANGEABLE,
            affinity,
            String.format(
                "Reference locale [%s] for input locale [%s] are known interchangeable locales, Yet, affinity is %s.",
                referenceLocale.toLanguageTag(),
                input.toLanguageTag(),
                inputLS.toLanguageTag(),
                affinity));
      } else {
        assertNotEquals(
            SAME_OR_INTERCHANGEABLE,
            affinity,
            String.format(
                "Reference locale [%s] for input locale [%s] do not share the same language & script. Yet, affinity is %s.",
                referenceLocale.toLanguageTag(), input.toLanguageTag(), affinity));
      }
    }
  }

  private boolean areKnownInterchangeableLocales(ULocale inputLS, ULocale referenceLS) {
    String input = inputLS.toLanguageTag();
    String reference = referenceLS.toLanguageTag();

    switch (input) {
        // Bosnian and Croatian
      case "bs-Latn":
        return reference.equals("hr-Latn");
        // Croatian and Bosnian
      case "hr-Latn":
        return reference.equals("bs-Latn");
        // German and Luxembourgish or Swiss German
      case "de-Latn":
        return reference.equals("lb-Latn") || reference.equals("gsw-Latn");
        // Luxembourgish and German
      case "lb-Latn":
        return reference.equals("de-Latn");
        // Swiss German and German
      case "gsw-Latn":
        return reference.equals("de-Latn");
        // Bokmål and Norwegian
      case "nb-Latn":
        return reference.equals("no-Latn");
        // Norwegian and Bokmål
      case "no-Latn":
        return reference.equals("nb-Latn");
        // Serbian (Latin script) and Serbian (Cyrillic script)
      case "sr-Latn":
        return reference.equals("sr-Cyrl");
        // Serbian (Cyrillic script) and Serbian (Latin script)
      case "sr-Cyrl":
        return reference.equals("sr-Latn");
      default:
        return false;
    }
  }

  @ParameterizedTest
  @MethodSource
  public void getGettingCorrespondingReferenceLocales_returnsExpected(
      final String input, final List<RelatedReferenceLocale> expectedRelatedReferenceLocales) {
    RelatedReferenceLocalesCalculator joiner =
        RelatedReferenceLocalesCalculatorBaseImpl.builder().build();
    List<RelatedReferenceLocale> relatedReferenceLocaleForAffinities =
        joiner.getRelatedReferenceLocalesForSupportedLocale(input);

    assertTrue(
        relatedReferenceLocaleForAffinities.stream()
            .allMatch(expectedRelatedReferenceLocales::contains));
  }

  public static Stream<Arguments> getGettingCorrespondingReferenceLocales_returnsExpected() {
    return Stream.of(
        Arguments.of("da-SE", danish()),
        Arguments.of("de-NL", german()),
        Arguments.of("en-US", english()),
        Arguments.of("fr-BE", french()),
        Arguments.of("it-SE", italian()),
        Arguments.of("nb", norwegian()),
        Arguments.of("sr-RS", serbian()),
        Arguments.of("sv-Latn-SE", swedish()),
        Arguments.of("ZH_us", chineseTraditional()),
        Arguments.of("zh-Hant", chineseTraditional()),
        Arguments.of("zh-Hant-HK", chineseTraditional()),
        Arguments.of("zh-Hant-CN", chineseTraditional()));
  }

  @ParameterizedTest
  @MethodSource
  public void getBestMatchingReferenceLocaleForTargetLocale_returnsExpected(
      final String input, final String expectedLanguageTag) {
    RelatedReferenceLocalesCalculator joiner =
        RelatedReferenceLocalesCalculatorBaseImpl.builder().build();
    assertThat(
        joiner.getBestMatchingReferenceLocaleForTargetLocale(input),
        is(Optional.of(ULocale.forLanguageTag(expectedLanguageTag))));
  }

  public static Stream<Arguments> getBestMatchingReferenceLocaleForTargetLocale_returnsExpected() {
    return Stream.of(
        Arguments.of("ZH_us", "zh-TW"),
        Arguments.of("zh-Hant", "zh-TW"),
        Arguments.of("zh-Hant-HK", "zh-HK"),
        Arguments.of("zh-Hant-CN", "zh-TW"),
        Arguments.of("fr-Latn-FR", "fr"),
        Arguments.of("fr-BE", "fr-BE"),
        Arguments.of("sr-RS", "sr"),
        Arguments.of("en-CA", "en-CA"),
        Arguments.of("en-US", "en"));
  }

  private static RelatedReferenceLocale rl(
      final String languageTag, final LocaleAffinity affinity) {
    return RelatedReferenceLocale.builder()
        .referenceLocale(ULocale.forLanguageTag(languageTag))
        .affinity(affinity)
        .build();
  }

  private static List<RelatedReferenceLocale> chineseTraditional() {
    return List.of(
        // Traditional Chinese
        rl("zh-HK", SAME_OR_INTERCHANGEABLE),
        rl("zh-MO", SAME_OR_INTERCHANGEABLE),
        rl("zh-TW", SAME_OR_INTERCHANGEABLE),
        // Cantonese
        rl("yue", HIGH));
  }

  private static List<RelatedReferenceLocale> danish() {
    return List.of(
        // Danish
        rl("da", SAME_OR_INTERCHANGEABLE),
        rl("da-GL", SAME_OR_INTERCHANGEABLE),
        // Bokmål
        rl("nb", HIGH),
        rl("nb-SJ", HIGH),
        // Norwegian
        rl("no", HIGH),
        // Faroese
        rl("fo", LOW),
        rl("fo-DK", LOW));
  }

  private static List<RelatedReferenceLocale> english() {
    return List.of(
        // Welsh
        rl("cy", LOW),
        // English
        rl("en", SAME_OR_INTERCHANGEABLE),
        rl("en-001", SAME_OR_INTERCHANGEABLE),
        rl("en-150", SAME_OR_INTERCHANGEABLE),
        rl("en-AE", SAME_OR_INTERCHANGEABLE),
        rl("en-AG", SAME_OR_INTERCHANGEABLE),
        rl("en-AI", SAME_OR_INTERCHANGEABLE),
        rl("en-AS", SAME_OR_INTERCHANGEABLE),
        rl("en-AT", SAME_OR_INTERCHANGEABLE),
        rl("en-AU", SAME_OR_INTERCHANGEABLE),
        rl("en-BB", SAME_OR_INTERCHANGEABLE),
        rl("en-BE", SAME_OR_INTERCHANGEABLE),
        rl("en-BI", SAME_OR_INTERCHANGEABLE),
        rl("en-BM", SAME_OR_INTERCHANGEABLE),
        rl("en-BS", SAME_OR_INTERCHANGEABLE),
        rl("en-BW", SAME_OR_INTERCHANGEABLE),
        rl("en-BZ", SAME_OR_INTERCHANGEABLE),
        rl("en-CA", SAME_OR_INTERCHANGEABLE),
        rl("en-CC", SAME_OR_INTERCHANGEABLE),
        rl("en-CH", SAME_OR_INTERCHANGEABLE),
        rl("en-CK", SAME_OR_INTERCHANGEABLE),
        rl("en-CM", SAME_OR_INTERCHANGEABLE),
        rl("en-CX", SAME_OR_INTERCHANGEABLE),
        rl("en-CY", SAME_OR_INTERCHANGEABLE),
        rl("en-DE", SAME_OR_INTERCHANGEABLE),
        rl("en-DG", SAME_OR_INTERCHANGEABLE),
        rl("en-DK", SAME_OR_INTERCHANGEABLE),
        rl("en-DM", SAME_OR_INTERCHANGEABLE),
        rl("en-ER", SAME_OR_INTERCHANGEABLE),
        rl("en-FI", SAME_OR_INTERCHANGEABLE),
        rl("en-FJ", SAME_OR_INTERCHANGEABLE),
        rl("en-FK", SAME_OR_INTERCHANGEABLE),
        rl("en-FM", SAME_OR_INTERCHANGEABLE),
        rl("en-GB", SAME_OR_INTERCHANGEABLE),
        rl("en-GD", SAME_OR_INTERCHANGEABLE),
        rl("en-GG", SAME_OR_INTERCHANGEABLE),
        rl("en-GH", SAME_OR_INTERCHANGEABLE),
        rl("en-GI", SAME_OR_INTERCHANGEABLE),
        rl("en-GM", SAME_OR_INTERCHANGEABLE),
        rl("en-GU", SAME_OR_INTERCHANGEABLE),
        rl("en-GY", SAME_OR_INTERCHANGEABLE),
        rl("en-HK", SAME_OR_INTERCHANGEABLE),
        rl("en-ID", SAME_OR_INTERCHANGEABLE),
        rl("en-IE", SAME_OR_INTERCHANGEABLE),
        rl("en-IL", SAME_OR_INTERCHANGEABLE),
        rl("en-IM", SAME_OR_INTERCHANGEABLE),
        rl("en-IN", SAME_OR_INTERCHANGEABLE),
        rl("en-IO", SAME_OR_INTERCHANGEABLE),
        rl("en-JE", SAME_OR_INTERCHANGEABLE),
        rl("en-JM", SAME_OR_INTERCHANGEABLE),
        rl("en-KE", SAME_OR_INTERCHANGEABLE),
        rl("en-KI", SAME_OR_INTERCHANGEABLE),
        rl("en-KN", SAME_OR_INTERCHANGEABLE),
        rl("en-KY", SAME_OR_INTERCHANGEABLE),
        rl("en-LC", SAME_OR_INTERCHANGEABLE),
        rl("en-LR", SAME_OR_INTERCHANGEABLE),
        rl("en-LS", SAME_OR_INTERCHANGEABLE),
        rl("en-MG", SAME_OR_INTERCHANGEABLE),
        rl("en-MH", SAME_OR_INTERCHANGEABLE),
        rl("en-MO", SAME_OR_INTERCHANGEABLE),
        rl("en-MP", SAME_OR_INTERCHANGEABLE),
        rl("en-MS", SAME_OR_INTERCHANGEABLE),
        rl("en-MT", SAME_OR_INTERCHANGEABLE),
        rl("en-MU", SAME_OR_INTERCHANGEABLE),
        rl("en-MV", SAME_OR_INTERCHANGEABLE),
        rl("en-MW", SAME_OR_INTERCHANGEABLE),
        rl("en-MY", SAME_OR_INTERCHANGEABLE),
        rl("en-NA", SAME_OR_INTERCHANGEABLE),
        rl("en-NF", SAME_OR_INTERCHANGEABLE),
        rl("en-NG", SAME_OR_INTERCHANGEABLE),
        rl("en-NL", SAME_OR_INTERCHANGEABLE),
        rl("en-NR", SAME_OR_INTERCHANGEABLE),
        rl("en-NU", SAME_OR_INTERCHANGEABLE),
        rl("en-NZ", SAME_OR_INTERCHANGEABLE),
        rl("en-PG", SAME_OR_INTERCHANGEABLE),
        rl("en-PH", SAME_OR_INTERCHANGEABLE),
        rl("en-PK", SAME_OR_INTERCHANGEABLE),
        rl("en-PN", SAME_OR_INTERCHANGEABLE),
        rl("en-PR", SAME_OR_INTERCHANGEABLE),
        rl("en-PW", SAME_OR_INTERCHANGEABLE),
        rl("en-RW", SAME_OR_INTERCHANGEABLE),
        rl("en-SB", SAME_OR_INTERCHANGEABLE),
        rl("en-SC", SAME_OR_INTERCHANGEABLE),
        rl("en-SD", SAME_OR_INTERCHANGEABLE),
        rl("en-SE", SAME_OR_INTERCHANGEABLE),
        rl("en-SG", SAME_OR_INTERCHANGEABLE),
        rl("en-SH", SAME_OR_INTERCHANGEABLE),
        rl("en-SI", SAME_OR_INTERCHANGEABLE),
        rl("en-SL", SAME_OR_INTERCHANGEABLE),
        rl("en-SS", SAME_OR_INTERCHANGEABLE),
        rl("en-SX", SAME_OR_INTERCHANGEABLE),
        rl("en-SZ", SAME_OR_INTERCHANGEABLE),
        rl("en-TC", SAME_OR_INTERCHANGEABLE),
        rl("en-TK", SAME_OR_INTERCHANGEABLE),
        rl("en-TO", SAME_OR_INTERCHANGEABLE),
        rl("en-TT", SAME_OR_INTERCHANGEABLE),
        rl("en-TV", SAME_OR_INTERCHANGEABLE),
        rl("en-TZ", SAME_OR_INTERCHANGEABLE),
        rl("en-UG", SAME_OR_INTERCHANGEABLE),
        rl("en-UM", SAME_OR_INTERCHANGEABLE),
        rl("en-VC", SAME_OR_INTERCHANGEABLE),
        rl("en-VG", SAME_OR_INTERCHANGEABLE),
        rl("en-VI", SAME_OR_INTERCHANGEABLE),
        rl("en-VU", SAME_OR_INTERCHANGEABLE),
        rl("en-WS", SAME_OR_INTERCHANGEABLE),
        rl("en-ZA", SAME_OR_INTERCHANGEABLE),
        rl("en-ZM", SAME_OR_INTERCHANGEABLE),
        rl("en-ZW", SAME_OR_INTERCHANGEABLE),
        // Irish
        rl("ga", LOW),
        rl("ga-GB", LOW),
        // Scottish Gaelic
        rl("gd", LOW),
        // Hawaiian
        rl("haw", LOW),
        // Icelandic
        // https://github.com/unicode-org/cldr/blame/main/common/supplemental/languageInfo.xml#L80
        rl("is", LOW),
        // Maori
        rl("mi", LOW),
        // Nigerian Pidgin
        rl("pcm", LOW));
  }

  private static List<RelatedReferenceLocale> french() {
    return List.of(
        // Breton
        rl("br", LOW),
        // French
        rl("fr", SAME_OR_INTERCHANGEABLE),
        rl("fr-BE", SAME_OR_INTERCHANGEABLE),
        rl("fr-BF", SAME_OR_INTERCHANGEABLE),
        rl("fr-BI", SAME_OR_INTERCHANGEABLE),
        rl("fr-BJ", SAME_OR_INTERCHANGEABLE),
        rl("fr-BL", SAME_OR_INTERCHANGEABLE),
        rl("fr-CA", SAME_OR_INTERCHANGEABLE),
        rl("fr-CD", SAME_OR_INTERCHANGEABLE),
        rl("fr-CF", SAME_OR_INTERCHANGEABLE),
        rl("fr-CG", SAME_OR_INTERCHANGEABLE),
        rl("fr-CH", SAME_OR_INTERCHANGEABLE),
        rl("fr-CI", SAME_OR_INTERCHANGEABLE),
        rl("fr-CM", SAME_OR_INTERCHANGEABLE),
        rl("fr-DJ", SAME_OR_INTERCHANGEABLE),
        rl("fr-DZ", SAME_OR_INTERCHANGEABLE),
        rl("fr-GA", SAME_OR_INTERCHANGEABLE),
        rl("fr-GF", SAME_OR_INTERCHANGEABLE),
        rl("fr-GN", SAME_OR_INTERCHANGEABLE),
        rl("fr-GP", SAME_OR_INTERCHANGEABLE),
        rl("fr-GQ", SAME_OR_INTERCHANGEABLE),
        rl("fr-HT", SAME_OR_INTERCHANGEABLE),
        rl("fr-KM", SAME_OR_INTERCHANGEABLE),
        rl("fr-LU", SAME_OR_INTERCHANGEABLE),
        rl("fr-MA", SAME_OR_INTERCHANGEABLE),
        rl("fr-MC", SAME_OR_INTERCHANGEABLE),
        rl("fr-MF", SAME_OR_INTERCHANGEABLE),
        rl("fr-MG", SAME_OR_INTERCHANGEABLE),
        rl("fr-ML", SAME_OR_INTERCHANGEABLE),
        rl("fr-MQ", SAME_OR_INTERCHANGEABLE),
        rl("fr-MR", SAME_OR_INTERCHANGEABLE),
        rl("fr-MU", SAME_OR_INTERCHANGEABLE),
        rl("fr-NC", SAME_OR_INTERCHANGEABLE),
        rl("fr-NE", SAME_OR_INTERCHANGEABLE),
        rl("fr-PF", SAME_OR_INTERCHANGEABLE),
        rl("fr-PM", SAME_OR_INTERCHANGEABLE),
        rl("fr-RE", SAME_OR_INTERCHANGEABLE),
        rl("fr-RW", SAME_OR_INTERCHANGEABLE),
        rl("fr-SC", SAME_OR_INTERCHANGEABLE),
        rl("fr-SN", SAME_OR_INTERCHANGEABLE),
        rl("fr-SY", SAME_OR_INTERCHANGEABLE),
        rl("fr-TD", SAME_OR_INTERCHANGEABLE),
        rl("fr-TG", SAME_OR_INTERCHANGEABLE),
        rl("fr-TN", SAME_OR_INTERCHANGEABLE),
        rl("fr-VU", SAME_OR_INTERCHANGEABLE),
        rl("fr-WF", SAME_OR_INTERCHANGEABLE),
        rl("fr-YT", SAME_OR_INTERCHANGEABLE),
        // Occitan
        rl("oc", LOW),
        rl("oc-ES", LOW));
  }

  private static List<RelatedReferenceLocale> german() {
    return List.of(
        // German
        rl("de", SAME_OR_INTERCHANGEABLE),
        rl("de-AT", SAME_OR_INTERCHANGEABLE),
        rl("de-BE", SAME_OR_INTERCHANGEABLE),
        rl("de-CH", SAME_OR_INTERCHANGEABLE),
        rl("de-IT", SAME_OR_INTERCHANGEABLE),
        rl("de-LI", SAME_OR_INTERCHANGEABLE),
        rl("de-LU", SAME_OR_INTERCHANGEABLE),
        // Swiss German
        rl("gsw", SAME_OR_INTERCHANGEABLE),
        rl("gsw-FR", SAME_OR_INTERCHANGEABLE),
        rl("gsw-LI", SAME_OR_INTERCHANGEABLE),
        // Luxembourgian
        rl("lb", SAME_OR_INTERCHANGEABLE),
        // Romansh
        rl("rm", LOW));
  }

  private static List<RelatedReferenceLocale> italian() {
    return List.of(
        rl("it", SAME_OR_INTERCHANGEABLE),
        rl("it-CH", SAME_OR_INTERCHANGEABLE),
        rl("it-SM", SAME_OR_INTERCHANGEABLE),
        rl("it-VA", SAME_OR_INTERCHANGEABLE));
  }

  private static List<RelatedReferenceLocale> norwegian() {
    return List.of(
        // Danish
        rl("da", HIGH),
        rl("da-GL", HIGH),
        // Bokmål
        rl("nb", SAME_OR_INTERCHANGEABLE),
        rl("nb-SJ", SAME_OR_INTERCHANGEABLE),
        // Norwegian
        rl("no", SAME_OR_INTERCHANGEABLE),
        // Nynorsk
        rl("nn", LOW));
  }

  private static List<RelatedReferenceLocale> serbian() {
    return List.of(
        rl("sr", SAME_OR_INTERCHANGEABLE),
        rl("sr-BA", SAME_OR_INTERCHANGEABLE),
        rl("sr-Cyrl-ME", SAME_OR_INTERCHANGEABLE),
        rl("sr-Latn", SAME_OR_INTERCHANGEABLE),
        rl("sr-Latn-BA", SAME_OR_INTERCHANGEABLE),
        rl("sr-Latn-XK", SAME_OR_INTERCHANGEABLE),
        rl("sr-ME", SAME_OR_INTERCHANGEABLE),
        rl("sr-XK", SAME_OR_INTERCHANGEABLE));
  }

  private static List<RelatedReferenceLocale> swedish() {
    return List.of(
        rl("sv", SAME_OR_INTERCHANGEABLE),
        rl("sv-AX", SAME_OR_INTERCHANGEABLE),
        rl("sv-FI", SAME_OR_INTERCHANGEABLE));
  }
}
