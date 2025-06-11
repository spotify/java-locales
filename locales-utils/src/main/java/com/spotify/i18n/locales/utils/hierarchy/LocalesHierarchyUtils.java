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

package com.spotify.i18n.locales.utils.hierarchy;

import com.google.common.base.Preconditions;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.ULocale.Builder;
import com.spotify.i18n.locales.utils.available.AvailableLocalesUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A Utility class that provides helpers to perform all locales hierarchy related operations and
 * checks. It works exclusively based on {@link ULocale}s and assumes that all given parameters are
 * non-null and locales available in CLDR.
 *
 * @see ULocale
 * @author Eric Fjøsne
 */
public class LocalesHierarchyUtils {

  private static final Map<ULocale, ULocale> CHILD_TO_PARENT_MAP =
      generateSpecialCldrChildToParentsMap();

  /** Set of language codes for which the script is a differentiator in CLDR */
  static final Set<String> LANGUAGE_CODES_WITH_MULTIPLE_SCRIPTS_IN_CLDR =
      Arrays.stream(ULocale.getAvailableLocales())
          .filter(locale -> !locale.getScript().isEmpty())
          .map(ULocale::getLanguage)
          .collect(Collectors.toSet());

  /**
   * Returns the {@link Set} of all {@link ULocale}s that are descendants of the given locale,
   * according to the CLDR hierarchy.
   *
   * @param locale the locale
   * @return all of its descendant available locales, according to the CLDR hierarchy
   */
  public static Set<ULocale> getDescendantLocales(final ULocale locale) {
    Preconditions.checkNotNull(locale);
    if (isRootLocale(locale)) {
      // Optimization when requesting descendants of ROOT
      return AvailableLocalesUtils.getCldrLocales();
    } else {
      return AvailableLocalesUtils.getCldrLocales().stream()
          .filter(ul -> isDescendantLocale(ul, locale))
          .collect(Collectors.toSet());
    }
  }

  /**
   * Returns the {@link List} of {@link ULocale}s that are ancestors of the given locale, according
   * to the CLDR hierarchy, ordered from immediate parent all the way to the ROOT (included in the
   * returned list).
   *
   * @param locale the locale
   * @return all of its ancestors, ordered from immediate parent to the ROOT locale (included)
   */
  public static List<ULocale> getAncestorLocales(final ULocale locale) {
    Preconditions.checkNotNull(locale);
    if (isRootLocale(locale)) {
      return List.of();
    }
    List<ULocale> ancestors = new ArrayList<>();
    Optional<ULocale> currentOpt = getParentLocale(locale);
    do {
      ULocale current = currentOpt.get();
      ancestors.add(current);
      currentOpt = getParentLocale(current);
    } while (currentOpt.isPresent());
    return ancestors;
  }

  /**
   * Returns the highest ancestor {@link ULocale}s (ROOT excluded) for the given locale, according
   * to the CLDR hierarchy.
   *
   * @param locale the locale
   * @return highest ancestor locale, ROOT excluded
   * @throws IllegalArgumentException when given locale is the ROOT
   */
  public static ULocale getHighestAncestorLocale(final ULocale locale) {
    Preconditions.checkNotNull(locale);
    Preconditions.checkArgument(!isRootLocale(locale), "Param locale cannot be the ROOT.");
    ULocale highestAncestor = locale;
    while (true) {
      Optional<ULocale> currentOpt = getParentLocale(highestAncestor);
      if (currentOpt.isEmpty()) {
        return highestAncestor;
      }
      if (currentOpt.get().equals(ULocale.ROOT)) {
        return highestAncestor;
      }
      highestAncestor = currentOpt.get();
    }
  }

  /**
   * Returns true if the given locale is the ROOT locale
   *
   * @param underTest the locale under test
   * @return true if the given locale is the ROOT locale
   */
  public static boolean isRootLocale(final ULocale underTest) {
    return isSameLocale(underTest, ULocale.ROOT);
  }

