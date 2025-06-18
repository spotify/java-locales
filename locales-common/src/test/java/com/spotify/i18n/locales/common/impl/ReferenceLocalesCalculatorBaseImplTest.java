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
import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isSameLocale;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.ULocale.Builder;
import com.spotify.i18n.locales.common.LocaleAffinityBiCalculator;
import com.spotify.i18n.locales.common.ReferenceLocalesCalculator;
import com.spotify.i18n.locales.common.model.LocaleAffinity;
import com.spotify.i18n.locales.common.model.RelatedReferenceLocale;
import com.spotify.i18n.locales.utils.available.AvailableLocalesUtils;
import com.spotify.i18n.locales.utils.language.LanguageUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ReferenceLocalesCalculatorBaseImplTest {

  public static final ReferenceLocalesCalculator REFERENCE_LOCALES_CALCULATOR =
      ReferenceLocalesCalculatorBaseImpl.builder().buildReferenceLocalesCalculator();

  public static final LocaleAffinityBiCalculator LOCALE_AFFINITY_BI_CALCULATOR =
      ReferenceLocalesCalculatorBaseImpl.builder().buildLocaleAffinityBiCalculator();

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

      if (isSameLocale(inputLanguageScriptOnly, referenceLanguageScriptOnly)
          || isSameSpokenLanguage(inputLanguageScriptOnly, referenceLanguageScriptOnly)) {
        assertEquals(
            SAME,
            affinity,
            String.format(
                "Reference locale [%s] for input locale [%s] share the same language & script [%s], Yet, affinity is %s.",
                referenceLocale.toLanguageTag(),
                input.toLanguageTag(),
                inputLanguageScriptOnly.toLanguageTag(),
                affinity));
      } else if (areKnownMutuallyIntelligibleLocales(
          inputLanguageScriptOnly, referenceLanguageScriptOnly)) {
        assertEquals(
            MUTUALLY_INTELLIGIBLE,
            affinity,
            String.format(
                "Reference locale [%s] for input locale [%s] are known mutually intelligible locales, Yet, affinity is %s.",
                referenceLocale.toLanguageTag(),
                input.toLanguageTag(),
                inputLanguageScriptOnly.toLanguageTag(),
                affinity));
      } else {
        assertNotEquals(
            SAME,
            affinity,
            String.format(
                "Reference locale [%s] for input locale [%s] do not share the same language & script. Yet, affinity is %s.",
                referenceLocale.toLanguageTag(), input.toLanguageTag(), affinity));
        assertNotEquals(
            MUTUALLY_INTELLIGIBLE,
            affinity,
            String.format(
                "Reference locale [%s] for input locale [%s] do not share the same language & script. Yet, affinity is %s.",
                referenceLocale.toLanguageTag(), input.toLanguageTag(), affinity));
      }
    }
  }

  private boolean isSameSpokenLanguage(
      ULocale inputLanguageScriptOnly, ULocale referenceLanguageScriptOnly) {
    return LanguageUtils.getSpokenLanguageLocale(inputLanguageScriptOnly.toLanguageTag())
        .equals(LanguageUtils.getSpokenLanguageLocale(referenceLanguageScriptOnly.toLanguageTag()));
  }

  private static ULocale getLocaleWithLanguageAndScriptOnly(ULocale input) {
    return new Builder()
        .setLocale(ULocale.addLikelySubtags(input))
        .setRegion(null)
        .clearExtensions()
        .build();
  }

  private boolean areKnownMutuallyIntelligibleLocales(ULocale inputLS, ULocale referenceLS) {
    String input = inputLS.toLanguageTag();
    String reference = referenceLS.toLanguageTag();

    switch (input) {
        // Bosnian and Croatian
      case "bs-Latn":
      case "bs-Cyrl":
        return reference.equals("hr-Latn");
        // Croatian and Bosnian
      case "hr-Latn":
        return reference.equals("bs-Latn") || reference.equals("bs-Cyrl");
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
    assertEquals(expectedRelatedReferenceLocales.size(), relatedReferenceLocales.size());
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
        Arguments.of("ZH_us", "zh-Hant-MY"),
        Arguments.of("zh-Hant", "zh-TW"),
        Arguments.of("zh-Hant-HK", "zh-HK"),
        Arguments.of("zh-Hant-CN", "zh-Hant-MY"),
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
        rrl("zh-HK", SAME),
        rrl("zh-MO", SAME),
        rrl("zh-TW", SAME),
        rrl("zh-Hant-MY", SAME),
        // Cantonese
        rrl("yue", HIGH),
        rrl("yue-MO", HIGH),
        rrl("yue-Hant-CN", HIGH));
  }

  private static List<RelatedReferenceLocale> danish() {
    return List.of(
        // Danish
        rrl("da", SAME),
        rrl("da-GL", SAME),
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
        // Afrikaans
        rrl("af", LOW),
        rrl("af-NA", LOW),
        // Welsh
        rrl("cy", LOW),
        // English
        rrl("en", SAME),
        rrl("en-001", SAME),
        rrl("en-150", SAME),
        rrl("en-AE", SAME),
        rrl("en-AG", SAME),
        rrl("en-AI", SAME),
        rrl("en-AS", SAME),
        rrl("en-AT", SAME),
        rrl("en-AU", SAME),
        rrl("en-BB", SAME),
        rrl("en-BE", SAME),
        rrl("en-BI", SAME),
        rrl("en-BM", SAME),
        rrl("en-BS", SAME),
        rrl("en-BW", SAME),
        rrl("en-BZ", SAME),
        rrl("en-CA", SAME),
        rrl("en-CC", SAME),
        rrl("en-CH", SAME),
        rrl("en-CK", SAME),
        rrl("en-CM", SAME),
        rrl("en-CX", SAME),
        rrl("en-CY", SAME),
        rrl("en-CZ", SAME),
        rrl("en-DE", SAME),
        rrl("en-DG", SAME),
        rrl("en-DK", SAME),
        rrl("en-DM", SAME),
        rrl("en-ER", SAME),
        rrl("en-ES", SAME),
        rrl("en-FI", SAME),
        rrl("en-FJ", SAME),
        rrl("en-FR", SAME),
        rrl("en-FK", SAME),
        rrl("en-FM", SAME),
        rrl("en-GB", SAME),
        rrl("en-GS", SAME),
        rrl("en-GD", SAME),
        rrl("en-GG", SAME),
        rrl("en-GH", SAME),
        rrl("en-GI", SAME),
        rrl("en-GM", SAME),
        rrl("en-GU", SAME),
        rrl("en-GY", SAME),
        rrl("en-HK", SAME),
        rrl("en-HU", SAME),
        rrl("en-ID", SAME),
        rrl("en-IE", SAME),
        rrl("en-IL", SAME),
        rrl("en-IM", SAME),
        rrl("en-IN", SAME),
        rrl("en-IO", SAME),
        rrl("en-IT", SAME),
        rrl("en-JE", SAME),
        rrl("en-JM", SAME),
        rrl("en-KE", SAME),
        rrl("en-KI", SAME),
        rrl("en-KN", SAME),
        rrl("en-KY", SAME),
        rrl("en-LC", SAME),
        rrl("en-LR", SAME),
        rrl("en-LS", SAME),
        rrl("en-MG", SAME),
        rrl("en-MH", SAME),
        rrl("en-MO", SAME),
        rrl("en-MP", SAME),
        rrl("en-MS", SAME),
        rrl("en-MT", SAME),
        rrl("en-MU", SAME),
        rrl("en-MV", SAME),
        rrl("en-MW", SAME),
        rrl("en-MY", SAME),
        rrl("en-NA", SAME),
        rrl("en-NF", SAME),
        rrl("en-NG", SAME),
        rrl("en-NL", SAME),
        rrl("en-NO", SAME),
        rrl("en-NR", SAME),
        rrl("en-NU", SAME),
        rrl("en-NZ", SAME),
        rrl("en-PG", SAME),
        rrl("en-PH", SAME),
        rrl("en-PK", SAME),
        rrl("en-PL", SAME),
        rrl("en-PN", SAME),
        rrl("en-PR", SAME),
        rrl("en-PT", SAME),
        rrl("en-PW", SAME),
        rrl("en-RO", SAME),
        rrl("en-RW", SAME),
        rrl("en-SB", SAME),
        rrl("en-SC", SAME),
        rrl("en-SD", SAME),
        rrl("en-SE", SAME),
        rrl("en-SG", SAME),
        rrl("en-SH", SAME),
        rrl("en-SI", SAME),
        rrl("en-SK", SAME),
        rrl("en-SL", SAME),
        rrl("en-SS", SAME),
        rrl("en-SX", SAME),
        rrl("en-SZ", SAME),
        rrl("en-TC", SAME),
        rrl("en-TK", SAME),
        rrl("en-TO", SAME),
        rrl("en-TT", SAME),
        rrl("en-TV", SAME),
        rrl("en-TZ", SAME),
        rrl("en-UG", SAME),
        rrl("en-UM", SAME),
        rrl("en-VC", SAME),
        rrl("en-VG", SAME),
        rrl("en-VI", SAME),
        rrl("en-VU", SAME),
        rrl("en-WS", SAME),
        rrl("en-ZA", SAME),
        rrl("en-ZM", SAME),
        rrl("en-ZW", SAME),
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
        rrl("fr", SAME),
        rrl("fr-BE", SAME),
        rrl("fr-BF", SAME),
        rrl("fr-BI", SAME),
        rrl("fr-BJ", SAME),
        rrl("fr-BL", SAME),
        rrl("fr-CA", SAME),
        rrl("fr-CD", SAME),
        rrl("fr-CF", SAME),
        rrl("fr-CG", SAME),
        rrl("fr-CH", SAME),
        rrl("fr-CI", SAME),
        rrl("fr-CM", SAME),
        rrl("fr-DJ", SAME),
        rrl("fr-DZ", SAME),
        rrl("fr-GA", SAME),
        rrl("fr-GF", SAME),
        rrl("fr-GN", SAME),
        rrl("fr-GP", SAME),
        rrl("fr-GQ", SAME),
        rrl("fr-HT", SAME),
        rrl("fr-KM", SAME),
        rrl("fr-LU", SAME),
        rrl("fr-MA", SAME),
        rrl("fr-MC", SAME),
        rrl("fr-MF", SAME),
        rrl("fr-MG", SAME),
        rrl("fr-ML", SAME),
        rrl("fr-MQ", SAME),
        rrl("fr-MR", SAME),
        rrl("fr-MU", SAME),
        rrl("fr-NC", SAME),
        rrl("fr-NE", SAME),
        rrl("fr-PF", SAME),
        rrl("fr-PM", SAME),
        rrl("fr-RE", SAME),
        rrl("fr-RW", SAME),
        rrl("fr-SC", SAME),
        rrl("fr-SN", SAME),
        rrl("fr-SY", SAME),
        rrl("fr-TD", SAME),
        rrl("fr-TG", SAME),
        rrl("fr-TN", SAME),
        rrl("fr-VU", SAME),
        rrl("fr-WF", SAME),
        rrl("fr-YT", SAME),
        // Occitan
        rrl("oc", LOW),
        rrl("oc-ES", LOW));
  }

  private static List<RelatedReferenceLocale> german() {
    return List.of(
        // German
        rrl("de", SAME),
        rrl("de-AT", SAME),
        rrl("de-BE", SAME),
        rrl("de-CH", SAME),
        rrl("de-IT", SAME),
        rrl("de-LI", SAME),
        rrl("de-LU", SAME),
        // Swiss German
        rrl("gsw", MUTUALLY_INTELLIGIBLE),
        rrl("gsw-FR", MUTUALLY_INTELLIGIBLE),
        rrl("gsw-LI", MUTUALLY_INTELLIGIBLE),
        // Luxembourgian
        rrl("lb", MUTUALLY_INTELLIGIBLE),
        // Romansh
        rrl("rm", LOW));
  }

  private static List<RelatedReferenceLocale> italian() {
    return List.of(rrl("it", SAME), rrl("it-CH", SAME), rrl("it-SM", SAME), rrl("it-VA", SAME));
  }

  private static List<RelatedReferenceLocale> norwegian() {
    return List.of(
        // Danish
        rrl("da", HIGH),
        rrl("da-GL", HIGH),
        // Bokmål
        rrl("nb", SAME),
        rrl("nb-SJ", SAME),
        // Norwegian
        rrl("no", SAME),
        // Nynorsk
        rrl("nn", SAME));
  }

  private static List<RelatedReferenceLocale> serbian() {
    return List.of(
        rrl("sr", SAME),
        rrl("sr-BA", SAME),
        rrl("sr-Cyrl-ME", SAME),
        rrl("sr-Latn", SAME),
        rrl("sr-Latn-BA", SAME),
        rrl("sr-Latn-XK", SAME),
        rrl("sr-ME", SAME),
        rrl("sr-XK", SAME));
  }

  private static List<RelatedReferenceLocale> swedish() {
    return List.of(rrl("sv", SAME), rrl("sv-AX", SAME), rrl("sv-FI", SAME));
  }

  @Test
  public void whenCalculatingForOutlierValues_returnsExpected() {
    assertEquals(NONE, LOCALE_AFFINITY_BI_CALCULATOR.calculate(null, null).affinity());
    assertEquals(NONE, LOCALE_AFFINITY_BI_CALCULATOR.calculate("", "").affinity());
    assertEquals(NONE, LOCALE_AFFINITY_BI_CALCULATOR.calculate(null, "").affinity());
    assertEquals(NONE, LOCALE_AFFINITY_BI_CALCULATOR.calculate("", null).affinity());
    assertEquals(NONE, LOCALE_AFFINITY_BI_CALCULATOR.calculate("  ", "    ").affinity());
  }

  @Test
  public void whenCalculatingBestMatchingReferenceLocaleForOutlierValues_returnsExpected() {
    assertEquals(
        Optional.empty(), REFERENCE_LOCALES_CALCULATOR.calculateBestMatchingReferenceLocale(null));
    assertEquals(
        Optional.empty(), REFERENCE_LOCALES_CALCULATOR.calculateBestMatchingReferenceLocale(""));
    assertEquals(
        Optional.empty(), REFERENCE_LOCALES_CALCULATOR.calculateBestMatchingReferenceLocale("   "));
  }

  @Test
  public void whenCalculatingRelatedReferenceLocalesForOutlierValues_returnsExpected() {
    assertEquals(
        Collections.emptyList(),
        REFERENCE_LOCALES_CALCULATOR.calculateRelatedReferenceLocales(null));
    assertEquals(
        Collections.emptyList(), REFERENCE_LOCALES_CALCULATOR.calculateRelatedReferenceLocales(""));
    assertEquals(
        Collections.emptyList(),
        REFERENCE_LOCALES_CALCULATOR.calculateRelatedReferenceLocales("   "));
  }

  @Test
  public void calculateBiAffinity() {
    assertEquals(
        MUTUALLY_INTELLIGIBLE,
        LOCALE_AFFINITY_BI_CALCULATOR.calculate("bs-Latn", "hr-BA").affinity());
    assertEquals(
        MUTUALLY_INTELLIGIBLE,
        LOCALE_AFFINITY_BI_CALCULATOR.calculate("bs-Cyrl", "hr-BA").affinity());
    assertEquals(
        MUTUALLY_INTELLIGIBLE, LOCALE_AFFINITY_BI_CALCULATOR.calculate("bs", "hr-BA").affinity());
    assertEquals(
        MUTUALLY_INTELLIGIBLE, LOCALE_AFFINITY_BI_CALCULATOR.calculate("bs-Latn", "hr").affinity());
  }
}
