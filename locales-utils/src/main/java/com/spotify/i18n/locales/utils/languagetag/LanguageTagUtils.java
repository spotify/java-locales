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

package com.spotify.i18n.locales.utils.languagetag;

import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils;
import java.util.Optional;

/**
 * A Utility class that provides helpers to perform language tag related operations. It works
 * exclusively based on {@link ULocale}s and assumes any sort of input (including null values).
 *
 * <p>The values returned by these helpers should never be trusted as matching supported locale(s)
 * and/or language(s) for your use case. If you are looking for the best matching supported language
 * or locale matching a given Accept-Language value, you need to use the locale resolution helpers.
 *
 * @see <a href="https://en.wikipedia.org/wiki/IETF_language_tag">IETF BCP 47 language tag</a>
 * @see ULocale
 * @author Eric Fjøsne
 */
public class LanguageTagUtils {

  private static final ULocale UNDEFINED_LOCALE = ULocale.forLanguageTag("und");

  /**
   * Returns the normalized BCP47 language tag value, for a given languageTag value. By normalized,
   * we mean a value that has been sanitized or fixed from any invalid input, and formatted
   * according to BCP47.
   *
   * <p>If provided value is empty, null or invalid, returns "und" (language tag for Undefined)
   *
   * @see <a href="https://en.wikipedia.org/wiki/IETF_language_tag">IETF BCP 47 language tag</a>
   * @param languageTag
   * @return Normalized language tag
   */
  public static String normalize(final String languageTag) {
    return parse(languageTag).map(ULocale::toLanguageTag).orElse(UNDEFINED_LOCALE.toLanguageTag());
  }

  /**
   * Returns the {@link Optional} {@link ULocale} resulting from parsing a given language tag.
   *
   * <p>If provided value is empty, null or invalid, returns an empty {@link Optional}.
   *
   * @see ULocale
   * @param languageTag
   * @return Optional best matching {@link ULocale}
   */
  public static Optional<ULocale> parse(final String languageTag) {
    return Optional.ofNullable(languageTag)
        .map(LanguageTagUtils::sanitizeLanguageTag)
        .map(ULocale::forLanguageTag)
        .filter(locale -> !LocalesHierarchyUtils.isSameLocale(UNDEFINED_LOCALE, locale));
  }

  private static String sanitizeLanguageTag(final String languageTag) {
    return languageTag
        // A languageTag like en_SG@calendar=buddhist should be parseable, but the parse method
        // fails if the character "@" is present ... so we replace it by the "-u-" character
        // chain, which then parses nicely.
        .replaceAll("@", "-u-")
        // Replace all underscores with hyphens
        .replaceAll("_", "-")
        // Replace all equal signs with hyphens
        .replaceAll("=", "-");
  }
}
