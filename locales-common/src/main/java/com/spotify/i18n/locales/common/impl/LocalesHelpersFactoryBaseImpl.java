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
import com.spotify.i18n.locales.common.ContextBasedLocalesResolver;
import com.spotify.i18n.locales.common.ContextBasedSupportedLocalesSupplier;
import com.spotify.i18n.locales.common.LocalesHelpersFactory;
import com.spotify.i18n.locales.common.SupportedLocalesSupplier;
import com.spotify.i18n.locales.common.impl.ContextBasedLocalesResolverBaseImpl.Builder;
import com.spotify.i18n.locales.common.model.ResolvedLocale;
import com.spotify.i18n.locales.common.model.SupportedLocale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * Base implementation of a {@link LocalesHelpersFactory}. It features a segmentation of end-users
 * based what is referred to as an {@link AUDIENCE}, which can be derived from a given {@link
 * CONTEXT} in order to limit exposing selected supported locales to certain users only.
 *
 * <p>This class is not intended for public subclassing. New object instances must be created using
 * the builder pattern, starting with the {@link #builder()} method.
 *
 * @param <CONTEXT> the type of the context based on which helpers created by this factory will
 *     operate.
 * @param <AUDIENCE> the type which defines the different target user audiences.
 * @author Eric Fjøsne
 */
@AutoValue
public abstract class LocalesHelpersFactoryBaseImpl<CONTEXT, AUDIENCE>
    implements LocalesHelpersFactory<CONTEXT> {

  /**
   * Returns the function that returns a new CompletionStage that, when this stage completes
   * normally, returns the target audience for the given {@link CONTEXT}
   *
   * @return the function as described above
   */
  public abstract Function<CONTEXT, CompletionStage<AUDIENCE>> contextToAudience();

  /**
   * Returns the function that returns the {@link SupportedLocalesSupplier} for the given {@link
   * AUDIENCE}.
   *
   * @return the function as described above
   */
  public abstract Function<AUDIENCE, SupportedLocalesSupplier> audienceToSupportedLocalesSupplier();

  /**
   * Returns the function that returns a new CompletionStage that, when this stage completes
   * normally, returns the optional {@code Accept-Language} value for the {@link CONTEXT}.
   *
   * @return the function as described above
   */
  public abstract Function<CONTEXT, CompletionStage<Optional<String>>> contextToAcceptLanguage();

  /**
   * Returns the function that returns a new CompletionStage that, when this stage completes
   * normally, returns the default {@link ResolvedLocale}, for a given {@link CONTEXT}.
   *
   * <p>This {@link ResolvedLocale} should be considered as the default/fallback resolved locale, in
   * case locale resolution fails to find a matching supported locale for that given {@link
   * CONTEXT}.
   *
   * <p>A base implementation of this function could be to map any {@link CONTEXT} to a unique
   * {@link ResolvedLocale} matching your default application language.
   *
   * @return the function as described above
   */
  public abstract Function<CONTEXT, CompletionStage<ResolvedLocale>>
      contextToDefaultResolvedLocale();

  /**
   * Returns a {@link Builder} instance that will allow you to manually create a {@link
   * LocalesHelpersFactoryBaseImpl} instance.
   *
   * @return The builder
   */
  public static Builder builder() {
    return new AutoValue_LocalesHelpersFactoryBaseImpl.Builder();
  }

  /**
   * Returns a preconfigured, ready-to-use instance of {@link ContextBasedSupportedLocalesSupplier}.
   *
   * <p>This supplier will return the matching {@link SupportedLocale}s, for the {@link AUDIENCE}
   * which gets inferred from the given {@link CONTEXT}.
   *
   * @return Pre configured supported locales supplier
   */
  public ContextBasedSupportedLocalesSupplier<CONTEXT> buildSupportedLocalesSupplier() {
    return ContextBasedSupportedLocalesSupplierBaseImpl.builder()
        .contextToAudience(contextToAudience())
        .audienceToSupplier(audienceToSupportedLocalesSupplier())
        .build();
  }

  /**
   * Returns a preconfigured, ready-to-use instance of {@link ContextBasedLocalesResolver}.
   *
   * <p>This resolver will return the matching {@link ResolvedLocale}, for a given {@link CONTEXT}.
   *
   * @return Pre configured locales resolver
   */
  @Override
  public ContextBasedLocalesResolver<CONTEXT> buildLocalesResolver() {
    // Build the supported locales supplier
    final ContextBasedSupportedLocalesSupplier<CONTEXT> supportedLocalesSupplier =
        buildSupportedLocalesSupplier();

    // Create a function that returns the supported locales set for a given {@link CONTEXT}, by
    // leveraging the supported locales supplier built instance
    final Function<CONTEXT, CompletionStage<Set<SupportedLocale>>> contextToSupportedLocales =
        (ctx) -> supportedLocalesSupplier.get(ctx);

    // Return the pre-configured context based locales resolver
    return ContextBasedLocalesResolverBaseImpl.builder()
        .contextToSupportedLocales(contextToSupportedLocales)
        .contextToAcceptLanguage(contextToAcceptLanguage())
        .contextToDefaultResolvedLocale(contextToDefaultResolvedLocale())
        .build();
  }

  @AutoValue.Builder
  public abstract static class Builder<CONTEXT, AUDIENCE> {
    Builder() {} // package private constructor

    /**
     * Configures the function that returns a new CompletionStage that, when this stage completes
     * normally, returns the target audience for the given {@link CONTEXT}
     *
     * @param contextToAudience the function as described above
     * @return The {@link Builder} instance
     */
    public abstract Builder contextToAudience(
        final Function<CONTEXT, CompletionStage<AUDIENCE>> contextToAudience);

    /**
     * Configures the function that returns the {@link SupportedLocalesSupplier} for the given
     * {@link AUDIENCE}.
     *
     * @param audienceToSupportedLocalesSupplier the function as described above
     * @return The {@link Builder} instance
     */
    public abstract Builder audienceToSupportedLocalesSupplier(
        final Function<AUDIENCE, SupportedLocalesSupplier> audienceToSupportedLocalesSupplier);

    /**
     * Configures the function that returns a new CompletionStage that, when this stage completes
     * normally, returns the optional {@code Accept-Language} value for the {@link CONTEXT}.
     *
     * @param contextToAcceptLanguage the function as described above
     * @return The {@link Builder} instance
     */
    public abstract Builder contextToAcceptLanguage(
        final Function<CONTEXT, CompletionStage<Optional<String>>> contextToAcceptLanguage);

    /**
     * Configures the function that returns a new CompletionStage that, when this stage completes
     * normally, returns the default {@link ResolvedLocale}, for a given {@link CONTEXT}.
     *
     * <p>This {@link ResolvedLocale} should be considered as the default/fallback resolved locale,
     * in case locale resolution fails to find a matching supported locale for that given {@link
     * CONTEXT}.
     *
     * <p>A base implementation of this function could be to map any {@link CONTEXT} to a unique
     * {@link ResolvedLocale} matching your default application language.
     *
     * @param contextToDefaultResolvedLocale the function as described above
     * @return The {@link Builder} instance
     */
    public abstract Builder contextToDefaultResolvedLocale(
        final Function<CONTEXT, CompletionStage<ResolvedLocale>> contextToDefaultResolvedLocale);

    abstract LocalesHelpersFactoryBaseImpl<CONTEXT, AUDIENCE> autoBuild();

    /**
     * Returns a built {@link LocalesHelpersFactory<CONTEXT>} based on the builder properties.
     *
     * @return built locales helpers factory
     */
    public final LocalesHelpersFactory<CONTEXT> build() {
      return autoBuild();
    }
  }
}
