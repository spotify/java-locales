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
import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.utils.available.AvailableLocalesUtils;

/**
 * A model class that represents a related reference locale.
 *
 * <p>This class is not intended for public subclassing. New object instances must be created using
 * the builder pattern, starting with the {@link #builder()} method.
 *
 * @author Eric Fjøsne
 */
@AutoValue
public abstract class RelatedReferenceLocale {

  /** Returns the reference locale */
  public abstract ULocale referenceLocale();

  /** Returns the calculated affinity for the reference locale */
  public abstract LocaleAffinity affinity();

  /**
   * Returns a {@link Builder} instance that will allow you to manually create a {@link
   * RelatedReferenceLocale} instance.
   *
   * @return The builder
   */
  public static Builder builder() {
    return new AutoValue_RelatedReferenceLocale.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    Builder() {} // package private constructor

    public abstract Builder affinity(final LocaleAffinity affinity);

    public abstract Builder referenceLocale(final ULocale locale);

    abstract RelatedReferenceLocale autoBuild(); // not public

    /**
     * Builds a {@link RelatedReferenceLocale} out of this builder.
     *
     * <p>This is safe to be called several times on the same builder.
     *
     * @throws IllegalStateException if any of the builder property does not match the requirements.
     */
    public final RelatedReferenceLocale build() {
      final RelatedReferenceLocale rrl = autoBuild();

      Preconditions.checkState(
          AvailableLocalesUtils.getReferenceLocales().contains(rrl.referenceLocale()),
          "Given parameter locale could not be matched with an available reference locale: %s",
          rrl.referenceLocale().toLanguageTag());

      return rrl;
    }
  }
}
