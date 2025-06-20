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

import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isSameLocale;
import static com.spotify.i18n.locales.utils.language.LanguageUtils.getSpokenLanguageLocale;
import static com.spotify.i18n.locales.utils.languagetag.LanguageTagUtils.parse;

import com.google.auto.value.AutoValue;
import com.ibm.icu.impl.locale.LSR;
import com.ibm.icu.impl.locale.LikelySubtags;
import com.ibm.icu.impl.locale.LocaleDistance;
import com.ibm.icu.util.LocaleMatcher.Direction;
import com.ibm.icu.util.LocaleMatcher.FavorSubtag;
import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.LocaleAffinityBiCalculator;
import com.spotify.i18n.locales.common.LocaleAffinityCalculator;
import com.spotify.i18n.locales.common.model.LocaleAffinity;
import com.spotify.i18n.locales.common.model.LocaleAffinityResult;
import com.spotify.i18n.locales.utils.available.AvailableLocalesUtils;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base implementation of {@link LocaleAffinityBiCalculator} that calculates the locale affinity
 * between two given language tags.
 *
 * <p>This class is not intended for public subclassing. New object instances must be created using
 * the builder pattern, starting with the {@link #builder()} method.
 *
 * @author Eric Fjøsne
 */
@AutoValue
public abstract class LocaleAffinityBiCalculatorBaseImpl implements LocaleAffinityBiCalculator {

  // Set containing all available language codes in CLDR.
  private static final Set<String> AVAILABLE_LANGUAGE_CODES =
      AvailableLocalesUtils.getReferenceLocales().stream()
          .map(ULocale::getLanguage)
          .collect(Collectors.toSet());

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
   * Returns the calculated {@link LocaleAffinityResult} for the given two language tags
   *
   * @return the locale affinity result
   */
  @Override
  public LocaleAffinityResult calculate(
      @Nullable final String languageTag1, @Nullable final String languageTag2) {
    return LocaleAffinityResult.builder().affinity(getAffinity(languageTag1, languageTag2)).build();
  }

  private LocaleAffinity getAffinity(
      @Nullable final String languageTag1, @Nullable final String languageTag2) {
    // We parse the language tags, and filter out locales with a language unavailable in CLDR.
    final Optional<ULocale> locale1 =
        parse(languageTag1).filter(locale -> isAvailableLanguage(locale));
    final Optional<ULocale> locale2 =
        parse(languageTag2).filter(locale -> isAvailableLanguage(locale));

    if (locale1.isPresent() && locale2.isPresent()) {
      // We attempt to match based on corresponding spoken language first, and make use of the
      // score-based affinity calculation as fallback.
      if (hasSameSpokenLanguageAffinity(locale1.get(), locale2.get())) {
        return LocaleAffinity.SAME;
      } else {
        return calculateScoreBasedAffinity(locale1.get(), locale2.get());
      }
    } else {
      return LocaleAffinity.NONE;
    }
  }

  private boolean hasSameSpokenLanguageAffinity(final ULocale locale1, final ULocale locale2) {
    final Optional<ULocale> spoken1 = getSpokenLanguageLocale(locale1.toLanguageTag());
    final Optional<ULocale> spoken2 = getSpokenLanguageLocale(locale2.toLanguageTag());
    return spoken1.isPresent() && spoken2.isPresent() && isSameLocale(spoken1.get(), spoken2.get());
  }

  static LocaleAffinity calculateScoreBasedAffinity(final ULocale l1, final ULocale l2) {
    int bestDistance = getBestDistanceBetweenLocales(l1, l2);
    int correspondingScore = convertDistanceToAffinityScore(bestDistance);
    return convertScoreToLocaleAffinity(correspondingScore);
  }

  static boolean isAvailableLanguage(final ULocale locale) {
    return AVAILABLE_LANGUAGE_CODES.contains(locale.getLanguage().toLowerCase());
  }

  private static int getBestDistanceBetweenLocales(final ULocale locale1, final ULocale locale2) {
    final LSR lsr1 = getMaximizedLanguageScriptRegion(locale1);
    final LSR lsr2 = getMaximizedLanguageScriptRegion(locale2);
    return getBestDistanceBetweenLSR(lsr1, lsr2);
  }

  static int getBestDistanceBetweenLSR(final LSR lsr1, final LSR lsr2) {
    // Croatian should be matched with Bosnian. This is the case for Bosnian written in Latin
    // script, but not Cyrillic, because the ICU implementation enforces script matching. We
    // created a workaround to ensure that we return a MUTUALLY_INTELLIGIBLE affinity when
    // encountering this locale.
    if (calculatingDistanceBetweenCroatianAndBosnian(lsr1, lsr2)) {
      return 0;
    } else {
      // We calculate distances both ways, and return the minimum value (= best distance).
      return Math.min(calculateDistance(lsr1, lsr2), calculateDistance(lsr2, lsr1));
    }
  }

  private static int calculateDistance(final LSR lsr1, final LSR lsr2) {
    return Math.abs(
        LOCALE_DISTANCE_INSTANCE.getBestIndexAndDistance(
            lsr1,
            new LSR[] {lsr2},
            LOCALE_DISTANCE_SUPPORTED_LSRS_LENGTH,
            LOCALE_DISTANCE_SHIFTED,
            LOCALE_DISTANCE_FAVOR_SUBTAG,
            LOCALE_DISTANCE_DIRECTION));
  }

  static int convertDistanceToAffinityScore(final int distance) {
    if (distance > DISTANCE_THRESHOLD) {
      return 0;
    } else {
      return (int) ((DISTANCE_THRESHOLD - distance) / DISTANCE_THRESHOLD * 100.0);
    }
  }

  static LocaleAffinity convertScoreToLocaleAffinity(final int score) {
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

  private static boolean calculatingDistanceBetweenCroatianAndBosnian(
      final LSR lsr1, final LSR lsr2) {
    return (lsr1.language.equals(LANGUAGE_CODE_CROATIAN)
            && lsr2.language.equals(LANGUAGE_CODE_BOSNIAN))
        || (lsr1.language.equals(LANGUAGE_CODE_BOSNIAN)
            && lsr2.language.equals(LANGUAGE_CODE_CROATIAN));
  }

  static LSR getMaximizedLanguageScriptRegion(final ULocale locale) {
    return LIKELY_SUBTAGS_INSTANCE.makeMaximizedLsrFrom(
        locale, LIKELY_SUBTAGS_RETURNS_INPUT_IF_UNMATCH);
  }

  /**
   * Returns a {@link Builder} instance that will allow you to manually create a {@link
   * LocaleAffinityBiCalculatorBaseImpl} instance.
   *
   * @return The builder
   */
  public static Builder builder() {
    return new AutoValue_LocaleAffinityBiCalculatorBaseImpl.Builder();
  }

  /** A builder for a {@link LocaleAffinityBiCalculatorBaseImpl}. */
  @AutoValue.Builder
  public abstract static class Builder {
    Builder() {} // package private constructor

    abstract LocaleAffinityBiCalculatorBaseImpl autoBuild();

    /** Builds a {@link LocaleAffinityCalculator} out of this builder. */
    public final LocaleAffinityBiCalculator build() {
      return autoBuild();
    }
  }
}
