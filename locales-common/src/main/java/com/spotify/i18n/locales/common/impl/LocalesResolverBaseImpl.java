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
import com.google.common.base.Strings;
import com.ibm.icu.util.LocaleMatcher;
import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.LocalesResolver;
import com.spotify.i18n.locales.common.model.ResolvedLocale;
import com.spotify.i18n.locales.common.model.SupportedLocale;
import com.spotify.i18n.locales.utils.acceptlanguage.AcceptLanguageUtils;
import com.spotify.i18n.locales.utils.available.AvailableLocalesUtils;
import com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils;
import com.spotify.i18n.locales.utils.languagetag.LanguageTagUtils;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base implementation of {@link LocalesResolver} that resolves a given "Accept-Language" value into
 * a {@link ResolvedLocale}, based on a given set of {@link SupportedLocale}s.
 *
 * <p>This class is not intended for public subclassing. New object instances must be created using
 * the builder pattern, starting with the {@link #builder()} method.
 *
 * @see SupportedLocale
 * @see ResolvedLocale
 * @see <a
 *     href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Language">Accept-Language</a>
 * @author Eric Fjøsne
 */
@AutoValue
public abstract class LocalesResolverBaseImpl implements LocalesResolver {

  /** Prepared {@link LocaleMatcher} ready to find the best matching CDLR available locale */
  private static final LocaleMatcher AVAILABLE_UNICODE_LOCALES_MATCHER =
      LocaleMatcher.builder()
          .setSupportedULocales(AvailableLocalesUtils.getCldrLocales())
          .setNoDefaultLocale()
          .build();

  /** Set containing all distinct language codes for locales available in CLDR */
  private static final Set<String> AVAILABLE_UNICODE_LANGUAGE_CODES =
      AvailableLocalesUtils.getCldrLocales().stream()
          .map(ULocale::getLanguage)
          .collect(Collectors.toSet());

  /** Wildcard character for language ranges */
  private static final String LANGUAGE_RANGE_WILDCARD = "*";

  /** Parseable code for undefined value in a language tag */
  private static final String ULOCALE_UNDEFINED_CODE = "Und";

  /**
   * Returns the set of {@link SupportedLocale} against which locale resolution will be performed.
   *
   * @return The set of {@link SupportedLocale}s
   */
  public abstract Set<SupportedLocale> supportedLocales();

  /**
   * Returns the default {@link ResolvedLocale} that will be used as final fallback for a given
   * Accept-Language value when no match can be found, or when the resolution fails for some reason.
   *
   * @return The default {@link ResolvedLocale}
   */
  public abstract ResolvedLocale defaultResolvedLocale();

  /**
   * Returns the {@link ResolvedLocale}, based on a given "Accept-Language" value.
   *
   * @return the resolved locale
   */
  @Override
  public ResolvedLocale resolve(final String acceptLanguage) {
    // Fail fast when resolution is impossible
    if (Strings.isNullOrEmpty(acceptLanguage)) {
      return defaultResolvedLocale();
    }

    // Get a map of supported locales, keyed by locale for translations, with corresponding related
    // locales for formatting as values
    Map<ULocale, Set<ULocale>> supportedLocalesMap =
        supportedLocales().stream()
            .collect(
                Collectors.toMap(
                    sl -> sl.localeForTranslations(), sl -> sl.relatedLocalesForFormatting()));

    // Generate matcher for all supported translations locales
    LocaleMatcher localeMatcherForTranslations = getLocaleMatcher(supportedLocalesMap.keySet());

    // We parse the accept-language value into a list of language ranges
    List<LanguageRange> languageRanges = AcceptLanguageUtils.parse(acceptLanguage);

    // We first try to get the best match directly
    Optional<ResolvedLocale> bestMatch =
        getBestMatch(languageRanges, localeMatcherForTranslations, supportedLocalesMap);

    // If there was no match, we override the accept-language entries with values from the default
    // locale, to find the closest match possible. This is required when the set of supported
    // locales contains several variants of the same language (for instance: en and en-GB, fr and
    // fr-CA, es and es-419, ...)
    if (bestMatch.isEmpty()) {
      bestMatch =
          getBestMatchBasedOnDefaultLocale(
              defaultResolvedLocale(),
              languageRanges,
              localeMatcherForTranslations,
              supportedLocalesMap);
    }

    // We return our best match, or the default locale if there was no such match.
    return bestMatch.orElse(defaultResolvedLocale());
  }

  /**
   * Returns the best matching {@link ResolvedLocale}, based on a given normalized "Accept-Language"
   *
   * @param languageRanges Accept language entries
   * @param localeMatcherForTranslations the pre-configured locale matcher for translations
   * @param supportedLocalesMap A map of supported locales for translations, and their corresponding
   *     supported locales for formatting
   * @return The optional best matching {@link ResolvedLocale}
   */
  private Optional<ResolvedLocale> getBestMatch(
      final List<LanguageRange> languageRanges,
      final LocaleMatcher localeMatcherForTranslations,
      final Map<ULocale, Set<ULocale>> supportedLocalesMap) {
    List<LanguageRange> overriddenAcceptLanguageEntries =
        getAcceptLanguageEntriesWithBestMatchingAvailableLocaleOverride(languageRanges);
    final String normalizedAcceptLanguage = languageRangesToValue(overriddenAcceptLanguageEntries);
    return Optional.ofNullable(localeMatcherForTranslations.getBestMatch(normalizedAcceptLanguage))
        .map(
            localeForTranslations ->
                ResolvedLocale.builder()
                    .localeForTranslations(localeForTranslations)
                    .localeForTranslationsFallbacks(
                        getRecommendedLocaleForTranslationsFallbacks(
                            localeForTranslations, supportedLocalesMap.keySet()))
                    .localeForFormatting(
                        getLocaleForFormatting(
                            supportedLocalesMap, normalizedAcceptLanguage, localeForTranslations))
                    .build());
  }

  /**
   * Returns the list of recommended fallback locales for translations
   *
   * @param resolvedLocaleForTranslations the resolved locale for translations
   * @param supportedLocales the set of supported locales
   * @return list of fallback locales for translations
   */
  private List<ULocale> getRecommendedLocaleForTranslationsFallbacks(
      final ULocale resolvedLocaleForTranslations, final Set<ULocale> supportedLocales) {
    return LocalesHierarchyUtils.getAncestorLocales(resolvedLocaleForTranslations).stream()
        // We want to ensure that we only consider supported locales as fallbacks
        .filter(supportedLocales::contains)
        .collect(Collectors.toList());
  }

  /**
   * Returns a list of {@link LanguageRange}, based on the given one, where the ranges were
   * overridden with their corresponding extended range, or their best matching available Unicode
   * locale, when needed.
   *
   * @param languageRanges List of {@link LanguageRange}
   * @return The copy of the given list of {@link LanguageRange}, with locale overrides
   */
  private List<LanguageRange> getAcceptLanguageEntriesWithBestMatchingAvailableLocaleOverride(
      List<LanguageRange> languageRanges) {
    return languageRanges.stream()
        .flatMap(this::getMatchingExtendedLanguageRanges)
        .map(this::getLanguageRangeWithMitigatedUnknownLanguageCode)
        .flatMap(Optional::stream)
        .collect(Collectors.toList());
  }

  /**
   * Returns a stream of {@link LanguageRange}, which results from extending the given {@link
   * LanguageRange#getRange()} to all the locales it covers, if it contains wildcards. A range
   * containing wildcards only will be ignored.
   *
   * @param languageRange The base {@link LanguageRange}
   * @return The stream of extended {@link LanguageRange}s
   * @see <a href="https://datatracker.ietf.org/doc/html/rfc4647#section-2.2">Extended Language
   *     Range</a>
   */
  private Stream<LanguageRange> getMatchingExtendedLanguageRanges(
      final LanguageRange languageRange) {
    // If there are no wildcards, we just return the range. This is the most likely outcome.
    if (!languageRange.getRange().contains(LANGUAGE_RANGE_WILDCARD)) {
      return Stream.of(languageRange);
    }

    // Strip the range of any trailing -*
    String cleanedRange =
        languageRange.getRange().replaceAll("(-\\" + LANGUAGE_RANGE_WILDCARD + ")*$", "");

    // The range only contained wildcards, so we simply ignore it.
    if (LANGUAGE_RANGE_WILDCARD.equals(cleanedRange)) {
      return Stream.of();
    }

    // There were only trailing wildcards in this range, meaning we don't need to extend it for
    // locale resolution to get the best possible match.
    if (!cleanedRange.contains(LANGUAGE_RANGE_WILDCARD)) {
      return Stream.of(new LanguageRange(cleanedRange, languageRange.getWeight()));
    }

    // In order to enable parsing, we replace * by Und
    String preparedRangeForParsing =
        cleanedRange.replace(LANGUAGE_RANGE_WILDCARD, ULOCALE_UNDEFINED_CODE);

    // Parse the locale and extend all undefined locale codes accordingly
    return LanguageTagUtils.parse(preparedRangeForParsing)
        .map(
            parsedLocale ->
                // As candidate locales for extension, we include and prioritize the most likely
                // locale according to CLDR, then add all the supported locales for translations
                Stream.concat(
                        Stream.of(ULocale.addLikelySubtags(parsedLocale)),
                        supportedLocales().stream().map(SupportedLocale::localeForTranslations))
                    // We extend the parsed locale by defining undefined locale codes,
                    // using the ones from the candidate locales, and retain only the ones available
                    // in CLDR.
                    // Note: Ideally, entries corresponding to the same region/country should be
                    // ordered by decreasing % of language coverage, to prioritize languages
                    // accordingly, but we don't do it here as we do not have this data at hand.
                    .map(
                        supportedLocale ->
                            mitigateParsedLocaleUndefinedCodes(supportedLocale, parsedLocale))
                    .filter(AvailableLocalesUtils.getCldrLocales()::contains)
                    // We remove potential duplicates
                    .distinct()
                    .map(
                        localeWithinRange ->
                            new LanguageRange(
                                localeWithinRange.toLanguageTag().toLowerCase(),
                                languageRange.getWeight())))
        .orElse(Stream.empty());
  }

  /**
   * Returns a new {@link ULocale} where any undefined attribute in the given `locale` is overridden
   * by the corresponding attribute
   *
   * @param supportedLocale the supported locale based on which mitigates will be applied
   * @param locale the parsed locale to be mitigated
   * @return mitigated locale
   */
  private ULocale mitigateParsedLocaleUndefinedCodes(ULocale supportedLocale, ULocale locale) {
    if (!locale.getCountry().isBlank()) {
      // If the country is defined, we need to mitigate the language and the script only.
      return new ULocale.Builder()
          .setLanguage(getMitigatedValue(locale.getLanguage(), supportedLocale.getLanguage()))
          .setScript(getMitigatedValue(locale.getScript(), supportedLocale.getScript()))
          .setRegion(locale.getCountry())
          .build();
    } else {
      // The script has to be defined, as the range would have been ignored in the first place
      // otherwise. In this case, only the language needs to be mitigated, the country/region can
      // safely be ignored.
      return new ULocale.Builder()
          .setLanguage(getMitigatedValue(locale.getLanguage(), supportedLocale.getLanguage()))
          .setScript(locale.getScript())
          .build();
    }
  }

  /**
   * Returns the `givenCode` if defined, the `givenCodeAlternative` otherwise.
   *
   * @param givenCode any locale code (language/script/region)
   * @param givenCodeAlternative the alternative to the `givenCode`
   * @return The mitigated code value
   */
  private String getMitigatedValue(final String givenCode, final String givenCodeAlternative) {
    return givenCode.isBlank() ? givenCodeAlternative : givenCode;
  }

  /**
   * Returns an optional copy of the given {@link LanguageRange}, where the locale is overridden
   * with its best matching available Unicode locale, when the given language code is not available
   * in CLDR.
   *
   * @param languageRange The base {@link LanguageRange}
   * @return The copy of the given {@link LanguageRange}, with overridden locale
   */
  private Optional<LanguageRange> getLanguageRangeWithMitigatedUnknownLanguageCode(
      final LanguageRange languageRange) {
    String languageCode =
        Optional.of(languageRange.getRange())
            .filter(lr -> lr.contains("-"))
            .map(lr -> lr.substring(0, lr.indexOf("-")))
            .orElse(languageRange.getRange());

    if (!"*".equals(languageCode) && !AVAILABLE_UNICODE_LANGUAGE_CODES.contains(languageCode)) {
      return Optional.ofNullable(
              AVAILABLE_UNICODE_LOCALES_MATCHER.getBestMatch(languageRange.getRange()))
          .map(
              bestMatchingLocale ->
                  new LanguageRange(
                      bestMatchingLocale.toLanguageTag().toLowerCase(), languageRange.getWeight()));
    } else {
      return Optional.of(languageRange);
    }
  }

  /**
   * Returns the accept-language formatted value, from a list of {@link LanguageRange}
   *
   * @param languageRanges list of {@link LanguageRange}
   * @return Corresponding accept-language formatted value
   */
  private String languageRangesToValue(List<LanguageRange> languageRanges) {
    return languageRanges.stream().map(LanguageRange::toString).collect(Collectors.joining(","));
  }

  /**
   * Returns the best matching {@link ResolvedLocale}, after overriding the {@link LanguageRange}
   * list with values from the default {@link ResolvedLocale}
   *
   * @param defaultResolvedLocale The default {@link ResolvedLocale}
   * @param languageRanges List of {@link LanguageRange}
   * @param localeMatcherForTranslations the pre-configured locale matcher for translations
   * @param supportedLocalesMap A map of supported locales for translations, and their corresponding
   *     supported locales for formatting
   * @return The optional best matching {@link ResolvedLocale}
   */
  private Optional<ResolvedLocale> getBestMatchBasedOnDefaultLocale(
      final ResolvedLocale defaultResolvedLocale,
      final List<LanguageRange> languageRanges,
      final LocaleMatcher localeMatcherForTranslations,
      final Map<ULocale, Set<ULocale>> supportedLocalesMap) {
    return getBestMatch(
        getAcceptLanguageEntriesWithOverrides(defaultResolvedLocale, languageRanges),
        localeMatcherForTranslations,
        supportedLocalesMap);
  }

  /**
   * Returns a copy of the given list of {@link LanguageRange}, where the ranges were overridden
   * based on what is contained in the default {@link ResolvedLocale}.
   *
   * @param defaultResolvedLocale The default {@link ResolvedLocale}
   * @param languageRanges List of {@link LanguageRange}
   * @return The copy of the given list of {@link LanguageRange}, with locale overrides
   */
  private List<LanguageRange> getAcceptLanguageEntriesWithOverrides(
      final ResolvedLocale defaultResolvedLocale, List<LanguageRange> languageRanges) {
    return languageRanges.stream()
        .map(lr -> languageRangeWithLocaleOverride(lr, defaultResolvedLocale))
        .collect(Collectors.toList());
  }

  /**
   * Returns a copy of the given {@link LanguageRange}, where the range was overridden based on what
   * is contained in the default {@link ResolvedLocale}.
   *
   * @param defaultResolvedLocale The default {@link ResolvedLocale}
   * @param languageRange The {@link LanguageRange}
   * @return The copy of the given {@link LanguageRange}, with overridden locale
   */
  private LanguageRange languageRangeWithLocaleOverride(
      final LanguageRange languageRange, final ResolvedLocale defaultResolvedLocale) {
    return new LanguageRange(
        overrideLanguageRangeBasedOnDefaultLocale(languageRange.getRange(), defaultResolvedLocale),
        languageRange.getWeight());
  }

  /**
   * Returns a new language range, built based on the default {@link
   * ResolvedLocale#localeForTranslations()}, with script and region attributes overridden from the
   * given language range
   *
   * @param defaultResolvedLocale The default {@link ResolvedLocale}
   * @param languageRange the language range, possibly containing script & region codes to use
   * @return The new languageRange
   */
  private String overrideLanguageRangeBasedOnDefaultLocale(
      final String languageRange, final ResolvedLocale defaultResolvedLocale) {
    return Optional.of(languageRange)
        .filter(lr -> lr.contains("-"))
        .map(
            lr ->
                defaultResolvedLocale.localeForTranslations().getLanguage()
                    + lr.substring(lr.indexOf("-")))
        .orElse(defaultResolvedLocale.localeForTranslations().getLanguage());
  }

  /**
   * Returns the {@link ULocale} that best matches the given localeToMatch, for formatting purposes
   *
   * @param supportedLocalesMap map of supported translations locales as keys, related formatting
   *     locales as values
   * @param localeToMatch the language tag for which we need to find a formatting match
   * @param localeForTranslations the locale that best matches for translations
   * @return the {@link ULocale} that best matches the given localeToMatch, for formatting purposes
   * @see ULocale
   */
  private ULocale getLocaleForFormatting(
      final Map<ULocale, Set<ULocale>> supportedLocalesMap,
      final String localeToMatch,
      final ULocale localeForTranslations) {
    return Optional.ofNullable(
            getLocaleMatcher(supportedLocalesMap.get(localeForTranslations))
                .getBestMatch(localeToMatch))
        .orElse(localeForTranslations);
  }

  /**
   * Returns a {@link LocaleMatcher} for a given set of supported {@link ULocale}
   *
   * @param supportedLocales the supported locales
   * @return the locale matcher
   * @see LocaleMatcher
   * @see ULocale
   */
  private LocaleMatcher getLocaleMatcher(final Set<ULocale> supportedLocales) {
    return LocaleMatcher.builder()
        .setSupportedULocales(supportedLocales)
        .setNoDefaultLocale()
        .build();
  }

  /**
   * Returns a {@link Builder} instance that will allow you to manually create a {@link
   * LocalesResolverBaseImpl} instance.
   *
   * @return The builder
   */
  public static Builder builder() {
    return new AutoValue_LocalesResolverBaseImpl.Builder();
  }

  /** A builder for a {@link LocalesResolverBaseImpl}. */
  @AutoValue.Builder
  public abstract static class Builder {
    Builder() {} // package private constructor

    /**
     * Configures the default {@link ResolvedLocale} that will be returned if locale resolution
     * fails for some reason.
     *
     * @param defaultResolvedLocale
     * @return The {@link Builder} instance
     */
    public abstract Builder defaultResolvedLocale(final ResolvedLocale defaultResolvedLocale);

    /**
     * Configures the set of {@link SupportedLocale} against which locale resolution will be
     * performed.
     *
     * @param supportedLocales
     * @return The {@link Builder} instance
     */
    public abstract Builder supportedLocales(final Set<SupportedLocale> supportedLocales);

    abstract LocalesResolverBaseImpl autoBuild();

    /** Builds a {@link LocalesResolver} out of this builder. */
    public final LocalesResolver build() {
      return autoBuild();
    }
  }
}
