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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class SupportedLocaleTest {
  private static final ULocale EN_US_POSIX = ULocale.forLanguageTag("en-US-POSIX");

  @Test
  void whenBuildingWithMissingRequiredProperties_buildFails() {
    IllegalStateException thrown =
        assertThrows(IllegalStateException.class, () -> SupportedLocale.builder().build());

    assertEquals(
        "Missing required properties: localeForTranslations relatedLocalesForFormatting",
        thrown.getMessage());
  }

  @Test
  void whenProvidedLocaleIsInvalid_buildFails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                SupportedLocale.builder()
                    .localeForTranslations(ULocale.ROOT)
                    .relatedLocalesForFormatting(Set.of())
                    .build());

    assertEquals("The given localeForTranslations cannot be the root.", thrown.getMessage());
  }

  @Test
  void whenLocaleIsMissingFromRelatedFormattingLocales_buildFails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                SupportedLocale.builder()
                    .localeForTranslations(ULocale.forLanguageTag("fr"))
                    .relatedLocalesForFormatting(Set.of())
                    .build());

    assertEquals(
        thrown.getMessage(),
        "The localeForTranslations fr must be present in the list for relatedLocalesForFormatting.");
  }

  @Test
  void whenRelatedFormattingLocalesContainsNonCanonicalTags_buildFails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                SupportedLocale.builder()
                    .localeForTranslations(ULocale.forLanguageTag("fr"))
                    .relatedLocalesForFormatting(Set.of(new ULocale("FR_be")))
                    .build());

    assertEquals(
        "The localeForTranslations fr must be present in the list for relatedLocalesForFormatting.",
        thrown.getMessage());
  }

  @Test
  void whenRelatedFormattingLocalesContainsNonChildrenLocales_buildFails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                SupportedLocale.builder()
                    .localeForTranslations(ULocale.forLanguageTag("fr"))
                    .relatedLocalesForFormatting(
                        Set.of("fr", "en-US").stream()
                            .map(ULocale::forLanguageTag)
                            .collect(Collectors.toSet()))
                    .build());

    assertEquals(
        "The given relatedLocaleForFormatting en-US is not the same as, or a descendant of the localeForTranslations fr.",
        thrown.getMessage());
  }

  @Test
  void whenSetsAreCompliantWithRequirements_buildSucceeds() {
    // Easy cases
    SupportedLocale.builder()
        .localeForTranslations(ULocale.forLanguageTag("fr"))
        .relatedLocalesForFormatting(
            Set.of("fr", "fr-BE", "fr-FR", "fr-CA").stream()
                .map(ULocale::forLanguageTag)
                .collect(Collectors.toSet()))
        .build();

    SupportedLocale.builder()
        .localeForTranslations(ULocale.forLanguageTag("fr-CA"))
        .relatedLocalesForFormatting(Set.of(ULocale.forLanguageTag("fr-CA")))
        .build();

    // Corner cases
    SupportedLocale.builder()
        .localeForTranslations(ULocale.forLanguageTag("en-001"))
        .relatedLocalesForFormatting(
            Set.of("en-001", "en-GB", "en-150", "en-IN").stream()
                .map(ULocale::forLanguageTag)
                .collect(Collectors.toSet()))
        .build();

    SupportedLocale.builder()
        .localeForTranslations(ULocale.forLanguageTag("es-419"))
        .relatedLocalesForFormatting(
            Set.of("es-419", "es-MX", "es-AR", "es-BZ").stream()
                .map(ULocale::forLanguageTag)
                .collect(Collectors.toSet()))
        .build();

    SupportedLocale.builder()
        .localeForTranslations(ULocale.forLanguageTag("pt-PT"))
        .relatedLocalesForFormatting(
            Set.of("pt-PT", "pt-AO", "pt-CV").stream()
                .map(ULocale::forLanguageTag)
                .collect(Collectors.toSet()))
        .build();
  }

  @Test
  void whenGeneratingFromAnUnexpectedLanguageTag_buildFails() {
    NullPointerException npe =
        assertThrows(NullPointerException.class, () -> SupportedLocale.fromLanguageTag(null));
    assertEquals("Given input cannot be null", npe.getMessage());
    IllegalStateException thrown =
        assertThrows(IllegalStateException.class, () -> SupportedLocale.fromLanguageTag(""));
    assertEquals(
        "Given parameter languageTag could not be matched with a locale available in CLDR: ",
        thrown.getMessage());
    thrown =
        assertThrows(
            IllegalStateException.class,
            () -> SupportedLocale.fromLanguageTag("Hi Joel & Kerrin!"));
    assertEquals(
        "Given parameter languageTag could not be matched with a locale available in CLDR: Hi Joel & Kerrin!",
        thrown.getMessage());

    thrown = assertThrows(IllegalStateException.class, () -> SupportedLocale.fromLanguageTag("zz"));
    assertEquals(
        "Given parameter languageTag could not be matched with a locale available in CLDR: zz",
        thrown.getMessage());
  }

  @Test
  void whenGeneratingFromAnUnexpectedLocale_buildFails() {
    NullPointerException npe =
        assertThrows(NullPointerException.class, () -> SupportedLocale.fromLocale(null));
    assertEquals("Given input cannot be null", npe.getMessage());

    IllegalStateException thrown =
        assertThrows(IllegalStateException.class, () -> SupportedLocale.fromLocale(Locale.ROOT));
    assertEquals(
        "Given parameter locale could not be matched with a locale available in CLDR: ",
        thrown.getMessage());

    thrown =
        assertThrows(
            IllegalStateException.class,
            () -> SupportedLocale.fromLocale(new Locale("Hi Joel & Kerrin!")));
    assertEquals(
        "Given parameter locale could not be matched with a locale available in CLDR: hi joel & kerrin!",
        thrown.getMessage());

    thrown =
        assertThrows(
            IllegalStateException.class, () -> SupportedLocale.fromLocale(new Locale("zz")));
    assertEquals(
        "Given parameter locale could not be matched with a locale available in CLDR: zz",
        thrown.getMessage());
  }

  @Test
  void whenGeneratingFromABadlyFormattedLocale_buildFails() {
    IllegalStateException thrown =
        assertThrows(IllegalStateException.class, () -> SupportedLocale.fromLanguageTag("FR_be"));
    assertEquals(
        "Given parameter languageTag could not be matched with a locale available in CLDR: FR_be",
        thrown.getMessage());

    thrown =
        assertThrows(IllegalStateException.class, () -> SupportedLocale.fromLanguageTag("ZH_en"));
    assertEquals(
        "Given parameter languageTag could not be matched with a locale available in CLDR: ZH_en",
        thrown.getMessage());
  }

  @Test
  void whenGeneratingFromAValidLanguageTag_buildSucceeds() {
    SupportedLocale eu = SupportedLocale.fromLanguageTag("eu");
    assertEquals(ULocale.forLanguageTag("eu"), eu.localeForTranslations());
    assertThat(eu.relatedLocalesForFormatting(), is(setOfRelatedLocalesFor("eu")));

    SupportedLocale zhHans = SupportedLocale.fromLanguageTag("zh-Hans");
    assertEquals(ULocale.forLanguageTag("zh-Hans"), zhHans.localeForTranslations());
    assertThat(zhHans.relatedLocalesForFormatting(), is(setOfRelatedLocalesFor("zh-Hans")));
  }

  @Test
  void whenGeneratingFromAnUnexpectedULocale_buildFails() {
    NullPointerException npe =
        assertThrows(NullPointerException.class, () -> SupportedLocale.fromULocale(null));
    assertEquals("Given input cannot be null", npe.getMessage());

    IllegalStateException thrown =
        assertThrows(IllegalStateException.class, () -> SupportedLocale.fromULocale(ULocale.ROOT));
    assertEquals(
        "Given parameter uLocale could not be matched with a locale available in CLDR: ",
        thrown.getMessage());

    thrown =
        assertThrows(
            IllegalStateException.class,
            () -> SupportedLocale.fromULocale(new ULocale("Hi Joel & Kerrin!")));
    assertEquals(
        "Given parameter uLocale could not be matched with a locale available in CLDR: hi joel & kerrin!",
        thrown.getMessage());

    thrown =
        assertThrows(
            IllegalStateException.class, () -> SupportedLocale.fromULocale(new ULocale("zz")));
    assertEquals(
        "Given parameter uLocale could not be matched with a locale available in CLDR: zz",
        thrown.getMessage());
  }

  @Test
  void whenGeneratingFromAValidLocale_buildSucceeds() {
    SupportedLocale enGb = SupportedLocale.fromLocale(Locale.UK);
    assertEquals(ULocale.forLanguageTag("en-GB"), enGb.localeForTranslations());
    assertThat(enGb.relatedLocalesForFormatting(), is(setOfRelatedLocalesFor("en-GB")));
  }

  @Test
  void whenGeneratingFromAnInvalidLocale_buildFails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () -> SupportedLocale.fromLocale(Locale.SIMPLIFIED_CHINESE));
    assertEquals(
        "Given parameter locale could not be matched with a locale available in CLDR: zh_CN",
        thrown.getMessage());
  }

  public Set<ULocale> setOfRelatedLocalesFor(String languageTag) {
    ULocale rootLocaleForFormatting =
        LocalesHierarchyUtils.getHighestAncestorLocale(ULocale.forLanguageTag(languageTag));
    return Stream.concat(
            Stream.of(rootLocaleForFormatting),
            LocalesHierarchyUtils.getDescendantLocales(rootLocaleForFormatting).stream())
        .filter(relatedLocale -> !LocalesHierarchyUtils.isSameLocale(relatedLocale, EN_US_POSIX))
        .collect(Collectors.toSet());
  }

  @Test
  void whenGettingRelatedLocalesForFormatting_enUSPOSIXIsRemoved() {
    SupportedLocale en = SupportedLocale.fromLanguageTag("en");
    assertFalse(en.relatedLocalesForFormatting().contains(EN_US_POSIX));
  }

  @Test
  void whenGeneratingFromABadlyFormattedULocale_buildSucceeds() {
    SupportedLocale frBE = SupportedLocale.fromULocale(new ULocale("FR_be"));
    assertEquals(ULocale.forLanguageTag("fr-BE"), frBE.localeForTranslations());
    assertThat(frBE.relatedLocalesForFormatting(), is(setOfRelatedLocalesFor("fr-BE")));
  }

  @Test
  void whenGeneratingFromABadlyFormattedInvalidULocale_buildFails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class, () -> SupportedLocale.fromULocale(new ULocale("ZH_en")));

    assertEquals(
        "Given parameter uLocale could not be matched with a locale available in CLDR: zh_EN",
        thrown.getMessage());
  }

  @Test
  void whenGeneratingFromAValidULocale_buildSucceeds() {
    SupportedLocale enGb = SupportedLocale.fromULocale(ULocale.UK);
    assertEquals(ULocale.forLanguageTag("en-GB"), enGb.localeForTranslations());
    assertThat(enGb.relatedLocalesForFormatting(), is(setOfRelatedLocalesFor("en-GB")));

    SupportedLocale zhHans = SupportedLocale.fromULocale(ULocale.SIMPLIFIED_CHINESE);
    assertEquals(ULocale.forLanguageTag("zh-Hans"), zhHans.localeForTranslations());
    assertThat(zhHans.relatedLocalesForFormatting(), is(setOfRelatedLocalesFor("zh-Hans")));
  }
}
