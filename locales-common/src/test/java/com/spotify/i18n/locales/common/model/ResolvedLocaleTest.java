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

package com.spotify.i18n.locales.common.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ibm.icu.util.ULocale;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ResolvedLocaleTest {

  @Test
  void whenBuildingWithMissingRequiredProperties_buildFails() {
    IllegalStateException thrown =
        assertThrows(IllegalStateException.class, () -> ResolvedLocale.builder().build());

    assertEquals(
        "Missing required properties: localeForTranslations localeForFormatting",
        thrown.getMessage());
  }

  @Test
  void whenProvidedLocaleForTranslationsIsInvalid_buildFails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                ResolvedLocale.builder()
                    .localeForTranslations(ULocale.ROOT)
                    .localeForTranslationsFallbacks(Collections.emptyList())
                    .localeForFormatting(ULocale.ROOT)
                    .build());

    assertEquals("The given localeForTranslations cannot be the root.", thrown.getMessage());
  }

  @Test
  void whenProvidedLocaleForFormattingIsInvalid_buildFails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                ResolvedLocale.builder()
                    .localeForTranslations(ULocale.ENGLISH)
                    .localeForTranslationsFallbacks(Collections.emptyList())
                    .localeForFormatting(ULocale.ROOT)
                    .build());

    assertEquals("The given localeForFormatting cannot be the root.", thrown.getMessage());
  }

  @Test
  void whenProvidedLocaleForTranslationsFallbacksIsInvalid_buildFails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                ResolvedLocale.builder()
                    .localeForTranslations(ULocale.ENGLISH)
                    .localeForTranslationsFallbacks(List.of(ULocale.ROOT))
                    .localeForFormatting(ULocale.ENGLISH)
                    .build());

    assertEquals(
        "The given fallbackLocalesForTranslations cannot contain the root.", thrown.getMessage());
  }

  @Test
  void whenLocaleForTranslationsFallbacksContainsLocaleForTranslations_buildFails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                ResolvedLocale.builder()
                    .localeForTranslations(ULocale.forLanguageTag("fr-CA"))
                    .localeForTranslationsFallbacks(
                        List.of(ULocale.forLanguageTag("fr-CA"), ULocale.forLanguageTag("fr")))
                    .localeForFormatting(ULocale.forLanguageTag("fr-CA"))
                    .build());

    assertEquals(
        "The given fallbackLocalesForTranslations cannot contain the localeForTranslations [fr-CA].",
        thrown.getMessage());
  }

  @Test
  void whenLocaleForTranslationsFallbacksAreNotRelatedToTranslationLocale_buildFails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                ResolvedLocale.builder()
                    .localeForTranslations(ULocale.forLanguageTag("fr"))
                    .localeForTranslationsFallbacks(
                        List.of(ULocale.forLanguageTag("ja"), ULocale.forLanguageTag("it")))
                    .localeForFormatting(ULocale.forLanguageTag("fr-BE"))
                    .build());

    assertEquals(
        "The given fallbackLocaleForTranslations [it,ja] are not compatible fallbacks for the localeForTranslations [fr].",
        thrown.getMessage());

    thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                ResolvedLocale.builder()
                    .localeForTranslations(ULocale.forLanguageTag("zh-Hant"))
                    .localeForTranslationsFallbacks(List.of(ULocale.forLanguageTag("zh")))
                    .localeForFormatting(ULocale.forLanguageTag("zh-Hant-HK"))
                    .build());

    assertEquals(
        "The given fallbackLocaleForTranslations [zh] are not compatible fallbacks for the localeForTranslations [zh-Hant].",
        thrown.getMessage());

    thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                ResolvedLocale.builder()
                    .localeForTranslations(ULocale.forLanguageTag("zh"))
                    .localeForTranslationsFallbacks(List.of(ULocale.forLanguageTag("zh-Hant")))
                    .localeForFormatting(ULocale.forLanguageTag("zh-Hant-HK"))
                    .build());

    assertEquals(
        "The given fallbackLocaleForTranslations [zh-Hant] are not compatible fallbacks for the localeForTranslations [zh].",
        thrown.getMessage());

    thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                ResolvedLocale.builder()
                    .localeForTranslations(ULocale.forLanguageTag("sr-Latn"))
                    .localeForTranslationsFallbacks(List.of(ULocale.forLanguageTag("sr")))
                    .localeForFormatting(ULocale.forLanguageTag("sr-Latn-RS"))
                    .build());

    assertEquals(
        "The given fallbackLocaleForTranslations [sr] are not compatible fallbacks for the localeForTranslations [sr-Latn].",
        thrown.getMessage());
  }

  @Test
  void whenFormattingLocaleIsNotRelatedToTranslationLocale_buildFails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                ResolvedLocale.builder()
                    .localeForTranslations(ULocale.forLanguageTag("fr"))
                    .localeForTranslationsFallbacks(Collections.emptyList())
                    .localeForFormatting(ULocale.forLanguageTag("en-US"))
                    .build());

    assertEquals(
        "The given localeForFormatting en-US is not the same as, or a descendant of the localeForTranslations fr.",
        thrown.getMessage());
  }

  @Test
  void whenSetsAreCompliantWithRequirements_buildSucceeds() {
    // Easy cases
    assertEquals(
        ResolvedLocale.builder()
            .localeForTranslations(ULocale.forLanguageTag("fr"))
            .localeForFormatting(ULocale.forLanguageTag("fr-BE"))
            .build()
            .localeForTranslationsFallbacks(),
        Collections.emptyList());

    ResolvedLocale.builder()
        .localeForTranslations(ULocale.forLanguageTag("fr"))
        .localeForTranslationsFallbacks(Collections.emptyList())
        .localeForFormatting(ULocale.forLanguageTag("fr-BE"))
        .build();

    // Corner cases
    ResolvedLocale.builder()
        .localeForTranslations(ULocale.forLanguageTag("en-001"))
        .localeForTranslationsFallbacks(List.of(ULocale.forLanguageTag("en")))
        .localeForFormatting(ULocale.forLanguageTag("en-IN"))
        .build();

    ResolvedLocale.builder()
        .localeForTranslations(ULocale.forLanguageTag("es-419"))
        .localeForTranslationsFallbacks(List.of(ULocale.forLanguageTag("es")))
        .localeForFormatting(ULocale.forLanguageTag("es-AR"))
        .build();

    ResolvedLocale.builder()
        .localeForTranslations(ULocale.forLanguageTag("pt-PT"))
        .localeForTranslationsFallbacks(List.of(ULocale.forLanguageTag("pt")))
        .localeForFormatting(ULocale.forLanguageTag("pt-AO"))
        .build();

    ResolvedLocale.builder()
        .localeForTranslations(ULocale.forLanguageTag("sr-Cyrl"))
        .localeForTranslationsFallbacks(List.of(ULocale.forLanguageTag("sr")))
        .localeForFormatting(ULocale.forLanguageTag("sr-Cyrl-RS"))
        .build();
  }

  @Test
  void whenGeneratingFromAnUnexpectedLanguageTag_buildFails() {
    NullPointerException npe =
        assertThrows(NullPointerException.class, () -> ResolvedLocale.fromLanguageTags(null, null));
    assertEquals("Given input cannot be null", npe.getMessage());
    IllegalStateException thrown =
        assertThrows(IllegalStateException.class, () -> ResolvedLocale.fromLanguageTags("", ""));
    assertEquals(
        "Given parameter languageTagForTranslations could not be matched with a locale available in CLDR: ",
        thrown.getMessage());

    thrown =
        assertThrows(IllegalStateException.class, () -> ResolvedLocale.fromLanguageTags("fr", ""));
    assertEquals(
        "Given parameter languageTagForFormatting could not be matched with a locale available in CLDR: ",
        thrown.getMessage());

    thrown =
        assertThrows(
            IllegalStateException.class,
            () -> ResolvedLocale.fromLanguageTags("Hi Joel!", "Hi Kerrin!"));
    assertEquals(
        "Given parameter languageTagForTranslations could not be matched with a locale available in CLDR: Hi Joel!",
        thrown.getMessage());

    thrown =
        assertThrows(
            IllegalStateException.class,
            () -> ResolvedLocale.fromLanguageTags("zh-Hant", "Hi Kerrin!"));
    assertEquals(
        "Given parameter languageTagForFormatting could not be matched with a locale available in CLDR: Hi Kerrin!",
        thrown.getMessage());

    thrown =
        assertThrows(
            IllegalStateException.class, () -> ResolvedLocale.fromLanguageTags("zz", "zz"));
    assertEquals(
        "Given parameter languageTagForTranslations could not be matched with a locale available in CLDR: zz",
        thrown.getMessage());

    thrown =
        assertThrows(
            IllegalStateException.class, () -> ResolvedLocale.fromLanguageTags("fr", "zz"));
    assertEquals(
        "Given parameter languageTagForFormatting could not be matched with a locale available in CLDR: zz",
        thrown.getMessage());
  }

  @Test
  void whenGeneratingFromABadlyFormattedLocale_buildFails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class, () -> ResolvedLocale.fromLanguageTags("FR_be", "FR_BE"));
    assertEquals(
        "Given parameter languageTagForTranslations could not be matched with a locale available in CLDR: FR_be",
        thrown.getMessage());

    thrown =
        assertThrows(
            IllegalStateException.class, () -> ResolvedLocale.fromLanguageTags("fr", "zh-EN"));
    assertEquals(
        "Given parameter languageTagForFormatting could not be matched with a locale available in CLDR: zh-EN",
        thrown.getMessage());
  }
}
