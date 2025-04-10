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

package com.spotify.i18n.locales.common.model;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.utils.available.AvailableLocalesUtils;
import com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A model class that represents a resolved locale. It contains the resolved supported locale for
 * translations, along with the resolved locale for formatting.
 *
 * <p>By "supported locale", we mean that this is a locale that identifies a language for which we
 * can supply translations.
 *
 * <p>This class is not intended for public subclassing. New object instances must be created using
 * the builder pattern, starting with the {@link #builder()} method.
 *
 * @see ULocale
 * @author Eric Fjøsne
 */
@AutoValue
public abstract class ResolvedLocale {

  /**
   * Returns a {@link ResolvedLocale} instance for the specified IETF BCP 47 language tag strings.
   *
   * @param languageTagForTranslations the resolved language tag for translations
   * @param languageTagForFormatting the resolved language tag for formatting
   * @return The corresponding resolved locale
   * @throws NullPointerException if either language tags is <code>null</code>
   * @throws IllegalStateException if either language tags could not be matched with a supported
   *     locale from CLDR
   */
  public static ResolvedLocale fromLanguageTags(
      final String languageTagForTranslations, final String languageTagForFormatting) {
    return fromLanguageTags(
        languageTagForTranslations, Collections.emptyList(), languageTagForFormatting);
  }

  /**
   * Returns a {@link ResolvedLocale} instance for the specified IETF BCP 47 language tag strings.
   *
   * @param languageTagForTranslations the resolved language tag for translations
   * @param languageTagForTranslationsFallbacks the resolved fallback language tags for translations
   * @param languageTagForFormatting the resolved language tag for formatting
   * @return The corresponding resolved locale
   * @throws NullPointerException if either language tags is <code>null</code>
   * @throws IllegalStateException if either language tags could not be matched with a supported
   *     locale from CLDR
   */
  public static ResolvedLocale fromLanguageTags(
      final String languageTagForTranslations,
      final List<String> languageTagForTranslationsFallbacks,
      final String languageTagForFormatting) {
    Preconditions.checkNotNull(languageTagForTranslations, "Given input cannot be null");
    Preconditions.checkNotNull(languageTagForTranslationsFallbacks, "Given input cannot be null");
    Preconditions.checkNotNull(languageTagForFormatting, "Given input cannot be null");

    ULocale localeForTranslations = ULocale.forLanguageTag(languageTagForTranslations);
    ULocale localeForFormatting = ULocale.forLanguageTag(languageTagForFormatting);
    List<ULocale> localeForTranslationsFallbacks =
        languageTagForTranslationsFallbacks.stream()
            .map(ULocale::forLanguageTag)
            .collect(Collectors.toList());

    Preconditions.checkState(
        AvailableLocalesUtils.getCldrLocales().contains(localeForTranslations),
        "Given parameter languageTagForTranslations could not be matched with a locale available in CLDR: %s",
        languageTagForTranslations);
    Preconditions.checkState(
        localeForTranslationsFallbacks.stream()
            .allMatch(AvailableLocalesUtils.getCldrLocales()::contains),
        "Given parameter languageTagForTranslationsFallbacks contains entries that could not be matched with a locale available in CLDR: %s",
        languageTagForTranslationsFallbacks);
    Preconditions.checkState(
        AvailableLocalesUtils.getCldrLocales().contains(localeForFormatting),
        "Given parameter languageTagForFormatting could not be matched with a locale available in CLDR: %s",
        languageTagForFormatting);

    return ResolvedLocale.builder()
        .localeForTranslations(localeForTranslations)
        .localeForTranslationsFallbacks(localeForTranslationsFallbacks)
        .localeForFormatting(localeForFormatting)
        .build();
  }

  /**
   * Returns the locale identifying the language in which translations should be served.
   *
   * @return best matching locale for translations
   */
  public abstract ULocale localeForTranslations();

  /**
   * Returns the ordered list of fallback locales identifying languages in which translations can
   * alternatively be served, when translations are not readily available for the {@link
   * #localeForTranslations()}.
   *
   * @return list of fallback locales for translations, compatible for usage with the {@link
   *     #localeForTranslations()}
   */
  public abstract List<ULocale> localeForTranslationsFallbacks();

  /**
   * Returns the locale to be used to apply formatting on Strings. It can be any descendant of the
   * highest non-ROOT ancestor locale of the {@link #localeForTranslations()}, according to the CLDR
   * locales hierarchy.
   *
   * @return best matching locale for formatting, compatible for usage with the {@link
   *     #localeForTranslations()}
   */
  public abstract ULocale localeForFormatting();

  /**
   * Returns a {@link Builder} instance that will allow you to manually create a {@link
   * ResolvedLocale} instance.
   *
   * @return The builder
   */
  public static Builder builder() {
    return new AutoValue_ResolvedLocale.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    Builder() { // package private constructor
      localeForTranslationsFallbacks(Collections.emptyList());
    }

    /**
     * Configures the locale identifying the language in which translations should be served.
     *
     * @param localeForTranslations best matching locale for translations
     * @return The {@link Builder} instance
     */
    public abstract Builder localeForTranslations(final ULocale localeForTranslations);

    /**
     * Configures the ordered list of fallback locales identifying languages in which translations
     * can alternatively be served, when translations are not readily available for the {@link
     * #localeForTranslations()}.
     *
     * @param localeForTranslationsFallbacks list of fallback locales for translations, compatible
     *     for usage with the {@link #localeForTranslations()}
     * @return The {@link Builder} instance
     */
    public abstract Builder localeForTranslationsFallbacks(
        final List<ULocale> localeForTranslationsFallbacks);

    /**
     * Configures the locale to be used to apply formatting on Strings. It can be any descendant of
     * the highest non-ROOT ancestor locale of the {@link #localeForTranslations()}, according to
     * the CLDR locales hierarchy.
     *
     * @param localeForFormatting best matching locale for formatting, compatible for usage with the
     *     {@link #localeForTranslations()}
     * @return The {@link Builder} instance
     */
    public abstract Builder localeForFormatting(final ULocale localeForFormatting);

    abstract ResolvedLocale autoBuild(); // not public

    /**
     * Builds a {@link ResolvedLocale} out of this builder.
     *
     * <ul>
     *   <li>All provided locales will be validated as normalized and canonicalized.
     *   <li>Their availability in CLDR will be checked and enforced
     *   <li>Locales hierarchy will be validated and enforced for all
     * </ul>
     *
     * <p>This is safe to be called several times on the same builder.
     *
     * @throws IllegalStateException if any of the builder property does not match the requirements.
     */
    public final ResolvedLocale build() {
      ResolvedLocale resolvedLocale = autoBuild();
      validateLocaleForTranslations(resolvedLocale);
      validateLocaleForTranslationsFallbacks(resolvedLocale);
      validateLocaleForFormatting(resolvedLocale);
      return resolvedLocale;
    }

    private static void validateLocaleForTranslations(ResolvedLocale resolvedLocale) {
      Preconditions.checkState(
          !resolvedLocale.localeForTranslations().equals(ULocale.ROOT),
          "The given localeForTranslations cannot be the root.");

      Preconditions.checkState(
          AvailableLocalesUtils.getCldrLocales().contains(resolvedLocale.localeForTranslations()),
          "The given localeForTranslations %s must be canonical and available in CLDR.",
          resolvedLocale.localeForTranslations().toLanguageTag());
    }

    private static void validateLocaleForTranslationsFallbacks(ResolvedLocale resolvedLocale) {
      if (resolvedLocale.localeForTranslationsFallbacks().isEmpty()) {
        return;
      }

      Preconditions.checkState(
          !resolvedLocale.localeForTranslationsFallbacks().stream()
              .anyMatch(locale -> LocalesHierarchyUtils.isSameLocale(locale, ULocale.ROOT)),
          "The given fallbackLocalesForTranslations cannot contain the root.");

      Preconditions.checkState(
          !resolvedLocale.localeForTranslationsFallbacks().stream()
              .anyMatch(
                  locale ->
                      LocalesHierarchyUtils.isSameLocale(
                          locale, resolvedLocale.localeForTranslations())),
          "The given fallbackLocalesForTranslations cannot contain the localeForTranslations [%s].",
          resolvedLocale.localeForTranslations().toLanguageTag());

      List<ULocale> nonCldrFallbackLocalesForTranslations =
          resolvedLocale.localeForTranslationsFallbacks().stream()
              .filter(Predicate.not(AvailableLocalesUtils.getCldrLocales()::contains))
              .collect(Collectors.toList());

      Preconditions.checkState(
          nonCldrFallbackLocalesForTranslations.isEmpty(),
          "The given fallbackLocalesForTranslations %s must be canonical and available in CLDR.",
          nonCldrFallbackLocalesForTranslations.stream()
              .map(ULocale::toLanguageTag)
              .sorted()
              .collect(Collectors.joining(",")));

      ULocale localeForTranslationsHighestAncestor =
          LocalesHierarchyUtils.getHighestAncestorLocale(resolvedLocale.localeForTranslations());

      List<ULocale> incompatibleFallbackLocalesForTranslations =
          resolvedLocale.localeForTranslationsFallbacks().stream()
              .filter(
                  locale ->
                      !LocalesHierarchyUtils.isSameLocale(
                          localeForTranslationsHighestAncestor,
                          LocalesHierarchyUtils.getHighestAncestorLocale(locale)))
              .collect(Collectors.toList());

      Preconditions.checkState(
          incompatibleFallbackLocalesForTranslations.isEmpty(),
          "The given fallbackLocaleForTranslations [%s] are not compatible fallbacks for the localeForTranslations [%s].",
          incompatibleFallbackLocalesForTranslations.stream()
              .map(ULocale::toLanguageTag)
              .sorted()
              .collect(Collectors.joining(",")),
          resolvedLocale.localeForTranslations().toLanguageTag());
    }

    private static void validateLocaleForFormatting(ResolvedLocale resolvedLocale) {
      Preconditions.checkState(
          !resolvedLocale.localeForFormatting().equals(ULocale.ROOT),
          "The given localeForFormatting cannot be the root.");

      Preconditions.checkState(
          AvailableLocalesUtils.getCldrLocales().contains(resolvedLocale.localeForFormatting()),
          "The given localeForFormatting %s must be canonical and available in CLDR.",
          resolvedLocale.localeForFormatting().toLanguageTag());

      ULocale rootLocaleForFormatting =
          LocalesHierarchyUtils.getHighestAncestorLocale(resolvedLocale.localeForTranslations());
      Preconditions.checkState(
          LocalesHierarchyUtils.isSameLocale(
                  resolvedLocale.localeForFormatting(), rootLocaleForFormatting)
              || LocalesHierarchyUtils.isDescendantLocale(
                  resolvedLocale.localeForFormatting(), rootLocaleForFormatting),
          "The given localeForFormatting %s is not the same as, or a descendant of the localeForTranslations %s.",
          resolvedLocale.localeForFormatting().toLanguageTag(),
          rootLocaleForFormatting.toLanguageTag());
    }
  }
}
