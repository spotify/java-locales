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
import com.spotify.i18n.locales.common.LocalesResolver;
import com.spotify.i18n.locales.common.impl.LocalesResolverBaseImpl.Builder;
import com.spotify.i18n.locales.common.model.ResolvedLocale;
import com.spotify.i18n.locales.common.model.SupportedLocale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Base implementation of an asynchronous resolver of locale, for a given {@link CONTEXT}
 *
 * <p>This resolver will return the matching {@link ResolvedLocale}, with optional fallbacks, for a
 * given {@link CONTEXT}.
 *
 * <p>This class is not intended for public subclassing. New object instances must be created using
 * the builder pattern, starting with the {@link #builder()} method.
 *
 * @param <CONTEXT>
 * @see ResolvedLocale
 * @author Eric Fjøsne
 */
@AutoValue
public abstract class ContextBasedLocalesResolverBaseImpl<CONTEXT>
    implements ContextBasedLocalesResolver<CONTEXT> {

  /**
   * Returns the function that returns a completion stage that, when this stage completes normally,
   * returns the supported locales set for the given {@link CONTEXT}.
   *
   * @return the function as described above
   */
  public abstract Function<CONTEXT, CompletionStage<Set<SupportedLocale>>>
      contextToSupportedLocales();

  /**
   * Returns the function that returns a completion stage that, when this stage completes normally,
   * returns the optional {@code Accept-Language} value for the given {@link CONTEXT}, against which
   * locale resolution will be performed.
   *
   * @return the function as described above
   */
  public abstract Function<CONTEXT, CompletionStage<Optional<String>>> contextToAcceptLanguage();

  /**
   * Returns the function returns a completion stage that, when this stage completes normally,
   * returns the default {@link ResolvedLocale} for the given {@link CONTEXT}, which will be used as
   * default/fallback resolved locale when locale resolution fails for some reason.
   *
   * @return the function as described above
   */
  public abstract Function<CONTEXT, CompletionStage<ResolvedLocale>>
      contextToDefaultResolvedLocale();

  /**
   * Returns a {@link Builder} instance that will allow you to manually create a {@link
   * ContextBasedLocalesResolverBaseImpl} instance.
   *
   * @return The builder
   */
  public static Builder builder() {
    return new AutoValue_ContextBasedLocalesResolverBaseImpl.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder<CONTEXT> {
    Builder() {} // package private constructor

    /**
     * Configures the function that returns a completion stage that, when this stage completes
     * normally, returns the supported locales set for the given {@link CONTEXT}.
     *
     * @param contextToSupportedLocales the function as described above
     * @return The {@link Builder} instance
     */
    public abstract Builder<CONTEXT> contextToSupportedLocales(
        final Function<CONTEXT, CompletionStage<Set<SupportedLocale>>> contextToSupportedLocales);

    /**
     * Configures the function that returns a completion stage that, when this stage completes
     * normally, returns the optional {@code Accept-Language} value for the given {@link CONTEXT},
     * against which locale resolution will be performed.
     *
     * @param contextToAcceptLanguage the function as described above
     * @return The {@link Builder} instance
     */
    public abstract Builder<CONTEXT> contextToAcceptLanguage(
        final Function<CONTEXT, CompletionStage<Optional<String>>> contextToAcceptLanguage);

    /**
     * Configures the function returns a completion stage that, when this stage completes normally,
     * returns the default {@link ResolvedLocale} for the given {@link CONTEXT}, which will be used
     * as default/fallback resolved locale when locale resolution fails for some reason.
     *
     * @param contextToDefaultResolvedLocale the function as described above
     * @return The {@link Builder} instance
     */
    public abstract Builder<CONTEXT> contextToDefaultResolvedLocale(
        final Function<CONTEXT, CompletionStage<ResolvedLocale>> contextToDefaultResolvedLocale);

    abstract ContextBasedLocalesResolverBaseImpl<CONTEXT> autoBuild(); // not public

    /** Builds a {@link ContextBasedLocalesResolver<CONTEXT>} out of this builder. */
    public final ContextBasedLocalesResolver<CONTEXT> build() {
      return autoBuild();
    }
  }

  /**
   * Returns a new CompletionStage that, when this stage completes normally, returns the matching
   * {@link SupportedLocale}s for the given {@link CONTEXT}.
   *
   * @param ctx the {@link CONTEXT}
   * @return the new CompletionStage that returns the supported locales
   */
  public CompletionStage<ResolvedLocale> resolve(final CONTEXT ctx) {
    return contextToAcceptLanguage()
        .apply(ctx)
        .thenCompose(
            acceptLanguageOpt ->
                acceptLanguageOpt
                    .map(resolveLocaleForAcceptLanguage(ctx))
                    .orElse(contextToDefaultResolvedLocale().apply(ctx)));
  }

  private Function<String, CompletionStage<ResolvedLocale>> resolveLocaleForAcceptLanguage(
      CONTEXT ctx) {
    return acceptLanguage ->
        contextToSupportedLocales()
            .apply(ctx)
            .thenCombine(contextToDefaultResolvedLocale().apply(ctx), getLocaleResolver())
            .thenApply(localesResolver -> localesResolver.resolve(acceptLanguage));
  }

  private BiFunction<Set<SupportedLocale>, ResolvedLocale, LocalesResolver> getLocaleResolver() {
    return (supportedLocales, defaultResolvedLocale) ->
        LocalesResolverBaseImpl.builder()
            .defaultResolvedLocale(defaultResolvedLocale)
            .supportedLocales(supportedLocales)
            .build();
  }
}
