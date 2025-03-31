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
import com.google.common.base.Preconditions;
import com.ibm.icu.impl.locale.LSR;
import com.ibm.icu.impl.locale.LikelySubtags;
import com.ibm.icu.impl.locale.LocaleDistance;
import com.ibm.icu.util.LocaleMatcher.Direction;
import com.ibm.icu.util.LocaleMatcher.FavorSubtag;
import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.LocalesMatcher;
import com.spotify.i18n.locales.common.model.LocalesMatcherResult;
import com.spotify.i18n.locales.utils.languagetag.LanguageTagUtils;
import java.util.Set;

/**
 * Base implementation of {@link LocalesMatcher} that attributes a matching score to a given input
 * value (language tag) against a set of supported locales.
 *
 * <p>This class is not intended for public subclassing. New object instances must be created using
 * the builder pattern, starting with the {@link #builder()} method.
 *
 * @author Eric Fjøsne
 */
@AutoValue
public abstract class LocalesMatcherBaseImpl implements LocalesMatcher {

  // Chosen max distance threshold. Anything beyond will be scored 0.
  private static final double MAX_DISTANCE_THRESHOLD = 224.0;

  // LocaleDistance.INSTANCE is commented as VisibleForTesting, so not ideal ... but this is the
  // only way to make use of this class, which contains all we need here.
  private static final LocaleDistance LOCALE_DISTANCE_INSTANCE = LocaleDistance.INSTANCE;

  // LocaleDistance best distance arguments, all assigned to their default as per icu implementation
  private static final int LOCALE_DISTANCE_SHIFTED =
      LocaleDistance.shiftDistance(LOCALE_DISTANCE_INSTANCE.getDefaultScriptDistance());
  private static final int LOCALE_DISTANCE_SUPPORTED_LSRS_LENGTH = 1;
  private static final FavorSubtag LOCALE_DISTANCE_FAVOR_SUBTAG = FavorSubtag.LANGUAGE;
  private static final Direction LOCALE_DISTANCE_DIRECTION = Direction.WITH_ONE_WAY;

  // LikelySubtags.INSTANCE is commented as VisibleForTesting, so not ideal ... but this is the
  // only way to make use of this class, which contains all we need here.
  private static final LikelySubtags LIKELY_SUBTAGS_INSTANCE = LikelySubtags.INSTANCE;

  // LikelySubtags method arguments, all assigned to their default as per icu implementation
  private static final boolean LIKELY_SUBTAGS_RETURNS_INPUT_IF_UNMATCH = false;

  public abstract Set<ULocale> supportedLocales();

  @Override
  public LocalesMatcherResult match(final String languageTag) {
    if (supportedLocales().isEmpty()) {
      return LocalesMatcherResult.builder().matchingScore(0).build();
    } else {
      return LocalesMatcherResult.builder()
          .matchingScore(convertDistanceToScore(getBestDistance(languageTag)))
          .build();
    }
  }

  private int convertDistanceToScore(final int distance) {
    if (distance > MAX_DISTANCE_THRESHOLD) {
      return 0;
    } else {
      return (int) ((MAX_DISTANCE_THRESHOLD - distance) / MAX_DISTANCE_THRESHOLD * 100.0);
    }
  }

  private int getBestDistance(final String languageTag) {
    return LanguageTagUtils.parse(languageTag)
        .map(LocalesMatcherBaseImpl::getMaximizedLanguageScriptRegion)
        .map(
            maxParsed ->
                supportedLocales().stream()
                    .map(LocalesMatcherBaseImpl::getMaximizedLanguageScriptRegion)
                    .map(
                        maxSupported ->
                            getDistanceBetweenInputAndSupported(maxParsed, maxSupported))
                    .map(Math::abs)
                    .min(Integer::compare)
                    .orElse(Integer.MAX_VALUE))
        .orElse(Integer.MAX_VALUE);
  }

  private static int getDistanceBetweenInputAndSupported(
      final LSR maxParsed, final LSR maxSupported) {
    return LOCALE_DISTANCE_INSTANCE.getBestIndexAndDistance(
        maxParsed,
        new LSR[] {maxSupported},
        LOCALE_DISTANCE_SUPPORTED_LSRS_LENGTH,
        LOCALE_DISTANCE_SHIFTED,
        LOCALE_DISTANCE_FAVOR_SUBTAG,
        LOCALE_DISTANCE_DIRECTION);
  }

  private static LSR getMaximizedLanguageScriptRegion(final ULocale locale) {
    return LIKELY_SUBTAGS_INSTANCE.makeMaximizedLsrFrom(
        locale, LIKELY_SUBTAGS_RETURNS_INPUT_IF_UNMATCH);
  }

  /**
   * Returns a {@link Builder} instance that will allow you to manually create a {@link
   * LocalesMatcherBaseImpl} instance.
   *
   * @return The builder
   */
  public static Builder builder() {
    return new AutoValue_LocalesMatcherBaseImpl.Builder();
  }

  /** A builder for a {@link LocalesMatcherBaseImpl}. */
  @AutoValue.Builder
  public abstract static class Builder {
    Builder() {} // package private constructor

    public abstract Builder supportedLocales(final Set<ULocale> baseLocales);

    abstract LocalesMatcherBaseImpl autoBuild();

    /** Builds a {@link LocalesMatcher} out of this builder. */
    public final LocalesMatcher build() {
      final LocalesMatcherBaseImpl built = autoBuild();
      for (ULocale baseLocale : built.supportedLocales()) {
        Preconditions.checkState(
            !baseLocale.equals(ULocale.ROOT), "The supported locales cannot contain the root.");
      }
      return built;
    }
  }
}
