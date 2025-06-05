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

import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isDescendantLocale;
import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isRootLocale;
import static org.junit.jupiter.api.Assertions.*;

import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LanguageUtilsTest {

  @ParameterizedTest
  @MethodSource("cldrAvailableLocales")
  void canGetWrittenLanguageLocaleForCldrAvailableLocales(final ULocale locale) {
    final ULocale writtenLanguageLocale = LanguageUtils.getWrittenLanguageLocale(locale);
    assertNotNull(writtenLanguageLocale);
    assertFalse(isRootLocale(writtenLanguageLocale));
    assertTrue(writtenLanguageLocale.getCountry().isEmpty());
    switch (writtenLanguageLocale.getLanguage()) {
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
        assertFalse(writtenLanguageLocale.getScript().isEmpty());
        break;
      default:
        assertTrue(writtenLanguageLocale.getScript().isEmpty());
        break;
    }
  }

  static final ULocale CHINESE_TRADITIONAL = ULocale.forLanguageTag("zh-Hant");

  @ParameterizedTest
  @MethodSource("cldrAvailableLocales")
  void canGetSpokenLanguageLocaleForCldrAvailableLocales(final ULocale locale) {
    final ULocale spokenLanguageLocale = LanguageUtils.getSpokenLanguageLocale(locale);
    assertNotNull(spokenLanguageLocale);
    assertFalse(isRootLocale(spokenLanguageLocale));
    assertTrue(spokenLanguageLocale.getCountry().isEmpty());
    if (!isSameLocale(locale, CHINESE_TRADITIONAL)
        && !isDescendantLocale(locale, CHINESE_TRADITIONAL)) {
      assertTrue(spokenLanguageLocale.getScript().isEmpty());
    }
  }

  public static Stream<Arguments> cldrAvailableLocales() {
    return Arrays.stream(ULocale.getAvailableLocales())
        .filter(Predicate.not(LocalesHierarchyUtils::isRootLocale))
        .map(Arguments::of);
  }
}
