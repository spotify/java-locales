/*-
 * -\-\-
 * locales-common
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

package com.spotify.i18n.locales.common;

import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.model.RelatedReferenceLocale;
import com.spotify.i18n.locales.utils.available.AvailableLocalesUtils;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Represents an engine that enables reference locales based operations, most notably to join
 * datasets by enabling match operations between an origin and a target locale, and enabling
 * filtering on the affinity between these locales.
 *
 * <p>Reference locales are unique and standard locales based on which join operations between
 * datasets are safe to perform. This is required as different locales (or language tags) might
 * actually reference the same language, while having very different identifiers.
 *
 * <p>As an example: we would like to join dataset A and B based on language identifiers.
 *
 * <ul>
 *   <li>Dataset A contains an entry with the language identifier zh-HK.
 *   <li>Dataset B contains an entry with the identifier zh-Hant.
 *   <li>A direct join operation is not possible, as the identifiers differ.
 *   <li>We solve this problem by introducing an intermediary reference locale, which enables both
 *       datasets to be joined.
 * </ul>
 *
 * <p>This join operation must be a 3-step one:
 *
 * <ul>
 *   <li>Calculate all related reference locales for a given origin locale (ex: user language
 *       identifier). This is done using the {@link #calculateRelatedReferenceLocales(String)}
 *       method.
 *   <li>Calculate the best matching reference locale for a given target locale (ex: content
 *       language identifier). This is done using the {@link
 *       #calculateBestMatchingReferenceLocale(String)} method.
 *   <li>Join datasets based on the calculated reference locale ({@link
 *       RelatedReferenceLocale#referenceLocale()} and the output of {@link
 *       #calculateBestMatchingReferenceLocale(String)}), and filter based on the desired level of
 *       {@link RelatedReferenceLocale#affinity()}.
 * </ul>
 *
 * @see RelatedReferenceLocale
 * @see AvailableLocalesUtils#getReferenceLocales()
 * @author Eric Fjøsne
 */
public interface ReferenceLocalesCalculator {

  /**
   * Returns the list of related reference locales, along with their calculated affinity, for the
   * given language tag.
   *
   * @param languageTag language tag
   * @return List of related reference locales, along with their calculated affinity
   */
  List<RelatedReferenceLocale> calculateRelatedReferenceLocales(@Nullable final String languageTag);

  /**
   * Returns the best matching reference locale for a given language tag.
   *
   * @param languageTag language tag
   * @return the optional best matching reference locale
   */
  Optional<ULocale> calculateBestMatchingReferenceLocale(@Nullable final String languageTag);
}
