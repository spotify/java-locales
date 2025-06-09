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

package com.spotify.i18n.locales.utils.available;

import static com.ibm.icu.util.ULocale.CHINESE;
import static com.ibm.icu.util.ULocale.SIMPLIFIED_CHINESE;
import static com.ibm.icu.util.ULocale.TRADITIONAL_CHINESE;
import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isLanguageWrittenInSeveralScripts;
import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isRootLocale;
import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isSameLocale;

import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils;
import com.spotify.i18n.locales.utils.language.LanguageUtils;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AvailableLocalesUtils {

  // Outlier locale we don't want to encounter
  private static final ULocale EN_US_POSIX = ULocale.forLanguageTag("en-US-POSIX");

  // Set containing all CLDR available locales, except the ROOT and en-US-POSIX
  private static final Set<ULocale> CLDR_LOCALES =
      Arrays.stream(ULocale.getAvailableLocales())
          .filter(l -> !isRootLocale(l) && !isSameLocale(l, EN_US_POSIX))
          .collect(Collectors.toSet());

  // Set containing all written language locales. Languages which can only be written in a single
  // script are identified using the language code only. The ones that can be written in several
  // scripts are identified using both the language and script codes.
  private static final Set<ULocale> WRITTEN_LANGUAGE_LOCALES =
      CLDR_LOCALES.stream()
          .filter(LocalesHierarchyUtils::isHighestAncestorLocale)
          .map(
              locale ->
                  Optional.of(locale)
                      .filter(l -> l.getScript().isEmpty())
                      .filter(l -> isLanguageWrittenInSeveralScripts(l.getLanguage()))
                      .map(ULocale::addLikelySubtags)
                      .map(
                          l ->
                              new ULocale.Builder()
                                  .setLanguage(l.getLanguage())
                                  .setScript(l.getScript())
                                  .build())
                      .orElse(locale))
          .collect(Collectors.toSet());

  // Set containing all spoken language locales.
  private static final Set<ULocale> SPOKEN_LANGUAGE_LOCALES =
      Stream.concat(
              // Locales consisting only of a language code, except the ones for Chinese.
              CLDR_LOCALES.stream()
                  .filter(locale -> locale.getScript().isEmpty() && locale.getCountry().isEmpty())
                  .filter(locale -> !CHINESE.getLanguage().equals(locale.getLanguage())),
              // We explicitly add Simplified and Traditional Chinese as zh-Hans and zh-Hant.
              Stream.of(SIMPLIFIED_CHINESE, TRADITIONAL_CHINESE))
          .collect(Collectors.toSet());

  /**
   * Set containing all reference locales, which are all CLDR available locales except the ROOT and
   * en-US-POSIX, minimized.
   */
  private static final Set<ULocale> REFERENCE_LOCALES =
      CLDR_LOCALES.stream().map(ULocale::minimizeSubtags).collect(Collectors.toUnmodifiableSet());

  /**
   * Returns a set containing all available CLDR {@link ULocale}, without the ROOT and en-US-POSIX.
   *
   * @see <a href="https://cldr.unicode.org/">Unicode CLDR Project</a>
   */
  public static Set<ULocale> getCldrLocales() {
    return CLDR_LOCALES;
  }

  /**
   * Returns a set containing all reference locales, which is a reduced set of the one returned by
   * {@link #getCldrLocales()}, where alias entries were removed.
   *
   * <p>This set therefore offers the guarantee that it contains a single unique corresponding
   * locale, for each unique identifiable language.
   *
   * <p>Reduction example:
   *
   * <ul>
   *   <li>CLDR locales: zh, zh-Hans, zh-Hans-CN
   *   <li>Reduced to: zh
   * </ul>
   */
  public static Set<ULocale> getReferenceLocales() {
    return REFERENCE_LOCALES;
  }

  /**
   * Returns a set containing all unambiguous written language locales available in CLDR. They are
   * unambiguous in the sense that:
   *
   * <ul>
   *   <li>Languages which can be written in several scripts will always be identified by both a
   *       language code and a script code.
   *   <li>Languages that can be written in only one script will be identified only by a language
   *       code.
   * </ul>
   *
   * This set of locales is safe to use for locale resolution. We however recommend making use of
   * the {@link LanguageUtils#getWrittenLanguageLocale(String)} helper method to retrieve the
   * written language associated with any given language tag.
   *
   * @see <a href="https://cldr.unicode.org/">Unicode CLDR Project</a>
   */
  public static Set<ULocale> getWrittenLanguageLocales() {
    return WRITTEN_LANGUAGE_LOCALES;
  }

  /**
   * Returns a set containing all unambiguous spoken language locales available in CLDR. They are
   * unambiguous in the sense that:
   *
   * <ul>
   *   <li>Languages for which the script is a differentiator will always be identified by both a
   *       language code and a script code.
   *   <li>Languages that can only be written in a single script, or that remain the same whether
   *       written in a given script or another, will be identified only by a language code.
   * </ul>
   *
   * This set of locales is not safe to use for locale resolution. Please make use of the {@link
   * LanguageUtils#getSpokenLanguageLocale(String)} helper method to retrieve the spoken language
   * associated with any given language tag.
   *
   * @see <a href="https://cldr.unicode.org/">Unicode CLDR Project</a>
   */
  public static Set<ULocale> getSpokenLanguageLocales() {
    return SPOKEN_LANGUAGE_LOCALES;
  }
}
