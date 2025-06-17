/*-
 * -\-\-
 * locales-utils
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

package com.spotify.i18n.locales.utils.language;

import static com.ibm.icu.util.ULocale.CHINESE;
import static com.ibm.icu.util.ULocale.SIMPLIFIED_CHINESE;
import static com.ibm.icu.util.ULocale.TRADITIONAL_CHINESE;
import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isDescendantLocale;
import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isRootLocale;
import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isSameLocale;
import static org.junit.jupiter.api.Assertions.*;

import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.utils.available.AvailableLocalesUtils;
import com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LanguageUtilsTest {

  @ParameterizedTest
  @MethodSource("cldrAvailableLocales")
  void canGetWrittenLanguageLocaleForCldrAvailableLocales(final ULocale locale) {
    final Optional<ULocale> writtenLanguageLocale =
        LanguageUtils.getWrittenLanguageLocale(locale.toLanguageTag());
    assertFalse(writtenLanguageLocale.isEmpty());
    assertFalse(isRootLocale(writtenLanguageLocale.get()));
    assertTrue(writtenLanguageLocale.get().getCountry().isEmpty());
    switch (writtenLanguageLocale.get().getLanguage()) {
      case "az":
      case "bs":
      case "ff":
      case "hi":
      case "kk":
      case "kok":
      case "ks":
      case "kxv":
      case "mni":
      case "pa":
      case "sat":
      case "sd":
      case "shi":
      case "sr":
      case "su":
      case "uz":
      case "vai":
      case "yue":
      case "zh":
        assertFalse(writtenLanguageLocale.get().getScript().isEmpty());
        break;
      default:
        assertTrue(writtenLanguageLocale.get().getScript().isEmpty());
        break;
    }
  }

  @ParameterizedTest
  @MethodSource("cldrAvailableLocales")
  void canGetSpokenLanguageLocaleForCldrAvailableLocales(final ULocale locale) {
    final Optional<ULocale> spokenLanguageLocale =
        LanguageUtils.getSpokenLanguageLocale(locale.toLanguageTag());
    assertFalse(spokenLanguageLocale.isEmpty());
    assertFalse(isRootLocale(spokenLanguageLocale.get()));
    assertTrue(spokenLanguageLocale.get().getCountry().isEmpty());

    if (isSameLocale(locale, CHINESE) || isDescendantLocale(locale, CHINESE)) {
      isSameLocale(spokenLanguageLocale.get(), SIMPLIFIED_CHINESE);
    } else if (isSameLocale(locale, TRADITIONAL_CHINESE)
        || isDescendantLocale(locale, TRADITIONAL_CHINESE)) {
      isSameLocale(spokenLanguageLocale.get(), TRADITIONAL_CHINESE);
    } else {
      assertTrue(spokenLanguageLocale.get().getScript().isEmpty());
    }
  }

  public static Stream<Arguments> cldrAvailableLocales() {
    return Arrays.stream(ULocale.getAvailableLocales())
        .filter(Predicate.not(LocalesHierarchyUtils::isRootLocale))
        .map(Arguments::of);
  }

  @Test
  public void confirmLogicAccountsForAllHighestAncestorLocalesWithScript() {
    // Set of highest ancestor locales for which the fallback locale (language code only) identifies
    // the same spoken language. Assessed and confirmed as valid as of CLDR 47.
    final Set<String> fallbackIsSpokenLanguage =
        Set.of(
            "az-Cyrl", // https://www.omniglot.com/writing/azeri.htm
            "bs-Cyrl", // https://www.omniglot.com/writing/bosnian.htm
            "ff-Adlm", // https://www.omniglot.com/writing/fula.htm
            "kok-Latn", // https://www.omniglot.com/writing/konkani.htm
            "ks-Deva", // https://www.omniglot.com/writing/kashmiri.htm
            "kxv-Deva", // https://www.businesswireindia.com/two-of-indias-endangered-languages-kuvi-and-kangri-get-a-lease-of-revival-as-motorola-and-lenovo-foundation-bring-alive-their-indigenous-languages-digital-inclusion-initiative-83131.html
            "kxv-Orya", // https://www.businesswireindia.com/two-of-indias-endangered-languages-kuvi-and-kangri-get-a-lease-of-revival-as-motorola-and-lenovo-foundation-bring-alive-their-indigenous-languages-digital-inclusion-initiative-83131.html
            "kxv-Telu", // https://www.businesswireindia.com/two-of-indias-endangered-languages-kuvi-and-kangri-get-a-lease-of-revival-as-motorola-and-lenovo-foundation-bring-alive-their-indigenous-languages-digital-inclusion-initiative-83131.html
            "pa-Arab", // https://www.omniglot.com/writing/punjabi.htm
            "sd-Deva", // https://www.omniglot.com/writing/sindhi.htm
            "shi-Latn", // https://www.omniglot.com/writing/shilha.htm
            "sr-Latn", // https://www.omniglot.com/writing/serbian.htm
            "uz-Arab", // https://www.omniglot.com/writing/uzbek.htm
            "uz-Cyrl", // https://www.omniglot.com/writing/uzbek.htm
            "vai-Latn", // https://www.omniglot.com/writing/vai.htm
            "yue-Hans" // https://www.omniglot.com/chinese/cantonese.htm
            );

    final Set<String> fallbackIsNotSpokenLanguage = Set.of("zh-Hant");

    assertEquals(
        // After filtering out all locales we have assessed, there shouldn't remain any entry here
        0,
        AvailableLocalesUtils.getCldrLocales().stream()
            .map(LocalesHierarchyUtils::getHighestAncestorLocale)
            // We only retain highest ancestor locales which have a script defined.
            .filter(locale -> !locale.getScript().isEmpty())
            // We discard all locales that we have assessed.
            .filter(locale -> !fallbackIsSpokenLanguage.contains(locale.toLanguageTag()))
            .filter(locale -> !fallbackIsNotSpokenLanguage.contains(locale.toLanguageTag()))
            .count());
  }

  @Test
  public void validateBosnianCroatian() {
    ULocale bosnian = ULocale.forLanguageTag("bs");
    ULocale bosnianLatin = ULocale.forLanguageTag("bs-Latn");
    ULocale bosnianCyrillic = ULocale.forLanguageTag("bs-Cyrl");
    ULocale croatian = ULocale.forLanguageTag("hr");

    // Bosnian written
    Set.of("bs", "bs-Latn", "bs-Latn-BA")
        .forEach(
            languageTag ->
                assertEquals(
                    bosnianLatin,
                    LanguageUtils.getWrittenLanguageLocale(languageTag).get(),
                    String.format(
                        "Spoken language for language tag %s should be %s",
                        languageTag, bosnianLatin.toLanguageTag())));
    Set.of("bs-Cyrl", "bs-Cyrl-BA")
        .forEach(
            languageTag ->
                assertEquals(
                    bosnianCyrillic,
                    LanguageUtils.getWrittenLanguageLocale(languageTag).get(),
                    String.format(
                        "Spoken language for language tag %s should be %s",
                        languageTag, bosnianCyrillic.toLanguageTag())));

    // Bosnian spoken
    Set.of("bs", "bs-Cyrl", "bs-Cyrl-BA", "bs-Latn", "bs-Latn-BA")
        .forEach(
            languageTag ->
                assertEquals(
                    bosnian,
                    LanguageUtils.getSpokenLanguageLocale(languageTag).get(),
                    String.format(
                        "Spoken language for language tag %s should be %s",
                        languageTag, bosnian.toLanguageTag())));

    // Croatian written
    Set.of("hr", "hr-BA", "hr-HR")
        .forEach(
            languageTag ->
                assertEquals(
                    croatian,
                    LanguageUtils.getWrittenLanguageLocale(languageTag).get(),
                    String.format(
                        "Written language for language tag %s should be %s",
                        languageTag, croatian.toLanguageTag())));

    // Croatian spoken
    Set.of("hr", "hr-BA", "hr-HR")
        .forEach(
            languageTag ->
                assertEquals(
                    croatian,
                    LanguageUtils.getSpokenLanguageLocale(languageTag).get(),
                    String.format(
                        "Spoken language for language tag %s should be %s",
                        languageTag, croatian.toLanguageTag())));
  }
}
