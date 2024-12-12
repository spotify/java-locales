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
import com.spotify.i18n.locales.common.ContextBasedSupportedLocalesSupplier;
import com.spotify.i18n.locales.common.SupportedLocalesSupplier;
import com.spotify.i18n.locales.common.impl.LocalesResolverBaseImpl.Builder;
import com.spotify.i18n.locales.common.model.SupportedLocale;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * Base implementation of an asynchronous supplier of {@link SupportedLocale}s, for a given {@link
 * CONTEXT}, considering that a specific {@link CONTEXT} can always be mapped to a supported locales
 * target {@link AUDIENCE}
 *
 * <p>This class is not intended for public subclassing. New object instances must be created using
 * the builder pattern, starting with the {@link #builder()} method.
 *
 * @author Eric Fjøsne
 * @param <CONTEXT>
 * @param <AUDIENCE>
 */
@AutoValue
public abstract class ContextBasedSupportedLocalesSupplierBaseImpl<CONTEXT, AUDIENCE>
    implements ContextBasedSupportedLocalesSupplier<CONTEXT> {

  /**
   * Returns the function that returns the {@link SupportedLocalesSupplier} corresponding to a given
   * {@link AUDIENCE}
   *
   * @return the function as described above
   */
  public abstract Function<AUDIENCE, SupportedLocalesSupplier> audienceToSupplier();

  /**
   * Returns the function that returns a completion stage that, when this stage completes normally,
   * returns the supported locales target {@link AUDIENCE} for the given {@link CONTEXT}.
   *
   * @return the function as described above
   */
  public abstract Function<CONTEXT, CompletionStage<AUDIENCE>> contextToAudience();

  /**
   * Returns a {@link Builder} instance that will allow you to manually create a {@link
   * ContextBasedSupportedLocalesSupplierBaseImpl} instance.
   *
   * @return The builder
   */
  public static Builder builder() {
    return new AutoValue_ContextBasedSupportedLocalesSupplierBaseImpl.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder<CONTEXT, AUDIENCE> {
    Builder() {} // package private constructor

    /**
     * Configures the function that returns the {@link SupportedLocalesSupplier} corresponding to a
     * given {@link AUDIENCE}
     *
     * @param audienceToSupplier the function as described above
     * @return The {@link Builder} instance
     */
    public abstract Builder<CONTEXT, AUDIENCE> audienceToSupplier(
        final Function<AUDIENCE, SupportedLocalesSupplier> audienceToSupplier);

    /**
     * Configures the function that returns a completion stage that, when this stage completes
     * normally, returns the supported locales target {@link AUDIENCE} for the given {@link
     * CONTEXT}.
     *
     * @param contextToAudienceFunction the function as described above
     * @return The {@link Builder} instance
     */
    public abstract Builder<CONTEXT, AUDIENCE> contextToAudience(
        final Function<CONTEXT, CompletionStage<AUDIENCE>> contextToAudienceFunction);

    abstract ContextBasedSupportedLocalesSupplierBaseImpl<CONTEXT, AUDIENCE>
        autoBuild(); // not public

    /** Builds a {@link ContextBasedSupportedLocalesSupplier<CONTEXT>} out of this builder. */
    public final ContextBasedSupportedLocalesSupplier<CONTEXT> build() {
      return autoBuild();
    }
  }

  /**
   * Returns a new CompletionStage that, when this stage completes normally, returns the matching
   * {@link SupportedLocale}s for the appropriate {@link AUDIENCE}, which gets inferred from the
   * given {@link CONTEXT}.
   *
   * @param ctx the {@link CONTEXT}
   * @return the new CompletionStage that returns the set of supported locales
   */
  public CompletionStage<Set<SupportedLocale>> get(final CONTEXT ctx) {
    return contextToAudience()
        .apply(ctx)
        .thenApply(audienceToSupplier())
        .thenCompose(SupportedLocalesSupplier::get);
  }
}
