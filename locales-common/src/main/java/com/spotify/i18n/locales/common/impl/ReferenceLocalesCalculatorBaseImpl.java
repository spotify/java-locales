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

package com.spotify.i18n.locales.common.impl;

import com.google.auto.value.AutoValue;
import com.ibm.icu.util.LocaleMatcher;
import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.LocaleAffinityCalculator;
import com.spotify.i18n.locales.common.ReferenceLocalesCalculator;
import com.spotify.i18n.locales.common.model.LocaleAffinity;
import com.spotify.i18n.locales.common.model.ReferenceLocale;
import com.spotify.i18n.locales.utils.languagetag.LanguageTagUtils;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base implementation of an engine that enables reference locales based operations, most notably to
 * join datasets by enabling match operations between an origin and a target locale, and enabling
 * filtering on the affinity between these locales.
 *
 * <p>Read more about when to use this implementation in {@link ReferenceLocalesCalculator}.
 *
 * @see ReferenceLocale
 * @author Eric Fjøsne
 */
@AutoValue
public abstract class ReferenceLocalesCalculatorBaseImpl implements ReferenceLocalesCalculator {

  /** Prepared {@link LocaleMatcher}, ready to find the best matching reference locale */
  private static final LocaleMatcher REFERENCE_LOCALE_MATCHER =
      LocaleMatcher.builder()
          .setSupportedULocales(ReferenceLocale.availableReferenceLocales())
          .setNoDefaultLocale()
          .build();

  /**
   * Returns the list of related reference locales, along with their calculated affinity, for the
   * given language tag.
   *
   * @param languageTag language tag
   * @return List of related reference locales, along with their calculated affinity
   */
  @Override
  public List<ReferenceLocale> calculateRelatedReferenceLocales(
      @Nullable final String languageTag) {
    return LanguageTagUtils.parse(languageTag)
        .map(this::getRelatedReferenceLocales)
        .orElse(Collections.emptyList());
  }

  private List<ReferenceLocale> getRelatedReferenceLocales(final ULocale locale) {
    final LocaleAffinityCalculator affinityCalculator = buildAffinityCalculator(locale);
    return ReferenceLocale.availableReferenceLocales().stream()
        .map(
            refLocale ->
                ReferenceLocale.builder()
                    .locale(refLocale)
                    .affinity(affinityCalculator.calculate(refLocale.toLanguageTag()).affinity())
                    .build())
        // We only retain reference locales with some level of affinity
        .filter(refLocale -> refLocale.affinity() != LocaleAffinity.NONE)
        .collect(Collectors.toList());
  }

  private LocaleAffinityCalculator buildAffinityCalculator(final ULocale locale) {
    return LocaleAffinityCalculatorBaseImpl.builder().againstLocales(Set.of(locale)).build();
  }

  /**
   * Returns the best matching reference locale for a given language tag.
   *
   * @param languageTag language tag
   * @return the optional best matching reference locale
   */
  @Override
  public Optional<ULocale> calculateBestMatchingReferenceLocale(
      @Nullable final String languageTag) {
    return LanguageTagUtils.parse(languageTag).map(REFERENCE_LOCALE_MATCHER::getBestMatch);
  }

  /**
   * Returns a {@link Builder} instance that will allow you to manually create a {@link
   * ReferenceLocalesCalculatorBaseImpl} instance.
   *
   * @return The builder
   */
  public static Builder builder() {
    return new AutoValue_ReferenceLocalesCalculatorBaseImpl.Builder();
  }

  /** A builder for a {@link ReferenceLocalesCalculatorBaseImpl}. */
  @AutoValue.Builder
  public abstract static class Builder {
    Builder() {}

    abstract ReferenceLocalesCalculatorBaseImpl autoBuild();

    /** Builds a {@link ReferenceLocalesCalculator} out of this builder. */
    public final ReferenceLocalesCalculator build() {
      return autoBuild();
    }
  }
}
