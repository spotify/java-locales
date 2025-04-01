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

package com.spotify.i18n.locales.common.model;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.spotify.i18n.locales.common.LocaleAffinityCalculator;

/**
 * A model class that represents a {@link LocaleAffinityCalculator} result.
 *
 * <p>This class is not intended for public subclassing. New object instances must be created using
 * the builder pattern, starting with the {@link #builder()} method.
 *
 * @author Eric Fjøsne
 */
@AutoValue
public abstract class LocaleAffinityResult {

  private static final int MAX_SCORE = 100;
  private static final int MIN_SCORE = 0;

  /**
   * Returns the calculated affinity score
   *
   * @return affinity score, with value within the range 0 to 100.
   */
  public abstract int affinityScore();

  /**
   * Returns a {@link Builder} instance that will allow you to manually create a {@link
   * LocaleAffinityResult} instance.
   *
   * @return The builder
   */
  public static Builder builder() {
    return new AutoValue_LocaleAffinityResult.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    Builder() {} // package private constructor

    public abstract Builder affinityScore(int affinityScore);

    abstract LocaleAffinityResult autoBuild(); // not public

    /**
     * Builds a {@link LocaleAffinityResult} out of this builder.
     *
     * <p>This is safe to be called several times on the same builder.
     *
     * @throws IllegalStateException if any of the builder property does not match the requirements.
     */
    public final LocaleAffinityResult build() {
      LocaleAffinityResult result = autoBuild();
      int score = result.affinityScore();
      Preconditions.checkState(
          score >= MIN_SCORE && score <= MAX_SCORE,
          String.format(
              "The affinity score must be between %d and %d. Provided: %d.",
              MIN_SCORE, MAX_SCORE, score));
      return result;
    }
  }
}
