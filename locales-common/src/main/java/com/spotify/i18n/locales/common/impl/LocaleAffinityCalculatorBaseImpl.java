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

import static com.spotify.i18n.locales.common.impl.LocaleAffinityBiCalculatorBaseImpl.convertDistanceToAffinityScore;
import static com.spotify.i18n.locales.common.impl.LocaleAffinityBiCalculatorBaseImpl.convertScoreToLocaleAffinity;
import static com.spotify.i18n.locales.common.impl.LocaleAffinityBiCalculatorBaseImpl.getBestDistanceBetweenLSR;
import static com.spotify.i18n.locales.common.impl.LocaleAffinityBiCalculatorBaseImpl.getMaximizedLanguageScriptRegion;
import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isRootLocale;
import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isSameLocale;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.ibm.icu.impl.locale.LSR;
import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.LocaleAffinityCalculator;
import com.spotify.i18n.locales.common.model.LocaleAffinity;
import com.spotify.i18n.locales.common.model.LocaleAffinityResult;
import com.spotify.i18n.locales.utils.language.LanguageUtils;
import com.spotify.i18n.locales.utils.languagetag.LanguageTagUtils;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base implementation of {@link LocaleAffinityCalculator} that calculates the locale affinity for a
 * given language tag, against a set of locales.
 *
 * <p>This class is not intended for public subclassing. New object instances must be created using
 * the builder pattern, starting with the {@link #builder()} method.
 *
 * @author Eric Fjøsne
 */
@AutoValue
public abstract class LocaleAffinityCalculatorBaseImpl implements LocaleAffinityCalculator {

  /**
   * Returns the set of {@link ULocale} against which affinity is being calculated.
   *
   * @return set of locales
   */
  public abstract Set<ULocale> againstLocales();

  /**
   * Returns the set of best matching spoken {@link ULocale} against which affinity is being
   * calculated.
   *
   * @return set of locales
   */
  abstract Set<ULocale> againstSpokenLocales();

  /**
   * Returns the set of maximized {@link LSR} against which affinity is being calculated.
   *
   * @return set of locales
   */
  abstract Set<LSR> againstMaximizedLSRs();

  /**
   * Returns the calculated {@link LocaleAffinityResult} for the given language tag
   *
   * @return the locale affinity result
   */
  @Override
  public LocaleAffinityResult calculate(@Nullable final String languageTag) {
    return LocaleAffinityResult.builder().affinity(getAffinity(languageTag)).build();
  }

  private LocaleAffinity getAffinity(@Nullable final String languageTag) {
    if (againstLocales().isEmpty()) {
      return LocaleAffinity.NONE;
    } else {
      // We attempt to match based on corresponding spoken language first, and make use of the
      // score-based affinity calculation as fallback.
      if (hasSameSpokenLanguageAffinity(languageTag)) {
        return LocaleAffinity.SAME;
      } else {
        return calculateScoreBasedAffinity(languageTag);
      }
    }
  }

  private boolean hasSameSpokenLanguageAffinity(@Nullable final String languageTag) {
    return LanguageUtils.getSpokenLanguageLocale(languageTag)
        .map(
            spokenLanguageLocale ->
                againstSpokenLocales().stream()
                    .anyMatch(
                        againstSpokenLocale ->
                            isSameLocale(spokenLanguageLocale, againstSpokenLocale)))
        .orElse(false);
  }

  private LocaleAffinity calculateScoreBasedAffinity(String languageTag) {
    int bestDistance = getBestDistance(languageTag);
    int correspondingScore = convertDistanceToAffinityScore(bestDistance);
    return convertScoreToLocaleAffinity(correspondingScore);
  }

  private int getBestDistance(@Nullable final String languageTag) {
    return LanguageTagUtils.parse(languageTag)
        .filter(LocaleAffinityBiCalculatorBaseImpl::isAvailableLanguage)
        .map(parsed -> getMaximizedLanguageScriptRegion(parsed))
        .map(
            maxParsed ->
                againstMaximizedLSRs().stream()
                    .map(maxAgainst -> getBestDistanceBetweenLSR(maxParsed, maxAgainst))
                    .min(Integer::compare)
                    .orElse(Integer.MAX_VALUE))
        .orElse(Integer.MAX_VALUE);
  }

  /**
   * Returns a {@link Builder} instance that will allow you to manually create a {@link
   * LocaleAffinityCalculatorBaseImpl} instance.
   *
   * @return The builder
   */
  public static Builder builder() {
    return new AutoValue_LocaleAffinityCalculatorBaseImpl.Builder();
  }

  /** A builder for a {@link LocaleAffinityCalculatorBaseImpl}. */
  @AutoValue.Builder
  public abstract static class Builder {
    Builder() {} // package private constructor

    /**
     * Configures the set of {@link ULocale} against which affinity will be calculated.
     *
     * @param locales
     * @return The {@link Builder} instance
     */
    public abstract Builder againstLocales(final Set<ULocale> locales);

    /**
     * Configures the set of best matching spoken {@link ULocale} against which affinity will be
     * calculated.
     *
     * @param locales spoken locales
     * @return The {@link Builder} instance
     */
    abstract Builder againstSpokenLocales(final Set<ULocale> locales);

    /**
     * Configures the set of maximized {@link LSR} against which affinity will be calculated.
     *
     * @param maximizedLSR
     * @return The {@link Builder} instance
     */
    abstract Builder againstMaximizedLSRs(final Set<LSR> maximizedLSR);

    abstract Set<ULocale> againstLocales();

    abstract LocaleAffinityCalculatorBaseImpl autoBuild();

    /** Builds a {@link LocaleAffinityCalculator} out of this builder. */
    public final LocaleAffinityCalculator build() {
      for (ULocale locale : againstLocales()) {
        Preconditions.checkState(
            !isRootLocale(locale),
            "The locales against which affinity needs to be calculated cannot contain the root.");
      }

      // Filter out locales with a language unavailable in CLDR
      againstLocales(
          againstLocales().stream()
              .filter(LocaleAffinityBiCalculatorBaseImpl::isAvailableLanguage)
              .collect(Collectors.toSet()));

      // Prepare the best matching spoken locales set, for faster calculations
      againstSpokenLocales(
          againstLocales().stream()
              .map(ULocale::toLanguageTag)
              .map(LanguageUtils::getSpokenLanguageLocale)
              .flatMap(Optional::stream)
              .collect(Collectors.toSet()));

      // Prepare the maximized LSR set, for faster calculations
      againstMaximizedLSRs(
          againstLocales().stream()
              .map(LocaleAffinityBiCalculatorBaseImpl::getMaximizedLanguageScriptRegion)
              .collect(Collectors.toSet()));

      return autoBuild();
    }
  }
}
