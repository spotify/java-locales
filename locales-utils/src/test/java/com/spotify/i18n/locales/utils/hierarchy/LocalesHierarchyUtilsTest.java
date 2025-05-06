/*-
 * -\-\-
 * locales-utils
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

package com.spotify.i18n.locales.utils.hierarchy;

import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.LANGUAGE_CODES_WITH_MULTIPLE_SCRIPTS_IN_CLDR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.utils.available.AvailableLocalesUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LocalesHierarchyUtilsTest {

  @Test
  void languageCodesWithMultipleScriptsInCldr() {
    final Set<String> expectedLanguageCodesWithMultipleScriptsInCldr =
        Set.of(
            "az", "bs", "ff", "hi", "kk", "kok", "ks", "kxv", "mni", "pa", "sat", "sd", "shi", "sr",
            "su", "uz", "vai", "yue", "zh");

    assertEquals(
        expectedLanguageCodesWithMultipleScriptsInCldr.size(),
        LANGUAGE_CODES_WITH_MULTIPLE_SCRIPTS_IN_CLDR.size());

    expectedLanguageCodesWithMultipleScriptsInCldr.forEach(
        languageCode ->
            assertTrue(LANGUAGE_CODES_WITH_MULTIPLE_SCRIPTS_IN_CLDR.contains(languageCode)));
  }

  @ParameterizedTest
  @MethodSource
  void getDescendantLocales(String languageTag, String allDescendantLanguageTags) {
    Set<ULocale> resulting =
        LocalesHierarchyUtils.getDescendantLocales(ULocale.forLanguageTag(languageTag));
    assertThat(resulting, is(setFromString(allDescendantLanguageTags)));
  }

  static Stream<Arguments> getDescendantLocales() {
    return Map.of(
            "",
            String.join(
                ",",
                AvailableLocalesUtils.getCldrLocales().stream()
                    .map(ULocale::toLanguageTag)
                    .collect(Collectors.toSet())),
            "fr",
            "fr-BE,fr-BF,fr-BI,fr-BJ,fr-BL,fr-CA,fr-CD,fr-CF,fr-CG,fr-CH,fr-CI,fr-CM,fr-DJ,fr-DZ,fr-FR,fr-GA,fr-GF,fr-GN,fr-GP,fr-GQ,fr-HT,fr-KM,fr-LU,fr-MA,fr-MC,fr-MF,fr-MG,fr-ML,fr-MQ,fr-MR,fr-MU,fr-NC,fr-NE,fr-PF,fr-PM,fr-RE,fr-RW,fr-SC,fr-SN,fr-SY,fr-TD,fr-TG,fr-TN,fr-VU,fr-WF,fr-YT",
            "en",
            "en-001,en-150,en-AE,en-AG,en-AI,en-AS,en-AT,en-AU,en-BB,en-BE,en-BI,en-BM,en-BS,en-BW,en-BZ,en-CA,en-CC,en-CH,en-CK,en-CM,en-CX,en-CY,en-CZ,en-DE,en-DG,en-DK,en-DM,en-ER,en-ES,en-FI,en-FJ,en-FK,en-FM,en-FR,en-GB,en-GD,en-GG,en-GH,en-GI,en-GM,en-GS,en-GU,en-GY,en-HK,en-HU,en-ID,en-IE,en-IL,en-IM,en-IN,en-IO,en-IT,en-JE,en-JM,en-KE,en-KI,en-KN,en-KY,en-LC,en-LR,en-LS,en-MG,en-MH,en-MO,en-MP,en-MS,en-MT,en-MU,en-MV,en-MW,en-MY,en-NA,en-NF,en-NG,en-NL,en-NO,en-NR,en-NU,en-NZ,en-PG,en-PH,en-PK,en-PL,en-PN,en-PR,en-PT,en-PW,en-RO,en-RW,en-SB,en-SC,en-SD,en-SE,en-SG,en-SH,en-SI,en-SK,en-SL,en-SS,en-SX,en-SZ,en-TC,en-TK,en-TO,en-TT,en-TV,en-TZ,en-UG,en-UM,en-US,en-VC,en-VG,en-VI,en-VU,en-WS,en-ZA,en-ZM,en-ZW,hi-Latn,hi-Latn-IN",
            "zh-Hant",
            "zh-Hant-HK,zh-Hant-MO,zh-Hant-MY,zh-Hant-TW")
        .entrySet()
        .stream()
        .map(e -> Arguments.arguments(e.getKey(), e.getValue()));
  }

  private Set<ULocale> setFromString(String value) {
    return Arrays.stream(value.split(",")).map(ULocale::forLanguageTag).collect(Collectors.toSet());
  }

  @ParameterizedTest
  @MethodSource
  void getParentLocale(String childLanguageTag, String parentLanguageTag) {
    if (childLanguageTag.isBlank()) {
      assertTrue(LocalesHierarchyUtils.getParentLocale(ULocale.ROOT).isEmpty());
    }
    if (parentLanguageTag.isBlank()) {
      assertEquals(
          ULocale.ROOT,
          LocalesHierarchyUtils.getParentLocale(ULocale.forLanguageTag(childLanguageTag)).get());
    } else {
      assertEquals(
          ULocale.forLanguageTag(parentLanguageTag),
          LocalesHierarchyUtils.getParentLocale(ULocale.forLanguageTag(childLanguageTag)).get());
    }
  }

  static Stream<Arguments> getParentLocale() {
    return Stream.of(
        Arguments.of("en-150", "en-001"),
        Arguments.of("en-GB", "en-001"),
        Arguments.of("en-US", "en"),
        Arguments.of("fr", ""),
        Arguments.of("ja-JP", "ja"),
        Arguments.of("wo-Arab", ""),
        Arguments.of("zh-CN", "zh"),
        Arguments.of("zh-TW", "zh-Hant"),
        Arguments.of("zh-MO", "zh-Hant-HK"),
        Arguments.of("zh-Hans", "zh"),
        Arguments.of("zh-Hant", ""),
        Arguments.of("ht", "fr-HT"),
        Arguments.of("zh-Hant-MO", "zh-Hant-HK"));
  }

  @ParameterizedTest
  @MethodSource
  void emptyParentLocaleForUnsupportedCombinationsOfLanguageScriptCodes(String languageTag) {
    assertTrue(
        LocalesHierarchyUtils.getParentLocale(ULocale.forLanguageTag(languageTag)).isEmpty());
  }

  static Stream<Arguments> emptyParentLocaleForUnsupportedCombinationsOfLanguageScriptCodes() {
    return List.of("es-Arab", "fr-Hant", "zh-Japn").stream().map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  void isChildLocale(String childLanguageTag, String parentLanguageTag) {
    assertTrue(
        LocalesHierarchyUtils.isDescendantLocale(
            ULocale.forLanguageTag(childLanguageTag), ULocale.forLanguageTag(parentLanguageTag)));
  }

  static Stream<Arguments> isChildLocale() {
    return Map.of(
            "en-150", "en-001",
            "en-GB", "en-001",
            "en-US", "en",
            "fr", "",
            "ja-JP", "ja",
            "wo-Arab", "",
            "zh-Hans", "zh",
            "zh-Hant", "",
            "zh-Hant-MO", "zh-Hant-HK")
        .entrySet()
        .stream()
        .map(e -> Arguments.arguments(e.getKey(), e.getValue()));
  }

  @Test
  void testIsChildLocaleCornerCases() {
    assertFalse(
        LocalesHierarchyUtils.isDescendantLocale(
            ULocale.forLanguageTag("und"), ULocale.forLanguageTag("")));
    assertFalse(
        LocalesHierarchyUtils.isDescendantLocale(ULocale.forLanguageTag("und"), ULocale.ROOT));
    assertFalse(LocalesHierarchyUtils.isDescendantLocale(ULocale.ROOT, ULocale.ROOT));
  }

  @ParameterizedTest
  @MethodSource
  void allULocalesAreChidrenOfRoot(ULocale uLocale) {
    if (uLocale != ULocale.ROOT) {
      assertTrue(LocalesHierarchyUtils.isDescendantLocale(uLocale, ULocale.ROOT));
    }
  }

  static Stream<Arguments> allULocalesAreChidrenOfRoot() {
    return Arrays.stream(ULocale.getAvailableLocales()).map(Arguments::arguments);
  }

  @ParameterizedTest
  @MethodSource
  void getAncestorsLocales(String languageTag, List<String> ancestorsLanguageTags) {
    assertThat(
        LocalesHierarchyUtils.getAncestorLocales(ULocale.forLanguageTag(languageTag)),
        is(
            ancestorsLanguageTags.stream()
                .map(ULocale::forLanguageTag)
                .collect(Collectors.toList())));
  }

  static Stream<Arguments> getAncestorsLocales() {
    return Stream.of(
        Arguments.of("", List.of()),
        Arguments.of("en-150", List.of("en-001", "en", "")),
        Arguments.of("en-GB", List.of("en-001", "en", "")),
        Arguments.of("en-US", List.of("en", "")),
        Arguments.of("fr", List.of("")),
        Arguments.of("wo-Arab", List.of("")),
        Arguments.of("zh-Hans", List.of("zh", "")),
        Arguments.of("zh-Hant", List.of("")),
        Arguments.of("ht", List.of("fr-HT", "fr", "")),
        Arguments.of("zh-Hant-MO", List.of("zh-Hant-HK", "zh-Hant", "")),
        Arguments.of("zh-MO", List.of("zh-Hant-HK", "zh-Hant", "")),
        Arguments.of("sr-RS", List.of("sr", "")),
        Arguments.of("sr-Latn-RS", List.of("sr-Latn", "")));
  }

  @ParameterizedTest
  @MethodSource
  void getHighestAncestorLocale(String languageTag, String highestLanguageTag) {
    assertThat(
        LocalesHierarchyUtils.getHighestAncestorLocale(ULocale.forLanguageTag(languageTag)),
        is(ULocale.forLanguageTag(highestLanguageTag)));
  }

  static Stream<Arguments> getHighestAncestorLocale() {
    return Stream.of(
        Arguments.of("en-150", "en"),
        Arguments.of("en-GB", "en"),
        Arguments.of("en-US", "en"),
        Arguments.of("fr", "fr"),
        Arguments.of("wo-Arab", "wo-Arab"),
        Arguments.of("zh-Hans-CN", "zh"),
        Arguments.of("zh-Hant", "zh-Hant"),
        Arguments.of("ht", "fr"),
        Arguments.of("zh-Hant-MO", "zh-Hant"),
        Arguments.of("sr-Latn", "sr-Latn"),
        Arguments.of("zh-MO", "zh-Hant"),
        Arguments.of("es-Arab-MA", "es-Arab"));
  }

  @Test
  public void whenGettingHighestAncestorLocaleForRoot_fails() {
    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> LocalesHierarchyUtils.getHighestAncestorLocale(ULocale.ROOT));

    assertEquals("Param locale cannot be the ROOT.", thrown.getMessage());
  }
}
