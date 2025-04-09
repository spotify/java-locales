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

package com.spotify.i18n.locales.common;

import com.google.common.base.Preconditions;
import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.impl.LocaleAffinityCalculatorBaseImpl;
import com.spotify.i18n.locales.common.impl.RelatedReferenceLocalesCalculatorBaseImpl;
import com.spotify.i18n.locales.utils.acceptlanguage.AcceptLanguageUtils;
import com.spotify.i18n.locales.utils.languagetag.LanguageTagUtils;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Locale.LanguageRange;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A factory for creating instances of locale affinity related helpers:
 *
 * <ul>
 *   <li>{@link LocaleAffinityCalculator}: A helper that calculates a locale affinity for a given
 *       language tag.
 *   <li>{@link RelatedReferenceLocalesCalculator}: A helper that enables reference locales based
 *       operations, most notably to join datasets by enabling match operations between an origin
 *       and a target locale.
 * </ul>
 *
 * @author Eric Fjøsne
 */
public class LocaleAffinityHelpersFactory {

  public static LocaleAffinityHelpersFactory getDefaultInstance() {
    return new LocaleAffinityHelpersFactory();
  }

  private LocaleAffinityHelpersFactory() {}

  /**
   * Returns a pre-configured, ready-to-use instance of {@link LocaleAffinityCalculator}, that will
   * calculate affinity for a given language tag, against all valid locales present in the
   * Accept-Language.
   *
   * <p>Malformed, empty or null Accept-Language values will be ignored.
   *
   * <p>Invalid or improperly formatted contained language tags will be ignored.
   *
   * @param acceptLanguage The Accept-Language value
   * @return Pre-configured locale affinity calculator
   * @see LocaleAffinityCalculator
   * @see <a
   *     href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Language">Accept-Language
   *     headers documentation</a>
   */
  public LocaleAffinityCalculator buildAffinityCalculatorForAcceptLanguage(
      @Nullable final String acceptLanguage) {
    return buildAffinityCalculatorForLanguageTags(
        AcceptLanguageUtils.parse(acceptLanguage).stream()
            .map(LanguageRange::getRange)
            .collect(Collectors.toSet()));
  }

  /**
   * Returns a pre-configured, ready-to-use instance of {@link LocaleAffinityCalculator}, that will
   * calculate affinity for a given language tag, against all supplied locales.
   *
   * @return Pre-configured locale affinity calculator
   * @see LocaleAffinityCalculator
   * @see ULocale
   */
  public LocaleAffinityCalculator buildAffinityCalculatorForLocales(final Set<ULocale> locales) {
    Preconditions.checkNotNull(locales);
    return LocaleAffinityCalculatorBaseImpl.builder().againstLocales(locales).build();
  }

  /**
   * Returns a pre-configured, ready-to-use instance of {@link LocaleAffinityCalculator}, that will
   * calculate affinity for a given language tag, against all supplied language tags.
   *
   * <p>Invalid or improperly formatted language tags will be ignored.
   *
   * @return Pre-configured locale affinity calculator
   * @see LocaleAffinityCalculator
   * @see <a href="https://en.wikipedia.org/wiki/IETF_language_tag">IETF BCP 47 language tag</a>
   */
  public LocaleAffinityCalculator buildAffinityCalculatorForLanguageTags(
      final Set<String> languageTags) {
    Preconditions.checkNotNull(languageTags);
    return buildAffinityCalculatorForLocales(
        languageTags.stream()
            .map(LanguageTagUtils::parse)
            .flatMap(Optional::stream)
            .collect(Collectors.toSet()));
  }

  /**
   * Returns a pre-configured, ready-to-use instance of {@link RelatedReferenceLocalesCalculator}.
   *
   * @return Pre-configured calculator
   * @see RelatedReferenceLocalesCalculator
   */
  public RelatedReferenceLocalesCalculator buildRelatedReferenceLocalesCalculator() {
    return RelatedReferenceLocalesCalculatorBaseImpl.builder().build();
  }
}
