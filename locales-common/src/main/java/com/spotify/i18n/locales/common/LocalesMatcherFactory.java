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
import com.spotify.i18n.locales.common.impl.LocalesMatcherBaseImpl;
import com.spotify.i18n.locales.utils.acceptlanguage.AcceptLanguageUtils;
import com.spotify.i18n.locales.utils.languagetag.LanguageTagUtils;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Locale.LanguageRange;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A factory for creating instances of {@link LocalesMatcher}.
 *
 * @author Eric Fjøsne
 */
public class LocalesMatcherFactory {

  public static LocalesMatcherFactory getDefaultInstance() {
    return new LocalesMatcherFactory();
  }

  private LocalesMatcherFactory() {}

  /**
   * Returns a preconfigured, ready-to-use instance of {@link LocalesMatcher}, using all valid
   * locales present in the Accept-Language as target supported locales.
   *
   * <p>Malformed or null Accept-Language values will be ignored.
   *
   * <p>Invalid or improperly formatted contained language tags will be ignored.
   *
   * @param acceptLanguage The Accept-Language value
   * @return Pre-configured matcher
   * @see LocalesMatcher
   * @see <a
   *     href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Language">Accept-Language
   *     headers documentation</a>
   */
  public LocalesMatcher buildLocalesMatcherForAcceptLanguage(
      @Nullable final String acceptLanguage) {
    return buildLocalesMatcherForLanguageTags(
        AcceptLanguageUtils.parse(acceptLanguage).stream()
            .map(LanguageRange::getRange)
            .collect(Collectors.toSet()));
  }

  /**
   * Returns a preconfigured, ready-to-use instance of {@link LocalesMatcher}, using the supplied
   * language tags as supported locales.
   *
   * <p>Invalid or improperly formatted language tags will be ignored.
   *
   * @return Pre-configured matcher
   * @see ULocale
   */
  public LocalesMatcher buildLocalesMatcherForLocales(final Set<ULocale> locales) {
    Preconditions.checkNotNull(locales);
    return LocalesMatcherBaseImpl.builder().supportedLocales(locales).build();
  }

  /**
   * Returns a preconfigured, ready-to-use instance of {@link LocalesMatcher}, using the supplied
   * language tags as supported locales.
   *
   * <p>Invalid or improperly formatted language tags will be ignored.
   *
   * @return Pre-configured matcher
   * @see LocalesMatcher
   * @see <a href="https://en.wikipedia.org/wiki/IETF_language_tag">IETF BCP 47 language tag</a>
   */
  public LocalesMatcher buildLocalesMatcherForLanguageTags(final Set<String> languageTags) {
    Preconditions.checkNotNull(languageTags);
    return buildLocalesMatcherForLocales(
        languageTags.stream()
            .map(LanguageTagUtils::parse)
            .flatMap(Optional::stream)
            .collect(Collectors.toSet()));
  }
}
