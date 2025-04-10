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
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A model class that contains a supported locale for translations, along with a list of related
 * acceptable locales that can be used to apply transformations on Strings (formatting of numbers,
 * dates, ...). All enclosed locales are:
 *
 * <ul>
 *   <li>in their normalized and canonical form
 *   <li>available in CLDR
 * </ul>
 *
 * <p>By "supported locale", we mean that this is a locale that identifies a language for which we
 * can supply translations.
 *
 * <p>The static helper methods {@link #fromLanguageTag(String)}, {@link #fromLocale(Locale)} and
 * {@link #fromULocale(ULocale)} do not perform locale resolution, normalization or
 * canonicalization. All given input are strictly validated and must represent supported locales
 * available in CLDR. The resulting {@link SupportedLocale} of these methods will contain:
 *
 * <ul>
 *   <li>the <code>localeForTranslations</code> that best represents the given input, according to
 *       ICU.
 *   <li>all children locales of this locale (including itself) as <code>relatedLocalesForFormatting
 *       </code>.
 * </ul>
 *
 * <p>This class is not intended for public subclassing. New object instances must be created using
 * the builder pattern, starting with the {@link #builder()} method.
 *
 * @see ULocale
 * @author Eric Fjøsne
 */
@AutoValue
public abstract class SupportedLocale {

  /**
   * Returns a {@link SupportedLocale} instance for the specified IETF BCP 47 language tag string.
   *
   * @param languageTag the language tag
   * @return The supported locale that best represents the given language tag.
   * @throws NullPointerException if <code>languageTag</code> is <code>null</code>
   * @throws IllegalStateException if <code>languageTag</code> could not be matched with a supported
   *     locale
   */
  public static SupportedLocale fromLanguageTag(final String languageTag) {
    Preconditions.checkNotNull(languageTag, "Given input cannot be null");
    ULocale locale = ULocale.forLanguageTag(languageTag);
    Preconditions.checkState(
        Objects.nonNull(locale) && AvailableLocalesUtils.getCldrLocales().contains(locale),
        "Given parameter languageTag could not be matched with a locale available in CLDR: %s",
        languageTag);
    return fromValidatedULocale(locale);
  }

  /**
   * Returns a {@link SupportedLocale} instance for the specified {@link Locale}.
   *
   * @param locale the locale
   * @return The supported locale that best represents the given language tag.
   * @throws NullPointerException if <code>locale</code> is <code>null</code>
   * @throws IllegalStateException if <code>locale</code> could not be matched with a supported
   *     locale
   * @see Locale
   */
  public static SupportedLocale fromLocale(final Locale locale) {
    Preconditions.checkNotNull(locale, "Given input cannot be null");
    ULocale uLocale = ULocale.forLocale(locale);
    Preconditions.checkState(
        Objects.nonNull(uLocale) && AvailableLocalesUtils.getCldrLocales().contains(uLocale),
        "Given parameter locale could not be matched with a locale available in CLDR: %s",
        locale);
    return fromValidatedULocale(uLocale);
  }

  /**
   * Returns a {@link SupportedLocale} instance for the specified {@link ULocale}.
   *
   * @param uLocale the uLocale
   * @return The supported locale that best represents the given language tag.
   * @throws NullPointerException if <code>uLocale</code> is <code>null</code>
   * @throws IllegalStateException if <code>uLocale</code> could not be matched with a supported
   *     locale
   * @see ULocale
   */
  public static SupportedLocale fromULocale(final ULocale uLocale) {
    Preconditions.checkNotNull(uLocale, "Given input cannot be null");
    Preconditions.checkState(
        AvailableLocalesUtils.getCldrLocales().contains(uLocale),
        "Given parameter uLocale could not be matched with a locale available in CLDR: %s",
        uLocale);
    return fromValidatedULocale(uLocale);
  }

  private static SupportedLocale fromValidatedULocale(final ULocale validatedULocale) {
    ULocale rootLocaleForFormatting =
        LocalesHierarchyUtils.getHighestAncestorLocale(validatedULocale);
    return SupportedLocale.builder()
        .localeForTranslations(validatedULocale)
        .relatedLocalesForFormatting(
            Stream.concat(
                    Stream.of(rootLocaleForFormatting),
                    LocalesHierarchyUtils.getDescendantLocales(rootLocaleForFormatting).stream())
                .collect(Collectors.toSet()))
        .build();
  }

  /**
   * Returns the locale identifying the language in which translations are available.
   *
   * @return The supported locale for translations
   */
  public abstract ULocale localeForTranslations();

  /**
   * Returns the list of locales, compatible for usage with the given {@link
   * #localeForTranslations()} from a locale hierarchy perspective. These locales can be used to
   * apply formatting operations on Strings.
   *
   * @return The list of locales for formatting
   */
  public abstract Set<ULocale> relatedLocalesForFormatting();

  /**
   * Returns a {@link Builder} instance that will allow you to manually create a {@link
   * SupportedLocale} instance.
   *
   * @return The builder
   */
  public static Builder builder() {
    return new AutoValue_SupportedLocale.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    Builder() {} // package private constructor

    /**
     * Configures the locale identifying the language in which translations are available.
     *
     * @param localeForTranslations the supported locale for translations
     * @return The {@link Builder} instance
     */
    public abstract Builder localeForTranslations(final ULocale localeForTranslations);

    /**
     * Configures the list of locales, compatible for usage with the given {@link
     * #localeForTranslations()} from a locale hierarchy perspective. These locales can be used to
     * apply formatting operations on Strings.
     *
     * @param relatedLocalesForFormatting the list of locales for formatting
     * @return The {@link Builder} instance
     */
    public abstract Builder relatedLocalesForFormatting(
        final Set<ULocale> relatedLocalesForFormatting);

    abstract SupportedLocale autoBuild(); // not public

    /**
     * Builds a {@link SupportedLocale} out of this builder.
     *
     * <ul>
     *   <li>All provided locales will be validated as normalized and canonicalized.
     *   <li>Their availability in CLDR will be checked and enforced
     *   <li>Locales hierarchy will be validated for all
     * </ul>
     *
     * <p>This is safe to be called several times on the same builder.
     *
     * @throws IllegalStateException if any of the builder property does not match the requirements.
     */
    public final SupportedLocale build() {
      SupportedLocale sl = autoBuild();

      Preconditions.checkState(
          !sl.localeForTranslations().equals(ULocale.ROOT),
          "The given localeForTranslations cannot be the root.");

      Preconditions.checkState(
          AvailableLocalesUtils.getCldrLocales().contains(sl.localeForTranslations()),
          "The given localeForTranslations %s must be canonical and available in CLDR.",
          sl.localeForTranslations().toLanguageTag());

      Preconditions.checkState(
          sl.relatedLocalesForFormatting().contains(sl.localeForTranslations()),
          "The localeForTranslations %s must be present in the list for relatedLocalesForFormatting.",
          sl.localeForTranslations().toLanguageTag());

      final ULocale rootLocaleForFormatting =
          LocalesHierarchyUtils.getHighestAncestorLocale(sl.localeForTranslations());
      sl.relatedLocalesForFormatting().stream()
          .forEach(
              relatedLocale -> {
                Preconditions.checkState(
                    AvailableLocalesUtils.getCldrLocales().contains(relatedLocale),
                    "The given relatedLocaleForFormatting %s must be canonical and available in CLDR.",
                    relatedLocale.toLanguageTag());
                Preconditions.checkState(
                    LocalesHierarchyUtils.isSameLocale(relatedLocale, rootLocaleForFormatting)
                        || LocalesHierarchyUtils.isDescendantLocale(
                            relatedLocale, rootLocaleForFormatting),
                    "The given relatedLocaleForFormatting %s is not the same as, or a descendant of the localeForTranslations %s.",
                    relatedLocale.toLanguageTag(),
                    rootLocaleForFormatting.toLanguageTag());
              });
      return sl;
    }
  }
}
