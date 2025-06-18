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

import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isRootLocale;
import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isSameLocale;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.ibm.icu.impl.locale.LSR;
import com.ibm.icu.impl.locale.LikelySubtags;
import com.ibm.icu.impl.locale.LocaleDistance;
import com.ibm.icu.util.LocaleMatcher.Direction;
import com.ibm.icu.util.LocaleMatcher.FavorSubtag;
import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.LocaleAffinityCalculator;
import com.spotify.i18n.locales.common.model.LocaleAffinity;
import com.spotify.i18n.locales.common.model.LocaleAffinityResult;
import com.spotify.i18n.locales.utils.language.LanguageUtils;
import com.spotify.i18n.locales.utils.languagetag.LanguageTagUtils;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Optional;
import java.util.Set;

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

  // LocaleDistance.INSTANCE is commented as VisibleForTesting, so not ideal ... but this is the
  // only way to make use of this class, which provides the features we need here.
  private static final LocaleDistance LOCALE_DISTANCE_INSTANCE = LocaleDistance.INSTANCE;

  // LocaleDistance best distance method arguments, all assigned to their default as per icu
  // implementation.
  private static final int LOCALE_DISTANCE_SHIFTED =
      LocaleDistance.shiftDistance(LOCALE_DISTANCE_INSTANCE.getDefaultScriptDistance());
  private static final int LOCALE_DISTANCE_SUPPORTED_LSRS_LENGTH = 1;
  private static final FavorSubtag LOCALE_DISTANCE_FAVOR_SUBTAG = FavorSubtag.LANGUAGE;
  private static final Direction LOCALE_DISTANCE_DIRECTION = Direction.WITH_ONE_WAY;

  // LikelySubtags.INSTANCE is commented as VisibleForTesting, so not ideal ... but this is the
  // only way to make use of this class, which provides the features we need here.
  private static final LikelySubtags LIKELY_SUBTAGS_INSTANCE = LikelySubtags.INSTANCE;

  // LikelySubtags method arguments, all assigned to their default as per icu implementation.
  private static final boolean LIKELY_SUBTAGS_RETURNS_INPUT_IF_UNMATCH = false;

  // Distance threshold: Anything above this value will be scored 0.
  private static final double DISTANCE_THRESHOLD = 224.0;

  // Score to affinity thresholds
  private static final int SCORE_THRESHOLD_MUTUALLY_INTELLIGIBLE = 65;
  private static final int SCORE_THRESHOLD_HIGH = 30;
  private static final int SCORE_THRESHOLD_LOW = 0;

  // Language codes for which we need some manual tweaks
  private static final String LANGUAGE_CODE_CROATIAN = "hr";
  private static final String LANGUAGE_CODE_BOSNIAN = "bs";

  /**
   * Returns the set of {@link ULocale} against which affinity is being calculated.
   *
   * @return set of locales
   */
  public abstract Set<ULocale> againstLocales();

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
                againstLocales().stream()
                    .map(ULocale::toLanguageTag)
                    .map(LanguageUtils::getSpokenLanguageLocale)
                    .flatMap(Optional::stream)
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
        .map(LocaleAffinityCalculatorBaseImpl::getMaximizedLanguageScriptRegion)
        .map(
            maxParsed ->
                againstLocales().stream()
                    .map(LocaleAffinityCalculatorBaseImpl::getMaximizedLanguageScriptRegion)
                    .map(
                        maxSupported ->
                            getDistanceBetweenInputAndSupported(maxParsed, maxSupported))
                    .map(Math::abs)
                    .min(Integer::compare)
                    .orElse(Integer.MAX_VALUE))
        .orElse(Integer.MAX_VALUE);
  }

  private int convertDistanceToAffinityScore(final int distance) {
    if (distance > DISTANCE_THRESHOLD) {
      return 0;
    } else {
      return (int) ((DISTANCE_THRESHOLD - distance) / DISTANCE_THRESHOLD * 100.0);
    }
  }

  private LocaleAffinity convertScoreToLocaleAffinity(final int score) {
    if (score > SCORE_THRESHOLD_MUTUALLY_INTELLIGIBLE) {
      return LocaleAffinity.MUTUALLY_INTELLIGIBLE;
    } else if (score > SCORE_THRESHOLD_HIGH) {
      return LocaleAffinity.HIGH;
    } else if (score > SCORE_THRESHOLD_LOW) {
      return LocaleAffinity.LOW;
    } else {
      return LocaleAffinity.NONE;
    }
  }

  private int getDistanceBetweenInputAndSupported(final LSR maxParsed, final LSR maxSupported) {
    // Croatian should be matched with Bosnian. This is the case for Bosnian written in Latin
    // script, but not Cyrillic, because the ICU implementation enforces script matching. We
    // created a workaround to ensure that we return a MUTUALLY_INTELLIGIBLE affinity when
    // encountering this locale.
    if (calculatingDistanceBetweenCroatianAndBosnian(maxParsed, maxSupported)) {
      return 0;
    }
    return LOCALE_DISTANCE_INSTANCE.getBestIndexAndDistance(
        maxParsed,
        new LSR[] {maxSupported},
        LOCALE_DISTANCE_SUPPORTED_LSRS_LENGTH,
        LOCALE_DISTANCE_SHIFTED,
        LOCALE_DISTANCE_FAVOR_SUBTAG,
        LOCALE_DISTANCE_DIRECTION);
  }

  private boolean calculatingDistanceBetweenCroatianAndBosnian(final LSR lsr1, final LSR lsr2) {
    return (lsr1.language.equals(LANGUAGE_CODE_CROATIAN)
            && lsr2.language.equals(LANGUAGE_CODE_BOSNIAN))
        || (lsr1.language.equals(LANGUAGE_CODE_BOSNIAN)
            && lsr2.language.equals(LANGUAGE_CODE_CROATIAN));
  }

  private static LSR getMaximizedLanguageScriptRegion(final ULocale locale) {
    return LIKELY_SUBTAGS_INSTANCE.makeMaximizedLsrFrom(
        locale, LIKELY_SUBTAGS_RETURNS_INPUT_IF_UNMATCH);
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

    abstract LocaleAffinityCalculatorBaseImpl autoBuild();

    /** Builds a {@link LocaleAffinityCalculator} out of this builder. */
    public final LocaleAffinityCalculator build() {
      final LocaleAffinityCalculatorBaseImpl built = autoBuild();
      for (ULocale locale : built.againstLocales()) {
        Preconditions.checkState(
            !isRootLocale(locale),
            "The locales against which affinity needs to be calculated cannot contain the root.");
      }
      return built;
    }
  }
}
