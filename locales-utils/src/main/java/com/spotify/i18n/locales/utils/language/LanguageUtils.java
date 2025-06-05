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

import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.getHighestAncestorLocale;
import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isRootLocale;
import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isSameLocale;

import com.google.common.base.Preconditions;
import com.ibm.icu.util.ULocale;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Utility class that provides helpers around the concepts of written and spoken languages. It
 * works exclusively based on {@link ULocale}s and assumes that all given parameters are non-null
 * and locales available in CLDR.
 *
 * @see ULocale
 * @author Eric Fjøsne
 */
public class LanguageUtils {

  /** Chinese (Traditional) */
  private static final ULocale CHINESE_TRADITIONAL = ULocale.forLanguageTag("zh-Hant");

  /**
   * Map containing locales identifying languages for which multiple scripts are supported in CLDR,
   * with their most likely equivalent in terms of language+script (no region code).
   */
  static final Map<ULocale, ULocale> LANGUAGE_TO_LIKELY_LANGUAGE_SCRIPT_MAP =
      Arrays.stream(ULocale.getAvailableLocales())
          .filter(locale -> hasScript(locale))
          .map(ULocale::getLanguage)
          .distinct()
          .collect(
              Collectors.toMap(
                  languageCode -> ULocale.forLanguageTag(languageCode),
                  languageCode ->
                      new ULocale.Builder()
                          .setLocale(ULocale.addLikelySubtags(ULocale.forLanguageTag(languageCode)))
                          .setRegion("")
                          .build()));

  /**
   * Set of locales for which the fallback locale (language code only) identifies the same spoken
   * language.
   */
  private static final Set<ULocale> FALLBACK_LOCALE_IS_SPOKEN_LANGUAGE_LOCALES =
      Stream.of(
              "az-Cyrl", // https://www.omniglot.com/writing/azeri.htm
              "bs-Cyrl", // https://www.omniglot.com/writing/bosnian.htm
              "ff-Adlm", // https://www.omniglot.com/writing/fula.htm
              "kok-Latn", // https://www.omniglot.com/writing/konkani.htm
              "ks-Deva", // https://www.omniglot.com/writing/kashmiri.htm
              "kxv-Deva", //
              "kxv-Orya", //
              "kxv-Telu", //
              "pa-Arab", // https://www.omniglot.com/writing/punjabi.htm
              "sd-Deva", // https://www.omniglot.com/writing/sindhi.htm
              "shi-Latn", // https://www.omniglot.com/writing/shilha.htm
              "sr-Latn", // https://www.omniglot.com/writing/serbian.htm
              "uz-Arab", // https://www.omniglot.com/writing/uzbek.htm
              "uz-Cyrl", // https://www.omniglot.com/writing/uzbek.htm
              "vai-Latn", // https://www.omniglot.com/writing/vai.htm
              "yue-Hans" // https://www.omniglot.com/chinese/cantonese.htm
              )
          .map(ULocale::forLanguageTag)
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
      return LANGUAGE_TO_LIKELY_LANGUAGE_SCRIPT_MAP.getOrDefault(
          highestAncestorLocale, highestAncestorLocale);
    }
  }

  /**
   * Returns the {@link ULocale} identifying the spoken language associated with the given locale.
   * The returned locale will consist of a language code only, except for Chinese (Traditional)
   * which will be identified as zh-Hant.
   *
   * @param locale the locale
   * @return locale identifying the spoken language
   * @throws IllegalArgumentException when given locale is the ROOT
   */
  public static ULocale getSpokenLanguageLocale(final ULocale locale) {
    Preconditions.checkNotNull(locale);
    Preconditions.checkArgument(!isRootLocale(locale), "Param locale cannot be the ROOT.");
    final ULocale highestAncestorLocale = getHighestAncestorLocale(locale);
    if (!hasScript(highestAncestorLocale)
        || isSameLocale(highestAncestorLocale, CHINESE_TRADITIONAL)) {
      // When the highest ancestor locale has no script, or is Chinese (Traditional), it
      // appropriately identifies the spoken language.
      return highestAncestorLocale;
    } else if (FALLBACK_LOCALE_IS_SPOKEN_LANGUAGE_LOCALES.contains(highestAncestorLocale)) {
      // When we encounter a highest ancestor locale for which we know the fallback locale is the
      // spoken language one, we return the fallback.
      return highestAncestorLocale.getFallback();
    } else {
      // This exception is mostly thrown as a safety measure, to ensure that we detect a new highest
      // ancestor locale which this code doesn't support yet when we update icu4j. This will cause
      // the unit tests to fail.
      throw new IllegalArgumentException(
          String.format(
              "Unexpected language tag encountered: %s", highestAncestorLocale.toLanguageTag()));
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