  /**
   * Returns true if the two given locales are the same.
   *
   * @param l1 the first locale
   * @param l2 the second locale
   * @return true if both locales are the same
   */
  public static boolean isSameLocale(final ULocale l1, final ULocale l2) {
    Preconditions.checkNotNull(l1);
    Preconditions.checkNotNull(l2);
    return l1.equals(l2);
  }

  /**
   * Returns true if the locale under test is the descendant (direct of subsequent) of the given
   * ancestor locale, according to the CLDR hierarchy
   *
   * @param underTest the locale under test
   * @param ancestorLocale the locale supposed to be one of the ancestors of underTest
   * @return true if underTest is a descendant of the ancestorLocale.
   */
  public static boolean isDescendantLocale(final ULocale underTest, final ULocale ancestorLocale) {
    Preconditions.checkNotNull(underTest);
    Preconditions.checkNotNull(ancestorLocale);

    // Both locales are the same
    if (isSameLocale(underTest, ancestorLocale)) {
      return false;
    }

    // We start from the underTest locale position and go up in the hierarchy
    Optional<ULocale> currentOpt = getParentLocale(underTest);
    while (true) {
      // We have gone up all the way in the hierarchy and didn't find a match.
      if (!currentOpt.isPresent()) {
        return false;
      }
      // If the current locale is the parent one that we seek, it is a bingo.
      if (isSameLocale(currentOpt.get(), ancestorLocale)) {
        return true;
      }
      currentOpt = getParentLocale(currentOpt.get());
    }
  }

  /**
   * Returns true if the locale under test is the direct child of the given parent locale, according
   * to the CLDR hierarchy
   *
   * @param underTest the locale under test
   * @param parentLocale the locale supposed to be one of the ancestors of underTest
   * @return true if underTest is a direct child of the parentLocale.
   */
  public static boolean isChildLocale(final ULocale underTest, final ULocale parentLocale) {
    Preconditions.checkNotNull(underTest);
    Preconditions.checkNotNull(parentLocale);

    // Both locales are the same
    if (isSameLocale(underTest, parentLocale)) {
      return false;
    }

    Optional<ULocale> parentOpt = getParentLocale(underTest);
    return parentOpt.isPresent() && isSameLocale(parentOpt.get(), parentLocale);
  }

  /**
   * Returns true if the locale under test is a direct child of the ROOT locale, according to the
   * CLDR hierarchy
   *
   * @param underTest the locale under test
   * @return true if underTest is a direct child of the ROOT locale.
   */
  public static boolean isHighestAncestorLocale(final ULocale underTest) {
    Preconditions.checkNotNull(underTest);
    return isChildLocale(underTest, ULocale.ROOT);
  }

  /**
   * Returns true for the given language code identifying a language that can be written using
   * different scripts.
   *
   * @param languageCode the language code
   * @return true if language can be written using different scripts.
   */
  public static boolean isLanguageWrittenInSeveralScripts(final String languageCode) {
    Preconditions.checkNotNull(languageCode);
    return LANGUAGE_CODES_WITH_MULTIPLE_SCRIPTS_IN_CLDR.contains(languageCode);
  }

  /**
   * Returns the optional parent {@link ULocale} according to CLDR, for a given locale.
   *
   * <p>This method considers special parent locales maintained in CLDR.
   *
   * @param locale the locale of which we want to get the parent of
   * @return the optional parent locale, according to CLDR
   */
  public static Optional<ULocale> getParentLocale(final ULocale locale) {
    Preconditions.checkNotNull(locale);
    if (CHILD_TO_PARENT_MAP.containsKey(locale)) {
      return Optional.of(CHILD_TO_PARENT_MAP.get(locale));
    } else {
      return getParentLocaleBasedOnFallback(locale);
    }
  }

