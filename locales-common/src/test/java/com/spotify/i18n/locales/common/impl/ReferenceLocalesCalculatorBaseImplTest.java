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
import com.spotify.i18n.locales.common.ReferenceLocalesCalculator;
import com.spotify.i18n.locales.common.model.LocaleAffinity;
import com.spotify.i18n.locales.common.model.RelatedReferenceLocale;
import com.spotify.i18n.locales.utils.available.AvailableLocalesUtils;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ReferenceLocalesCalculatorBaseImplTest {

  public static final ReferenceLocalesCalculator REFERENCE_LOCALES_CALCULATOR =
      ReferenceLocalesCalculatorBaseImpl.builder().build();

  public static Stream<Arguments> validateLocaleAffinityScoreRanges() {
    return AvailableLocalesUtils.getCldrLocales().stream().map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  public void validateLocaleAffinityScoreRanges(final ULocale input) {
    assertTrue(
        REFERENCE_LOCALES_CALCULATOR
            .calculateBestMatchingReferenceLocale(input.toLanguageTag())
            .isPresent());

    List<RelatedReferenceLocale> relatedReferenceLocales =
        REFERENCE_LOCALES_CALCULATOR.calculateRelatedReferenceLocales(input.toLanguageTag());

    ULocale inputLanguageScriptOnly = getLocaleWithLanguageAndScriptOnly(input);

    for (RelatedReferenceLocale relatedReferenceLocale : relatedReferenceLocales) {
      ULocale referenceLocale = relatedReferenceLocale.referenceLocale();
      LocaleAffinity affinity = relatedReferenceLocale.affinity();

      ULocale referenceLanguageScriptOnly = getLocaleWithLanguageAndScriptOnly(referenceLocale);

      if (isSameLocale(inputLanguageScriptOnly, referenceLanguageScriptOnly)) {
        assertEquals(
            SAME_OR_INTERCHANGEABLE,
            affinity,
            String.format(
                "Reference locale [%s] for input locale [%s] share the same language & script [%s], Yet, affinity is %s.",
                referenceLocale.toLanguageTag(),
                input.toLanguageTag(),
                inputLanguageScriptOnly.toLanguageTag(),
                affinity));
      } else if (areKnownInterchangeableLocales(
          inputLanguageScriptOnly, referenceLanguageScriptOnly)) {
        assertEquals(
            SAME_OR_INTERCHANGEABLE,
            affinity,
            String.format(
                "Reference locale [%s] for input locale [%s] are known interchangeable locales, Yet, affinity is %s.",
                referenceLocale.toLanguageTag(),
                input.toLanguageTag(),
                inputLanguageScriptOnly.toLanguageTag(),
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

  private static ULocale getLocaleWithLanguageAndScriptOnly(ULocale input) {
    return new Builder()
        .setLocale(ULocale.addLikelySubtags(input))
        .setRegion(null)
        .clearExtensions()
        .build();
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
  public void whenCalculatingRelatedReferenceLocales_returnsExpected(
      final String input, final List<RelatedReferenceLocale> expectedRelatedReferenceLocales) {
    List<RelatedReferenceLocale> relatedReferenceLocales =
        REFERENCE_LOCALES_CALCULATOR.calculateRelatedReferenceLocales(input);
    assertTrue(
        expectedRelatedReferenceLocales.stream().allMatch(relatedReferenceLocales::contains));
  }

  public static Stream<Arguments> whenCalculatingRelatedReferenceLocales_returnsExpected() {
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
  public void whenCalculatingBestMatchingLocale_returnsExpected(
      final String input, final String expectedLanguageTag) {
    assertThat(
        REFERENCE_LOCALES_CALCULATOR.calculateBestMatchingReferenceLocale(input),
        is(Optional.of(ULocale.forLanguageTag(expectedLanguageTag))));
  }

  public static Stream<Arguments> whenCalculatingBestMatchingLocale_returnsExpected() {
    return Stream.of(
        Arguments.of("zh-Hans-CN", "zh"),
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

  private static RelatedReferenceLocale rrl(
      final String languageTag, final LocaleAffinity affinity) {
    return RelatedReferenceLocale.builder()
        .referenceLocale(ULocale.forLanguageTag(languageTag))
        .affinity(affinity)
        .build();
  }

  private static List<RelatedReferenceLocale> chineseTraditional() {
    return List.of(
        // Traditional Chinese
        rrl("zh-HK", SAME_OR_INTERCHANGEABLE),
        rrl("zh-MO", SAME_OR_INTERCHANGEABLE),
        rrl("zh-TW", SAME_OR_INTERCHANGEABLE),
        // Cantonese
        rrl("yue", HIGH));
  }

  private static List<RelatedReferenceLocale> danish() {
    return List.of(
        // Danish
        rrl("da", SAME_OR_INTERCHANGEABLE),
        rrl("da-GL", SAME_OR_INTERCHANGEABLE),
        // Bokmål
        rrl("nb", HIGH),
        rrl("nb-SJ", HIGH),
        // Norwegian
        rrl("no", HIGH),
        // Faroese
        rrl("fo", LOW),
        rrl("fo-DK", LOW));
  }

  private static List<RelatedReferenceLocale> english() {
    return List.of(
        // Welsh
        rrl("cy", LOW),
        // English
        rrl("en", SAME_OR_INTERCHANGEABLE),
        rrl("en-001", SAME_OR_INTERCHANGEABLE),
        rrl("en-150", SAME_OR_INTERCHANGEABLE),
        rrl("en-AE", SAME_OR_INTERCHANGEABLE),
        rrl("en-AG", SAME_OR_INTERCHANGEABLE),
        rrl("en-AI", SAME_OR_INTERCHANGEABLE),
        rrl("en-AS", SAME_OR_INTERCHANGEABLE),
        rrl("en-AT", SAME_OR_INTERCHANGEABLE),
        rrl("en-AU", SAME_OR_INTERCHANGEABLE),
        rrl("en-BB", SAME_OR_INTERCHANGEABLE),
        rrl("en-BE", SAME_OR_INTERCHANGEABLE),
        rrl("en-BI", SAME_OR_INTERCHANGEABLE),
        rrl("en-BM", SAME_OR_INTERCHANGEABLE),
        rrl("en-BS", SAME_OR_INTERCHANGEABLE),
        rrl("en-BW", SAME_OR_INTERCHANGEABLE),
        rrl("en-BZ", SAME_OR_INTERCHANGEABLE),
        rrl("en-CA", SAME_OR_INTERCHANGEABLE),
        rrl("en-CC", SAME_OR_INTERCHANGEABLE),
        rrl("en-CH", SAME_OR_INTERCHANGEABLE),
        rrl("en-CK", SAME_OR_INTERCHANGEABLE),
        rrl("en-CM", SAME_OR_INTERCHANGEABLE),
        rrl("en-CX", SAME_OR_INTERCHANGEABLE),
        rrl("en-CY", SAME_OR_INTERCHANGEABLE),
        rrl("en-DE", SAME_OR_INTERCHANGEABLE),
        rrl("en-DG", SAME_OR_INTERCHANGEABLE),
        rrl("en-DK", SAME_OR_INTERCHANGEABLE),
        rrl("en-DM", SAME_OR_INTERCHANGEABLE),
        rrl("en-ER", SAME_OR_INTERCHANGEABLE),
        rrl("en-FI", SAME_OR_INTERCHANGEABLE),
        rrl("en-FJ", SAME_OR_INTERCHANGEABLE),
        rrl("en-FK", SAME_OR_INTERCHANGEABLE),
        rrl("en-FM", SAME_OR_INTERCHANGEABLE),
        rrl("en-GB", SAME_OR_INTERCHANGEABLE),
        rrl("en-GD", SAME_OR_INTERCHANGEABLE),
        rrl("en-GG", SAME_OR_INTERCHANGEABLE),
        rrl("en-GH", SAME_OR_INTERCHANGEABLE),
        rrl("en-GI", SAME_OR_INTERCHANGEABLE),
        rrl("en-GM", SAME_OR_INTERCHANGEABLE),
        rrl("en-GU", SAME_OR_INTERCHANGEABLE),
        rrl("en-GY", SAME_OR_INTERCHANGEABLE),
        rrl("en-HK", SAME_OR_INTERCHANGEABLE),
        rrl("en-ID", SAME_OR_INTERCHANGEABLE),
        rrl("en-IE", SAME_OR_INTERCHANGEABLE),
        rrl("en-IL", SAME_OR_INTERCHANGEABLE),
        rrl("en-IM", SAME_OR_INTERCHANGEABLE),
        rrl("en-IN", SAME_OR_INTERCHANGEABLE),
        rrl("en-IO", SAME_OR_INTERCHANGEABLE),
        rrl("en-JE", SAME_OR_INTERCHANGEABLE),
        rrl("en-JM", SAME_OR_INTERCHANGEABLE),
        rrl("en-KE", SAME_OR_INTERCHANGEABLE),
        rrl("en-KI", SAME_OR_INTERCHANGEABLE),
        rrl("en-KN", SAME_OR_INTERCHANGEABLE),
        rrl("en-KY", SAME_OR_INTERCHANGEABLE),
        rrl("en-LC", SAME_OR_INTERCHANGEABLE),
        rrl("en-LR", SAME_OR_INTERCHANGEABLE),
        rrl("en-LS", SAME_OR_INTERCHANGEABLE),
        rrl("en-MG", SAME_OR_INTERCHANGEABLE),
        rrl("en-MH", SAME_OR_INTERCHANGEABLE),
        rrl("en-MO", SAME_OR_INTERCHANGEABLE),
        rrl("en-MP", SAME_OR_INTERCHANGEABLE),
        rrl("en-MS", SAME_OR_INTERCHANGEABLE),
        rrl("en-MT", SAME_OR_INTERCHANGEABLE),
        rrl("en-MU", SAME_OR_INTERCHANGEABLE),
        rrl("en-MV", SAME_OR_INTERCHANGEABLE),
        rrl("en-MW", SAME_OR_INTERCHANGEABLE),
        rrl("en-MY", SAME_OR_INTERCHANGEABLE),
        rrl("en-NA", SAME_OR_INTERCHANGEABLE),
        rrl("en-NF", SAME_OR_INTERCHANGEABLE),
        rrl("en-NG", SAME_OR_INTERCHANGEABLE),
        rrl("en-NL", SAME_OR_INTERCHANGEABLE),
        rrl("en-NR", SAME_OR_INTERCHANGEABLE),
        rrl("en-NU", SAME_OR_INTERCHANGEABLE),
        rrl("en-NZ", SAME_OR_INTERCHANGEABLE),
        rrl("en-PG", SAME_OR_INTERCHANGEABLE),
        rrl("en-PH", SAME_OR_INTERCHANGEABLE),
        rrl("en-PK", SAME_OR_INTERCHANGEABLE),
        rrl("en-PN", SAME_OR_INTERCHANGEABLE),
        rrl("en-PR", SAME_OR_INTERCHANGEABLE),
        rrl("en-PW", SAME_OR_INTERCHANGEABLE),
        rrl("en-RW", SAME_OR_INTERCHANGEABLE),
        rrl("en-SB", SAME_OR_INTERCHANGEABLE),
        rrl("en-SC", SAME_OR_INTERCHANGEABLE),
        rrl("en-SD", SAME_OR_INTERCHANGEABLE),
        rrl("en-SE", SAME_OR_INTERCHANGEABLE),
        rrl("en-SG", SAME_OR_INTERCHANGEABLE),
        rrl("en-SH", SAME_OR_INTERCHANGEABLE),
        rrl("en-SI", SAME_OR_INTERCHANGEABLE),
        rrl("en-SL", SAME_OR_INTERCHANGEABLE),
        rrl("en-SS", SAME_OR_INTERCHANGEABLE),
        rrl("en-SX", SAME_OR_INTERCHANGEABLE),
        rrl("en-SZ", SAME_OR_INTERCHANGEABLE),
        rrl("en-TC", SAME_OR_INTERCHANGEABLE),
        rrl("en-TK", SAME_OR_INTERCHANGEABLE),
        rrl("en-TO", SAME_OR_INTERCHANGEABLE),
        rrl("en-TT", SAME_OR_INTERCHANGEABLE),
        rrl("en-TV", SAME_OR_INTERCHANGEABLE),
        rrl("en-TZ", SAME_OR_INTERCHANGEABLE),
        rrl("en-UG", SAME_OR_INTERCHANGEABLE),
        rrl("en-UM", SAME_OR_INTERCHANGEABLE),
        rrl("en-VC", SAME_OR_INTERCHANGEABLE),
        rrl("en-VG", SAME_OR_INTERCHANGEABLE),
        rrl("en-VI", SAME_OR_INTERCHANGEABLE),
        rrl("en-VU", SAME_OR_INTERCHANGEABLE),
        rrl("en-WS", SAME_OR_INTERCHANGEABLE),
        rrl("en-ZA", SAME_OR_INTERCHANGEABLE),
        rrl("en-ZM", SAME_OR_INTERCHANGEABLE),
        rrl("en-ZW", SAME_OR_INTERCHANGEABLE),
        // Irish
        rrl("ga", LOW),
        rrl("ga-GB", LOW),
        // Scottish Gaelic
        rrl("gd", LOW),
        // Hawaiian
        rrl("haw", LOW),
        // Icelandic
        // https://github.com/unicode-org/cldr/blame/main/common/supplemental/languageInfo.xml#L80
        rrl("is", LOW),
        // Maori
        rrl("mi", LOW),
        // Nigerian Pidgin
        rrl("pcm", LOW));
  }

  private static List<RelatedReferenceLocale> french() {
    return List.of(
        // Breton
        rrl("br", LOW),
        // French
        rrl("fr", SAME_OR_INTERCHANGEABLE),
        rrl("fr-BE", SAME_OR_INTERCHANGEABLE),
        rrl("fr-BF", SAME_OR_INTERCHANGEABLE),
        rrl("fr-BI", SAME_OR_INTERCHANGEABLE),
        rrl("fr-BJ", SAME_OR_INTERCHANGEABLE),
        rrl("fr-BL", SAME_OR_INTERCHANGEABLE),
        rrl("fr-CA", SAME_OR_INTERCHANGEABLE),
        rrl("fr-CD", SAME_OR_INTERCHANGEABLE),
        rrl("fr-CF", SAME_OR_INTERCHANGEABLE),
        rrl("fr-CG", SAME_OR_INTERCHANGEABLE),
        rrl("fr-CH", SAME_OR_INTERCHANGEABLE),
        rrl("fr-CI", SAME_OR_INTERCHANGEABLE),
        rrl("fr-CM", SAME_OR_INTERCHANGEABLE),
        rrl("fr-DJ", SAME_OR_INTERCHANGEABLE),
        rrl("fr-DZ", SAME_OR_INTERCHANGEABLE),
        rrl("fr-GA", SAME_OR_INTERCHANGEABLE),
        rrl("fr-GF", SAME_OR_INTERCHANGEABLE),
        rrl("fr-GN", SAME_OR_INTERCHANGEABLE),
        rrl("fr-GP", SAME_OR_INTERCHANGEABLE),
        rrl("fr-GQ", SAME_OR_INTERCHANGEABLE),
        rrl("fr-HT", SAME_OR_INTERCHANGEABLE),
        rrl("fr-KM", SAME_OR_INTERCHANGEABLE),
        rrl("fr-LU", SAME_OR_INTERCHANGEABLE),
        rrl("fr-MA", SAME_OR_INTERCHANGEABLE),
        rrl("fr-MC", SAME_OR_INTERCHANGEABLE),
        rrl("fr-MF", SAME_OR_INTERCHANGEABLE),
        rrl("fr-MG", SAME_OR_INTERCHANGEABLE),
        rrl("fr-ML", SAME_OR_INTERCHANGEABLE),
        rrl("fr-MQ", SAME_OR_INTERCHANGEABLE),
        rrl("fr-MR", SAME_OR_INTERCHANGEABLE),
        rrl("fr-MU", SAME_OR_INTERCHANGEABLE),
        rrl("fr-NC", SAME_OR_INTERCHANGEABLE),
        rrl("fr-NE", SAME_OR_INTERCHANGEABLE),
        rrl("fr-PF", SAME_OR_INTERCHANGEABLE),
        rrl("fr-PM", SAME_OR_INTERCHANGEABLE),
        rrl("fr-RE", SAME_OR_INTERCHANGEABLE),
        rrl("fr-RW", SAME_OR_INTERCHANGEABLE),
        rrl("fr-SC", SAME_OR_INTERCHANGEABLE),
        rrl("fr-SN", SAME_OR_INTERCHANGEABLE),
        rrl("fr-SY", SAME_OR_INTERCHANGEABLE),
        rrl("fr-TD", SAME_OR_INTERCHANGEABLE),
        rrl("fr-TG", SAME_OR_INTERCHANGEABLE),
        rrl("fr-TN", SAME_OR_INTERCHANGEABLE),
        rrl("fr-VU", SAME_OR_INTERCHANGEABLE),
        rrl("fr-WF", SAME_OR_INTERCHANGEABLE),
        rrl("fr-YT", SAME_OR_INTERCHANGEABLE),
        // Occitan
        rrl("oc", LOW),
        rrl("oc-ES", LOW));
  }

  private static List<RelatedReferenceLocale> german() {
    return List.of(
        // German
        rrl("de", SAME_OR_INTERCHANGEABLE),
        rrl("de-AT", SAME_OR_INTERCHANGEABLE),
        rrl("de-BE", SAME_OR_INTERCHANGEABLE),
        rrl("de-CH", SAME_OR_INTERCHANGEABLE),
        rrl("de-IT", SAME_OR_INTERCHANGEABLE),
        rrl("de-LI", SAME_OR_INTERCHANGEABLE),
        rrl("de-LU", SAME_OR_INTERCHANGEABLE),
        // Swiss German
        rrl("gsw", SAME_OR_INTERCHANGEABLE),
        rrl("gsw-FR", SAME_OR_INTERCHANGEABLE),
        rrl("gsw-LI", SAME_OR_INTERCHANGEABLE),
        // Luxembourgian
        rrl("lb", SAME_OR_INTERCHANGEABLE),
        // Romansh
        rrl("rm", LOW));
  }

  private static List<RelatedReferenceLocale> italian() {
    return List.of(
        rrl("it", SAME_OR_INTERCHANGEABLE),
        rrl("it-CH", SAME_OR_INTERCHANGEABLE),
        rrl("it-SM", SAME_OR_INTERCHANGEABLE),
        rrl("it-VA", SAME_OR_INTERCHANGEABLE));
  }

  private static List<RelatedReferenceLocale> norwegian() {
    return List.of(
        // Danish
        rrl("da", HIGH),
        rrl("da-GL", HIGH),
        // Bokmål
        rrl("nb", SAME_OR_INTERCHANGEABLE),
        rrl("nb-SJ", SAME_OR_INTERCHANGEABLE),
        // Norwegian
        rrl("no", SAME_OR_INTERCHANGEABLE),
        // Nynorsk
        rrl("nn", LOW));
  }

  private static List<RelatedReferenceLocale> serbian() {
    return List.of(
        rrl("sr", SAME_OR_INTERCHANGEABLE),
        rrl("sr-BA", SAME_OR_INTERCHANGEABLE),
        rrl("sr-Cyrl-ME", SAME_OR_INTERCHANGEABLE),
        rrl("sr-Latn", SAME_OR_INTERCHANGEABLE),
        rrl("sr-Latn-BA", SAME_OR_INTERCHANGEABLE),
        rrl("sr-Latn-XK", SAME_OR_INTERCHANGEABLE),
        rrl("sr-ME", SAME_OR_INTERCHANGEABLE),
        rrl("sr-XK", SAME_OR_INTERCHANGEABLE));
  }

  private static List<RelatedReferenceLocale> swedish() {
    return List.of(
        rrl("sv", SAME_OR_INTERCHANGEABLE),
        rrl("sv-AX", SAME_OR_INTERCHANGEABLE),
        rrl("sv-FI", SAME_OR_INTERCHANGEABLE));
  }
}
