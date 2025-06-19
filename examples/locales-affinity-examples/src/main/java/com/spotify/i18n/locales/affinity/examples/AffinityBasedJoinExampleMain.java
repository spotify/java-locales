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

import com.spotify.i18n.locales.common.LocaleAffinityBiCalculator;
import com.spotify.i18n.locales.common.LocaleAffinityHelpersFactory;
import com.spotify.i18n.locales.common.model.LocaleAffinityResult;
import java.util.List;

/**
 * Showcase implementation of Java-locales affinity calculation
 *
 * @author Eric Fjøsne
 */
public class AffinityBasedJoinExampleMain {

  /** Create a {@link LocaleAffinityBiCalculator} instance out of the factory */
  private static final LocaleAffinityBiCalculator LOCALE_AFFINITY_BI_CALCULATOR =
      LocaleAffinityHelpersFactory.getDefaultInstance().buildAffinityBiCalculator();

  /**
   * Example logic which attempts to join 2 sets of language tags.
   *
   * <p>Possible joins in the execution output are:
   *
   * <ul>
   *   <li>(bs-Cyrl-BA, bs-Latn) -> Join possible with SAME affinity.
   *   <li>(bs-Cyrl-BA, hr-MK) -> Join possible with MUTUALLY_INTELLIGIBLE affinity.
   *   <li>(de, de-AT) -> Join possible with SAME affinity.
   *   <li>(de, gsw-CH) -> Join possible with MUTUALLY_INTELLIGIBLE affinity.
   *   <li>(da-SE, nb-FI) -> Join possible with HIGH affinity.
   *   <li>(en-GB, en-JP) -> Join possible with SAME affinity.
   *   <li>(en-GB, en-SE) -> Join possible with SAME affinity.
   *   <li>(es-BE, ca) -> Join possible with LOW affinity.
   *   <li>(fr-SE, fr-BE-u-ca-gregorian) -> Join possible with SAME affinity.
   *   <li>(fr-SE, fr-CA) -> Join possible with SAME affinity.
   *   <li>(hr-BA, bs-Latn) -> Join possible with MUTUALLY_INTELLIGIBLE affinity.
   *   <li>(hr-BA, hr-MK) -> Join possible with SAME affinity.
   *   <li>(ja-IT, ja@calendar=buddhist) -> Join possible with SAME affinity.
   *   <li>(nl-BE, nl-ZA) -> Join possible with SAME affinity.
   *   <li>(no-SE, nb-FI) -> Join possible with SAME affinity.
   *   <li>(nn-DK, nb-FI) -> Join possible with SAME affinity.
   *   <li>(zh-Hans-US, zh-CN) -> Join possible with SAME affinity.
   * </ul>
   *
   * @param args
   */
  public static void main(String[] args) {
    final List<String> languageTagsInOriginDataset =
        List.of(
            "bs-Cyrl-BA", // Bosnian (Cyrillic), Bosnia and Herzegovina
            "de", // German
            "da-SE", // Danish (Sweden)
            "en-GB", // English (Great-Britain)
            "es-BE", // Spanish (Belgium)
            "fr-SE", // French (Sweden)
            "hr-BA", // Croatian (Bosnia and Herzegovina)
            "it-CH", // Italian (Switzerland)
            "ja-IT", // Japanese (Italy)
            "nl-BE", // Dutch (Belgium)
            "no-SE", // Norwegian (Sweden)
            "nn-DK", // Norwegian Nynorsk (Danemark)
            "zh-Hans-US", // Chinese (Simplified) (USA)
            "zh-HK" // Chinese (Hong-Kong)
            );

    final List<String> languageTagsInTargetDataset =
        List.of(
            "bs-Latn", // Bosnian (Latin)
            "ca", // Catalan
            "de-AT", // German (Austria)
            "en-JP", // English (Japan)
            "en-SE", // English (Sweden)
            "fr-BE-u-ca-gregorian", // French (Belgium), with gregorian calendar extension
            "fr-CA", // French (Canada)
            "gsw-CH", // Swiss German (Switzerland)
            "hr-MK", // Croatian (North Macedonia)
            "ja@calendar=buddhist", // Japanese, with buddhist calendar extension
            "nb-FI", // Norwegian Bokmål (Finland)
            "nl-ZA", // Dutch (South Africa)
            "pt-US", // Portuguese (USA)
            "zh-CN" // Chinese (Mainland China)
            );

    // Iterate through all possible combinations, and calculate the affinity for each of them.
    for (String languageTagInOriginDataset : languageTagsInOriginDataset) {
      for (String languageTagInTargetDataset : languageTagsInTargetDataset) {
        LocaleAffinityResult affinityResult =
            LOCALE_AFFINITY_BI_CALCULATOR.calculate(
                languageTagInOriginDataset, languageTagInTargetDataset);
        switch (affinityResult.affinity()) {
          case NONE:
            break;
          default:
            System.out.println(
                String.format(
                    "(%s, %s) -> Join possible with %s affinity.",
                    languageTagInOriginDataset,
                    languageTagInTargetDataset,
                    affinityResult.affinity()));
            break;
        }
      }
    }
  }
}
