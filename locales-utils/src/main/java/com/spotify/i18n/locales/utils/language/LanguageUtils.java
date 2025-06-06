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
import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.getHighestAncestorLocale;
import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isRootLocale;
import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isSameLocale;

import com.google.common.base.Preconditions;
import com.ibm.icu.util.ULocale;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Utility class that provides helpers around the concepts of written and spoken languages. It
 * works exclusively based on {@link ULocale}s and assumes that all given parameters are non-null
 * and locales available in CLDR.
 *
 * @see ULocale
 * @author Eric Fjøsne
 */
public class LanguageUtils {

  /**
   * Map containing locales identifying languages for which multiple scripts are supported in CLDR,
   * with their most likely equivalent in terms of language+script (no region code). We consider
   * them as ambiguous, as the language code only doesn't provide enough information to fully
   * identify the written language without looking into CLDR data.
   */
  private static final Map<ULocale, ULocale> AMBIGUOUS_LANGUAGE_TO_LIKELY_WRITTEN_LANGUAGE_MAP =
      Arrays.stream(ULocale.getAvailableLocales())
          .filter(locale -> hasScript(locale))
          .map(ULocale::getLanguage)
          .distinct()
          .collect(
              Collectors.toMap(
                  languageCode -> new ULocale.Builder().setLanguage(languageCode).build(),
                  languageCode ->
                      new ULocale.Builder()
                          .setLocale(ULocale.addLikelySubtags(ULocale.forLanguageTag(languageCode)))
                          .setRegion("")
                          .build()));

  /**
   * Set containing the codes for languages which can only be written in a single script, and are
   * therefore considered as unambiguous.
   */
  private static final Set<String> UNAMBIGUOUS_WRITTEN_LANGUAGE_CODES =
      Arrays.stream(ULocale.getAvailableLocales())
          .filter(locale -> locale.getScript().isEmpty() && locale.getCountry().isEmpty())
          .filter(locale -> !AMBIGUOUS_LANGUAGE_TO_LIKELY_WRITTEN_LANGUAGE_MAP.containsKey(locale))
          .map(ULocale::getLanguage)
          .collect(Collectors.toSet());

  /**
   * Returns the {@link ULocale} identifying the written language associated with the given locale.
   * The returned locale will consist of a language code at the minimum, but will also contain a
   * script code for languages that can be written in several scripts.
   *
   * @param locale the locale
   * @return locale identifying the written language
   * @throws IllegalArgumentException when given locale is the ROOT
   */
  public static ULocale getWrittenLanguageLocale(final ULocale locale) {
    Preconditions.checkNotNull(locale);
    Preconditions.checkArgument(!isRootLocale(locale), "Param locale cannot be the ROOT.");

    if (UNAMBIGUOUS_WRITTEN_LANGUAGE_CODES.contains(locale.getLanguage())) {
      // The language code is enough to identify the written language fully.
      return new ULocale.Builder().setLanguage(locale.getLanguage()).build();
    } else {
      final ULocale highestAncestorLocale = getHighestAncestorLocale(locale);
      if (hasScript(highestAncestorLocale)) {
        // When the highest ancestor locale already has a script, we know it is the best possible
        // identifier for the corresponding written language.
        return highestAncestorLocale;
      } else {
        // When the highest ancestor locale doesn't have a script code defined, we add one for
        // languages for which we need the differentiation to be explicit. Ex: for Serbian, we will
        // return sr-Cyrl and not sr, as the language can also be written in sr-Latn. For French, we
        // will return fr as the language is never written in another script.
        return AMBIGUOUS_LANGUAGE_TO_LIKELY_WRITTEN_LANGUAGE_MAP.getOrDefault(
            highestAncestorLocale, highestAncestorLocale);
      }
    }
  }

  /**
   * Returns the {@link ULocale} identifying the spoken language associated with the given locale.
   * The returned locale will consist of a language code only, except for Chinese (Simplified),
   * which will be identified as zh-Hans, and Chinese (Traditional) which will be identified as
   * zh-Hant.
   *
   * @param locale the locale
   * @return locale identifying the spoken language
   * @throws IllegalArgumentException when given locale is the ROOT
   */
  public static ULocale getSpokenLanguageLocale(final ULocale locale) {
    Preconditions.checkNotNull(locale);
    Preconditions.checkArgument(!isRootLocale(locale), "Param locale cannot be the ROOT.");

    if (CHINESE.getLanguage().equals(locale.getLanguage())) {
      // Chinese is the only language for which the script is a language differentiator.
      if (isSameLocale(getHighestAncestorLocale(locale), TRADITIONAL_CHINESE)) {
        return TRADITIONAL_CHINESE;
      } else {
        return SIMPLIFIED_CHINESE;
      }
    } else {
      // All other spoken languages only require the language code to be defined.
      return new ULocale.Builder().setLanguage(locale.getLanguage()).build();
    }
  }

  /**
   * Returns true if the given locale has a script code defined.
   *
   * @param locale the locale under test
   * @return true if the given locale has a script code defined
   */
  private static boolean hasScript(final ULocale locale) {
    return !locale.getScript().isEmpty();
  }
}