  /**
   * Returns the parent {@link ULocale} according to CLDR, for a given locale, based on its
   * fallback. By default, the fallback removes the last identifier from the tag, which might not be
   * enough as the script associated with the locale can be a language differentiator for selected
   * languages.
   *
   * @param locale A locale for which no explicit parent mapping is defined in CLDR
   * @return The parent locale, according to CLDR
   */
  private static Optional<ULocale> getParentLocaleBasedOnFallback(final ULocale locale) {
    if (isRootLocale(locale)) {
      return Optional.empty();
    } else if (isRootLocale(locale.getFallback())) {
      return Optional.of(ULocale.ROOT);
    } else if (isLanguageWrittenInSeveralScripts(locale.getLanguage())) {
      // When we know the script is a major differentiator, we assess the parent locale based on it.
      return getParentLocaleBasedOnLocaleAndScript(locale);
    } else {
      // When the script isn't a major differentiator, we can confidently return the fallback
      // property of the given locale as parent locale, as long as it doesn't contain any script
      // code. Otherwise, we assess the parent locale based on the locale script, to prevent
      // unavailable yet valid locales like es-Arab from being considered.
      if (locale.getScript().isEmpty()) {
        return Optional.of(locale.getFallback());
      } else {
        return getParentLocaleBasedOnLocaleAndScript(locale);
      }
    }
  }

  /**
   * Returns the parent {@link ULocale} according to CLDR, for a given locale for which the script
   * can be a language differentiator, based on its fallback. We need this to handle cases like
   * zh-TW, for which the parent locale should be zh-Hant and not zh. By default, the fallback logic
   * removes the last identifier from the tag.
   *
   * @param locale A locale for which no explicit parent mapping is defined in CLDR
   * @return The parent locale, according to CLDR
   */
  private static Optional<ULocale> getParentLocaleBasedOnLocaleAndScript(final ULocale locale) {
    // We retrieve the script from the given locale
    final String localeScript = getLocaleScript(locale);
    // We retrieve the script from the fallback locale
    final String parentLocaleScript = getLocaleScript(locale.getFallback());

    if (localeScript.equals(parentLocaleScript)) {
      // If they are a match, we can safely return the fallback.
      return Optional.of(locale.getFallback());
    } else {
      final ULocale localeWithScriptOverride =
          new Builder()
              // Build with the base language tag
              .setLanguageTag(locale.toLanguageTag())
              // Override the script only
              .setScript(localeScript)
              .build();
      if (isSameLocale(locale, localeWithScriptOverride)) {
        // This is most likely a locale that is not available in CLDR, with a wrong combination
        // of language code and script code.
        return Optional.empty();
      } else {
        // When they are not a match, we call the same method by force-feeding the script
        // identifier to the given locale. Ex: for zh-TW, we will be calling this same method,
        // with the overridden zh-Hant-TW locale.
        return getParentLocale(localeWithScriptOverride);
      }
    }
  }

  /**
   * Returns a given locale script
   *
   * @param locale any locale
   * @return the script associated with the locale
   */
  private static String getLocaleScript(final ULocale locale) {
    // We only calculate the script if it isn't yet present in the locale, as addLikelySubtag can be
    // a time-consuming operation.
    return Optional.of(locale)
        .map(ULocale::getScript)
        .filter(Predicate.not(String::isEmpty))
        .orElseGet(() -> ULocale.addLikelySubtags(locale).getScript());
  }

