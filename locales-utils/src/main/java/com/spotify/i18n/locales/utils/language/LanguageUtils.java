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

import com.ibm.icu.util.LocaleMatcher;
import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.utils.available.AvailableLocalesUtils;
import com.spotify.i18n.locales.utils.languagetag.LanguageTagUtils;
import java.util.Optional;

/**
 * A Utility class that provides helpers around the concepts of written and spoken languages. It
 * works exclusively based on {@link ULocale}s and assumes that all given parameters are non-null
 * and locales available in CLDR.
 *
 * @see ULocale
 * @author Eric Fjøsne
 */
public class LanguageUtils {

  // Locale matcher for all written language locales
  private static final LocaleMatcher WRITTEN_LANGUAGE_LOCALE_MATCHER =
      LocaleMatcher.builder()
          .setSupportedULocales(AvailableLocalesUtils.getWrittenLanguageLocales())
          .setNoDefaultLocale()
          .build();

  /**
   * Returns the optional {@link ULocale} identifying the written language associated with the given
   * language tag. The returned locale will consist of a language code at the minimum, but will
   * contain a script code for languages that can be written in several scripts, to alleviate any
   * possible ambiguity.
   *
   * @param languageTag the languageTag
   * @return the optional locale identifying the written language
   */
  public static Optional<ULocale> getWrittenLanguageLocale(final String languageTag) {
    return LanguageTagUtils.parse(languageTag).map(WRITTEN_LANGUAGE_LOCALE_MATCHER::getBestMatch);
  }

  /**
   * Returns the optional {@link ULocale} identifying the spoken language associated with the given
   * language tag. The returned locale will consist of a language code at the minimum, but will
   * contain a script code for languages for which the script is a language differentiator, to
   * alleviate any possible ambiguity. (ex: zh-Hans for Simplified Chinese, and zh-Hant for
   * Traditional Chinese).
   *
   * @param languageTag the languageTag
   * @return the optional locale identifying the written language
   */
  public static Optional<ULocale> getSpokenLanguageLocale(final String languageTag) {
    return LanguageTagUtils.parse(languageTag)
        .map(WRITTEN_LANGUAGE_LOCALE_MATCHER::getBestMatch)
        .map(LanguageUtils::getCorrespondingSpokenLanguageLocale);
  }

  /**
   * Returns the given written language locale stripped of its script code (if present), except for
   * the Chinese language, for which we need to retain it.
   *
   * @param locale written language locale
   * @return actual spoken language locale
   */
  private static ULocale getCorrespondingSpokenLanguageLocale(final ULocale locale) {
    if (locale.getScript().isEmpty() || CHINESE.getLanguage().equals(locale.getLanguage())) {
      return locale;
    } else {
      return locale.getFallback();
    }
  }
}
