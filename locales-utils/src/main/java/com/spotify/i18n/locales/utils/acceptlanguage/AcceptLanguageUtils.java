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

package com.spotify.i18n.locales.utils.acceptlanguage;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A Utility class that provides helpers to perform accept-language values related operations.
 *
 * <p>The values returned by these helpers should never be trusted as matching supported locale(s)
 * and/or language(s) for your use case. If you are looking for the best matching supported language
 * or locale matching a given Accept-Language value, you need to use the locale resolution helpers.
 *
 * @see <a
 *     href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Language">Accept-Language
 *     headers documentation</a>
 * @see <a href="https://en.wikipedia.org/wiki/IETF_language_tag">IETF BCP 47 language tag</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc4647">RFC 4647 Matching of Language
 *     Tags</a> (defines what a language-range is)
 * @author Eric Fjøsne
 */
public class AcceptLanguageUtils {

  private static final Pattern PATTERN_NEGATIVE_WEIGHTS = Pattern.compile(";q=-[.0-9]+");

  /**
   * Returns the normalized accept-language value, for a given accept-language value. By normalized,
   * we mean a value that has been sanitized from any invalid input, where each entry is fully
   * specified (language tag as per BCP47 formatting and related weight) and ordered by descending
   * weight value.
   *
   * <p>If provided value is empty, null or invalid, returns an empty String.
   *
   * @param acceptLanguageValue
   * @return Normalized accept-language value
   */
  public static String normalize(final String acceptLanguageValue) {
    return parse(acceptLanguageValue).stream()
        .map(LanguageRange::toString)
        .collect(Collectors.joining(","));
  }

  /**
   * Returns the list of normalized accept-language entries, for a given accept-language value. By
   * normalized, we mean a value that has been sanitized from any invalid input, where each entry is
   * fully specified (language tag as per BCP47 formatting and related weight) and ordered by
   * descending weight value.
   *
   * <p>If provided value is empty, null or invalid, returns an empty List.
   *
   * @param acceptLanguageValue
   * @return List of normalized {@link LanguageRange}
   */
  public static List<LanguageRange> parse(final String acceptLanguageValue) {
    return Optional.ofNullable(acceptLanguageValue)
        .map(AcceptLanguageUtils::parseGivenValue)
        .orElse(Collections.emptyList());
  }

  private static List<LanguageRange> parseGivenValue(final String acceptLanguage) {
    return Optional.ofNullable(acceptLanguage)
        .map(AcceptLanguageUtils::sanitizeAcceptLanguage)
        .filter(Predicate.not(String::isEmpty))
        .map(AcceptLanguageUtils::parseSanitizedValue)
        .orElse(Collections.emptyList())
        .stream()
        .sorted(Comparator.comparing(LanguageRange::getWeight).reversed())
        .distinct()
        .collect(Collectors.toList());
  }

  private static String sanitizeAcceptLanguage(final String acceptLanguage) {
    final String sanitizedValue =
        acceptLanguage
            // Remove all spaces
            .replaceAll(" ", "")
            // A locale like en_SG@calendar=buddhist should be parseable, but the parse method
            // fails if the character "@" is present ... so we replace it by the "-u-" character
            // chain, which then parses nicely.
            .replaceAll("@", "-u-")
            // Replace all underscores with hyphens, to successfully parse badly formatted
            // values
            .replaceAll("_", "-")
            // Remove all locale extensions (as per BCP47), because they cannot be parsed
            .replaceAll("-u-([^,;]*)", "");
    // Negative q values are not authorized ... but we receive them and therefore should handle
    // them. We replace the negative values by zero.
    // https://www.rfc-editor.org/rfc/rfc9110#field.accept-language
    // https://www.rfc-editor.org/rfc/rfc9110#quality.values
    return PATTERN_NEGATIVE_WEIGHTS.matcher(sanitizedValue).replaceAll(";q=0.0");
  }

  private static List<LanguageRange> parseSanitizedValue(final String acceptLanguage) {
    try {
      return LanguageRange.parse(acceptLanguage);
    } catch (Throwable t) {
      // Failing nicely when encountering a value that cannot be parsed
      return Collections.emptyList();
    }
  }
}
