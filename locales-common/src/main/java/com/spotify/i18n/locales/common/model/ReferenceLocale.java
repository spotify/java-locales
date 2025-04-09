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

import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isSameLocale;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.ibm.icu.util.ULocale;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A model class that represents a {@link ReferenceLocale}.
 *
 * <p>This class is not intended for public subclassing. New object instances must be created using
 * the builder pattern, starting with the {@link #builder()} method.
 *
 * @author Eric Fjøsne
 */
@AutoValue
public abstract class ReferenceLocale {

  public static final ULocale EN_US_POSIX = ULocale.forLanguageTag("en-US-POSIX");

  /**
   * Set containing all reference locales, which are all CLDR available {@link ULocale} without
   * duplicate entries, and without en-us-POSIX.
   */
  private static final Set<ULocale> REFERENCE_LOCALES =
      Arrays.stream(ULocale.getAvailableLocales())
          .filter(l -> !isSameLocale(l, ULocale.ROOT) && !isSameLocale(l, EN_US_POSIX))
          .map(ULocale::minimizeSubtags)
          .collect(Collectors.toUnmodifiableSet());

  /** Returns all available reference locales */
  public static Set<ULocale> availableReferenceLocales() {
    return REFERENCE_LOCALES;
  }

  /** Returns the reference locale */
  public abstract ULocale locale();

  /** Returns the calculated affinity for the reference locale */
  public abstract LocaleAffinity affinity();

  /**
   * Returns a {@link Builder} instance that will allow you to manually create a {@link
   * ReferenceLocale} instance.
   *
   * @return The builder
   */
  public static Builder builder() {
    return new AutoValue_ReferenceLocale.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    Builder() {} // package private constructor

    public abstract Builder affinity(final LocaleAffinity affinity);

    public abstract Builder locale(final ULocale locale);

    abstract ReferenceLocale autoBuild(); // not public

    /**
     * Builds a {@link ReferenceLocale} out of this builder.
     *
     * <p>This is safe to be called several times on the same builder.
     *
     * @throws IllegalStateException if any of the builder property does not match the requirements.
     */
    public final ReferenceLocale build() {
      final ReferenceLocale mrl = autoBuild();

      Preconditions.checkState(
          REFERENCE_LOCALES.contains(mrl.locale()),
          "Given parameter locale could not be matched with an available reference locale: %s",
          mrl.locale().toLanguageTag());

      return mrl;
    }
  }
}
