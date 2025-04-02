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
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Represents an engine that enables reference locales based operations, most notably to join
 * datasets, based on an origin source supported locale, and a target locale to match.
 *
 * <p>We define a reference locale as a unique and standard locale based on which join operations
 * between datasets are safe to perform. This is required as different locales (or language tags)
 * might actually point to the same language, but have very different identifiers.
 *
 * <p>As an example: we would like to join dataset A and B based on a given language identifier.
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
 *   <li>Calculate all related reference locales for a given supported locale (ex: supported user
 *       language). This is done using the {@link
 *       #getRelatedReferenceLocalesForSupportedLocale(String)} method.
 *   <li>Calculate the best matching reference locale for a given target locale (ex: target content
 *       language). This is done using the {@link
 *       #getBestMatchingReferenceLocaleForTargetLocale(String)}
 *   <li>Join datasets based on calculated reference locale only ({@link
 *       RelatedReferenceLocale#referenceLocale()} and output of {@link
 *       #getBestMatchingReferenceLocaleForTargetLocale(String)}), and filter based on the
 *       calculated {@link RelatedReferenceLocale#affinity()}.
 * </ul>
 *
 * @author Eric Fjøsne
 */
public interface RelatedReferenceLocalesCalculator {

  /**
   * For a given supported locale, returns the list of related reference locales, along with their
   * calculated affinities with the supported locale.
   *
   * @param languageTag supported locale identifier
   * @return List of related reference locales, along with the calculated affinity
   */
  List<RelatedReferenceLocale> getRelatedReferenceLocalesForSupportedLocale(
      @Nullable final String languageTag);

  /**
   * For a given target locale, returns the best matching reference locale.
   *
   * @param languageTag target locale identifier
   * @return the optional best matching reference locale
   */
  Optional<ULocale> getBestMatchingReferenceLocaleForTargetLocale(
      @Nullable final String languageTag);
}