  /**
   * Returns a {@link Map} containing the {@link ULocale}s and their corresponding parent {@link
   * ULocale}. It only contains the ones for which the parent locale differs from the one returned
   * by the {@link ULocale#getFallback()} method, according to CLDR.
   *
   * <p>The list of special parents used here is based on the linked json file below, as it existed
   * for commit 30c5cce from Feb 4 2025.
   *
   * @see <a
   *     href="https://github.com/unicode-org/cldr-json/blob/main/cldr-json/cldr-core/supplemental/parentLocales.json">parentLocales.json</a>
   * @return
   */
  private static Map<ULocale, ULocale> generateSpecialCldrChildToParentsMap() {
    HashMap<String, String> map = new HashMap<>();
    map.put("en-150", "en-001");
    map.put("en-AG", "en-001");
    map.put("en-AI", "en-001");
    map.put("en-AU", "en-001");
    map.put("en-BB", "en-001");
    map.put("en-BM", "en-001");
    map.put("en-BS", "en-001");
    map.put("en-BW", "en-001");
    map.put("en-BZ", "en-001");
    map.put("en-CC", "en-001");
    map.put("en-CK", "en-001");
    map.put("en-CM", "en-001");
    map.put("en-CX", "en-001");
    map.put("en-CY", "en-001");
    map.put("en-DG", "en-001");
    map.put("en-DM", "en-001");
    map.put("en-ER", "en-001");
    map.put("en-FJ", "en-001");
    map.put("en-FK", "en-001");
    map.put("en-FM", "en-001");
    map.put("en-GB", "en-001");
    map.put("en-GD", "en-001");
    map.put("en-GG", "en-001");
    map.put("en-GH", "en-001");
    map.put("en-GI", "en-001");
    map.put("en-GM", "en-001");
    map.put("en-GS", "en-001");
    map.put("en-GY", "en-001");
    map.put("en-HK", "en-001");
    map.put("en-ID", "en-001");
    map.put("en-IE", "en-001");
    map.put("en-IL", "en-001");
    map.put("en-IM", "en-001");
    map.put("en-IN", "en-001");
    map.put("en-IO", "en-001");
    map.put("en-JE", "en-001");
    map.put("en-JM", "en-001");
    map.put("en-KE", "en-001");
    map.put("en-KI", "en-001");
    map.put("en-KN", "en-001");
    map.put("en-KY", "en-001");
    map.put("en-LC", "en-001");
    map.put("en-LR", "en-001");
    map.put("en-LS", "en-001");
    map.put("en-MG", "en-001");
    map.put("en-MO", "en-001");
    map.put("en-MS", "en-001");
    map.put("en-MT", "en-001");
    map.put("en-MU", "en-001");
    map.put("en-MV", "en-001");
    map.put("en-MW", "en-001");
    map.put("en-MY", "en-001");
    map.put("en-NA", "en-001");
    map.put("en-NF", "en-001");
    map.put("en-NG", "en-001");
    map.put("en-NR", "en-001");
    map.put("en-NU", "en-001");
    map.put("en-NZ", "en-001");
    map.put("en-PG", "en-001");
    map.put("en-PK", "en-001");
    map.put("en-PN", "en-001");
    map.put("en-PW", "en-001");
    map.put("en-RW", "en-001");
    map.put("en-SB", "en-001");
    map.put("en-SC", "en-001");
    map.put("en-SD", "en-001");
    map.put("en-SG", "en-001");
    map.put("en-SH", "en-001");
    map.put("en-SL", "en-001");
    map.put("en-SS", "en-001");
    map.put("en-SX", "en-001");
    map.put("en-SZ", "en-001");
    map.put("en-TC", "en-001");
    map.put("en-TK", "en-001");
    map.put("en-TO", "en-001");
    map.put("en-TT", "en-001");
    map.put("en-TV", "en-001");
    map.put("en-TZ", "en-001");
    map.put("en-UG", "en-001");
    map.put("en-VC", "en-001");
    map.put("en-VG", "en-001");
    map.put("en-VU", "en-001");
    map.put("en-WS", "en-001");
    map.put("en-ZA", "en-001");
    map.put("en-ZM", "en-001");
    map.put("en-ZW", "en-001");
    map.put("en-AT", "en-150");
    map.put("en-BE", "en-150");
    map.put("en-CH", "en-150");
    map.put("en-CZ", "en-150");
    map.put("en-DE", "en-150");
    map.put("en-DK", "en-150");
    map.put("en-ES", "en-150");
    map.put("en-FI", "en-150");
    map.put("en-FR", "en-150");
    map.put("en-HU", "en-150");
    map.put("en-IT", "en-150");
    map.put("en-NL", "en-150");
    map.put("en-NO", "en-150");
    map.put("en-PL", "en-150");
    map.put("en-PT", "en-150");
    map.put("en-RO", "en-150");
    map.put("en-SE", "en-150");
    map.put("en-SI", "en-150");
    map.put("en-SK", "en-150");
    map.put("hi-Latn", "en-IN");
    map.put("es-AR", "es-419");
    map.put("es-BO", "es-419");
    map.put("es-BR", "es-419");
    map.put("es-BZ", "es-419");
    map.put("es-CL", "es-419");
    map.put("es-CO", "es-419");
    map.put("es-CR", "es-419");
    map.put("es-CU", "es-419");
    map.put("es-DO", "es-419");
    map.put("es-EC", "es-419");
    map.put("es-GT", "es-419");
    map.put("es-HN", "es-419");
    map.put("es-JP", "es-419");
    map.put("es-MX", "es-419");
    map.put("es-NI", "es-419");
    map.put("es-PA", "es-419");
    map.put("es-PE", "es-419");
    map.put("es-PR", "es-419");
    map.put("es-PY", "es-419");
    map.put("es-SV", "es-419");
    map.put("es-US", "es-419");
    map.put("es-UY", "es-419");
    map.put("es-VE", "es-419");
    map.put("ht", "fr-HT");
    map.put("nb", "no");
    map.put("nn", "no");
    map.put("no-NO", "no");
    map.put("pt-AO", "pt-PT");
    map.put("pt-CH", "pt-PT");
    map.put("pt-CV", "pt-PT");
    map.put("pt-FR", "pt-PT");
    map.put("pt-GQ", "pt-PT");
    map.put("pt-GW", "pt-PT");
    map.put("pt-LU", "pt-PT");
    map.put("pt-MO", "pt-PT");
    map.put("pt-MZ", "pt-PT");
    map.put("pt-ST", "pt-PT");
    map.put("pt-TL", "pt-PT");
    map.put("az-Arab", "und");
    map.put("az-Cyrl", "und");
    map.put("bal-Latn", "und");
    map.put("blt-Latn", "und");
    map.put("bm-Nkoo", "und");
    map.put("bs-Cyrl", "und");
    map.put("byn-Latn", "und");
    map.put("cu-Glag", "und");
    map.put("dje-Arab", "und");
    map.put("dyo-Arab", "und");
    map.put("en-Dsrt", "und");
    map.put("en-Shaw", "und");
    map.put("ff-Adlm", "und");
    map.put("ff-Arab", "und");
    map.put("ha-Arab", "und");
    map.put("iu-Latn", "und");
    map.put("kaa-Latn", "und");
    map.put("kk-Arab", "und");
    map.put("kok-Latn", "und");
    map.put("ks-Deva", "und");
    map.put("ku-Arab", "und");
    map.put("kxv-Deva", "und");
    map.put("kxv-Orya", "und");
    map.put("kxv-Telu", "und");
    map.put("ky-Arab", "und");
    map.put("ky-Latn", "und");
    map.put("ml-Arab", "und");
    map.put("mn-Mong", "und");
    map.put("mni-Mtei", "und");
    map.put("ms-Arab", "und");
    map.put("pa-Arab", "und");
    map.put("sat-Deva", "und");
    map.put("sd-Deva", "und");
    map.put("sd-Khoj", "und");
    map.put("sd-Sind", "und");
    map.put("shi-Latn", "und");
    map.put("so-Arab", "und");
    map.put("sr-Latn", "und");
    map.put("sw-Arab", "und");
    map.put("tg-Arab", "und");
    map.put("ug-Cyrl", "und");
    map.put("uz-Arab", "und");
    map.put("uz-Cyrl", "und");
    map.put("vai-Latn", "und");
    map.put("wo-Arab", "und");
    map.put("yo-Arab", "und");
    map.put("yue-Hans", "und");
    map.put("zh-Hant", "und");
    map.put("zh-Hant-MO", "zh-Hant-HK");
    return map.entrySet().stream()
        .collect(
            Collectors.toMap(
                e -> ULocale.forLanguageTag(e.getKey()),
                e -> ULocale.forLanguageTag(e.getValue())));
  }
}
