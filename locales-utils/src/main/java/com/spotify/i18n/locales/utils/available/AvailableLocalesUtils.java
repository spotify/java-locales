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

import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isSameLocale;

import com.ibm.icu.util.ULocale;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class AvailableLocalesUtils {

  // Outlier locale we don't want to encounter
  private static final ULocale EN_US_POSIX = ULocale.forLanguageTag("en-US-POSIX");

  // Set containing all CLDR available locales, except the ROOT and en-US-POSIX
  private static final Set<ULocale> CLDR_LOCALES =
      Arrays.stream(ULocale.getAvailableLocales())
          .filter(l -> !isSameLocale(l, ULocale.ROOT) && !isSameLocale(l, EN_US_POSIX))
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
}
