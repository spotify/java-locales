/*-
 * -\-\-
 * locales-common
 * --
 * Copyright (C) 2016 - 2024 Spotify AB
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
import com.spotify.i18n.locales.common.SupportedLocalesSupplier;
import com.spotify.i18n.locales.common.impl.LocalesResolverBaseImpl.Builder;
import com.spotify.i18n.locales.common.model.SupportedLocale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Base Implementation of {@link SupportedLocalesSupplier} that asynchronously supplies a list of
 * statically defined {@link SupportedLocale}s.
 *
 * <p>This class is not intended for public subclassing. New object instances must be created using
 * the builder pattern, starting with the {@link #builder()} method.
 *
 * @author Eric Fjøsne
 */
@AutoValue
public abstract class SupportedLocalesSupplierBaseImpl implements SupportedLocalesSupplier {

  /**
   * Returns the {@link Set} of {@link SupportedLocale}s
   *
   * @return The supported locales
   */
  public abstract Set<SupportedLocale> supportedLocales();

  /**
   * Returns a new CompletionStage that, when this stage completes normally, returns the set of
   * {@link SupportedLocale}s
   *
   * @return the new CompletionStage that returns the supported locales
   */
  @Override
  public final CompletionStage<Set<SupportedLocale>> get() {
    return CompletableFuture.completedFuture(supportedLocales());
  }

  /**
   * Returns a {@link Builder} instance that will allow you to manually create a {@link
   * SupportedLocalesSupplierBaseImpl} instance.
   *
   * @return The builder
   */
  public static Builder builder() {
    return new AutoValue_SupportedLocalesSupplierBaseImpl.Builder();
  }

  /** A builder for a {@link SupportedLocalesSupplierBaseImpl}. */
  @AutoValue.Builder
  public abstract static class Builder {
    Builder() {} // package private constructor

    /**
     * Configures the set of {@link SupportedLocale}s
     *
     * @param supportedLocales The set of supported locales
     * @return The {@link Builder} instance
     */
    public abstract Builder supportedLocales(final Set<SupportedLocale> supportedLocales);

    abstract SupportedLocalesSupplierBaseImpl autoBuild(); // not public

    /**
     * Builds a {@link SupportedLocalesSupplier} out of this builder. It will validate that at least
     * one {@link SupportedLocale} is present.
     *
     * @throws IllegalStateException if the list of {@link #supportedLocales()} is empty.
     */
    public final SupportedLocalesSupplier build() {
      SupportedLocalesSupplierBaseImpl supplier = autoBuild();
      Preconditions.checkState(
          supplier.supportedLocales().size() >= 1, "There must be at least one supported locale.");
      return supplier;
    }
  }
}
