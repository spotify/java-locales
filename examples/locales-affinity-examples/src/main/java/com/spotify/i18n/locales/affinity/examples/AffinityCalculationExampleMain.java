/*-
 * -\-\-
 * locales-affinity-examples
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

package com.spotify.i18n.locales.affinity.examples;

import static com.spotify.i18n.locales.common.model.LocaleAffinity.LOW;
import static com.spotify.i18n.locales.common.model.LocaleAffinity.NONE;
import static com.spotify.i18n.locales.common.model.LocaleAffinity.SAME_OR_INTERCHANGEABLE;

import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.LocaleAffinityCalculator;
import com.spotify.i18n.locales.common.LocaleAffinityHelpersFactory;
import com.spotify.i18n.locales.common.model.LocaleAffinity;
import com.spotify.i18n.locales.common.model.LocaleAffinityResult;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Showcase implementation of Java-locales affinity calculation.
 *
 * @author Eric Fjøsne
 */
public class AffinityCalculationExampleMain {

  /**
   * Returns a locale affinity calculator. This method is mostly meant to showcase that there are
   * several ways to create a locale affinity calculator, and therefore returns an instance at
   * random.
   *
   * <p>All built instances will calculate affinity against the same set of locales.
   */
  public static LocaleAffinityCalculator getLocaleAffinityCalculator() {
    switch (((int) Math.random()) % 3) {
      case 0:
        // Build a locale affinity calculator based on a set of language tags.
        return LocaleAffinityHelpersFactory.getDefaultInstance()
            .buildAffinityCalculatorForLanguageTags(getLanguageTags());
      case 1:
        // Build a locale affinity calculator based on an Accept-Language value.
        return LocaleAffinityHelpersFactory.getDefaultInstance()
            .buildAffinityCalculatorForAcceptLanguage(getAcceptLanguage());
      default:
        // Build a locale affinity calculator based on a set of locales
        return LocaleAffinityHelpersFactory.getDefaultInstance()
            .buildAffinityCalculatorForLocales(getLocales());
    }
  }

  /** Returns the set of language tags against which we want to calculate a given locale affinity */
  private static Set<String> getLanguageTags() {
    return Set.of(
        "ar", // Arabic
        "bs", // Bosnian
        "es", // Spanish
        "fr", // French
        "ja", // Japanese
        "pt", // Portuguese
        "sr-Latn", // Serbian (Latin script)
        "zh-Hant" // Traditional Chinese
        );
  }

  /** Returns the set of locales against which we want to calculate a given locale affinity */
  private static Set<ULocale> getLocales() {
    return getLanguageTags().stream().map(ULocale::forLanguageTag).collect(Collectors.toSet());
  }

  /**
   * Returns the Accept-Language value against which we want to calculate a given locale affinity
   *
   * @see <a
   *     href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Language">Accept-Language
   *     headers documentation</a>
   */
  private static String getAcceptLanguage() {
    return "ar;q=1.0,bs;q=0.9,es;q=0.8,fr;q=0.7,ja;q=0.6,pt;q=0.5,sr-Latn;q=0.4,zh-Hant;q=0.3";
  }

  /**
   * Returns a map of language tags, and associated expected {@link LocaleAffinity} enum value,
   * which were all imported as static in this class.
   */
  private static Map<String, LocaleAffinity> getLanguageTagToExpectedAffinityMap() {
    Map<String, LocaleAffinity> map = new HashMap<>();

    // Edge cases
    map.put(" Invalid language tag ", NONE);
    map.put("ok-junk", NONE);
    map.put("apples-and-bananas", NONE);
    map.put("", NONE);
    map.put(null, NONE);

    // Catalan should be matched, since we support Spanish
    map.put("ca", LOW);
    map.put("ca-ES", LOW);
    map.put("ca-AD", LOW);

    // No english should be matched
    map.put("en", NONE);
    map.put("en-GB", NONE);
    map.put("en-US", NONE);

    // Spanish in Europe should ok
    map.put("es-419", SAME_OR_INTERCHANGEABLE);
    map.put("es-GB", SAME_OR_INTERCHANGEABLE);
    map.put("es-US", SAME_OR_INTERCHANGEABLE);

    // Basque should be matched, since we support Spanish
    map.put("eu", LOW);

    // French
    map.put("fr", SAME_OR_INTERCHANGEABLE);
    map.put("fr-BE", SAME_OR_INTERCHANGEABLE);
    map.put("fr-CA", SAME_OR_INTERCHANGEABLE);
    map.put("fr-FR", SAME_OR_INTERCHANGEABLE);

    // Galician should be matched, since we support Spanish
    map.put("gl", LOW);

    // Hindi shouldn't be matched
    map.put("hi", NONE);

    // Croatian should be nicely matched with Bosnian
    map.put("hr-HR", SAME_OR_INTERCHANGEABLE);

    // Serbian Cyrillic should be matched, although only Latin script is supported
    map.put("sr", SAME_OR_INTERCHANGEABLE);
    map.put("sr-Latn", SAME_OR_INTERCHANGEABLE);
    map.put("sr-Cyrl-ME", SAME_OR_INTERCHANGEABLE);

    // Portuguese
    map.put("pt", SAME_OR_INTERCHANGEABLE);
    map.put("pt-BR", SAME_OR_INTERCHANGEABLE);
    map.put("pt-SE", SAME_OR_INTERCHANGEABLE);
    map.put("pt-US", SAME_OR_INTERCHANGEABLE);

    // Only Traditional Chinese should be matched, not Simplified
    map.put("zh-CN", NONE);
    map.put("zh-TW", SAME_OR_INTERCHANGEABLE);
    map.put("zh-HK", SAME_OR_INTERCHANGEABLE);
    return map;
  }

  public static void main(String[] args) {
    final LocaleAffinityCalculator affinityCalculator = getLocaleAffinityCalculator();

    // Example 1: Filter the list of test language tags, and only retain the ones that result in a
    // locale affinity at the level of SAME_OR_INTERCHANGEABLE, therefore guaranteeing that the
    // associated
    System.out.println("========================================");
    System.out.println(
        String.format(
            "Example 1: List of language tags with calculated affinity = %s",
            SAME_OR_INTERCHANGEABLE.name()));
    getLanguageTagToExpectedAffinityMap().keySet().stream()
        .filter(
            languageTag ->
                affinityCalculator.calculate(languageTag).affinity() == SAME_OR_INTERCHANGEABLE)
        .forEach(System.out::println);

    // Example 2: Check that calculated affinity for each language tag matches the expected value.
    System.out.println("========================================");
    System.out.println(
        "Example 2: Check that calculated affinity for each language tag matches the expected value.");
    getLanguageTagToExpectedAffinityMap()
        .forEach(
            (languageTag, expectedAffinity) -> {
              LocaleAffinityResult affinityResult = affinityCalculator.calculate(languageTag);
              if (affinityResult.affinity() != expectedAffinity) {
                throw new IllegalStateException(
                    String.format(
                        "Affinity calculation for language tag [%s] resulted in [%s], but was expected to be [%s].",
                        languageTag, affinityResult.affinity(), expectedAffinity));
              } else {
                System.out.println(
                    String.format(
                        "Affinity calculation for language tag [%s] resulted in the expected affinity [%s]",
                        languageTag, expectedAffinity));
              }
            });
  }
}
