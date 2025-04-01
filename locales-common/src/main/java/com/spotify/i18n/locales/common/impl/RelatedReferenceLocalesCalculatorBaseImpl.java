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

package com.spotify.i18n.locales.common.impl;

import com.google.auto.value.AutoValue;
import com.ibm.icu.util.LocaleMatcher;
import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.LocaleAffinityCalculator;
import com.spotify.i18n.locales.common.RelatedReferenceLocalesCalculator;
import com.spotify.i18n.locales.common.model.LocaleAffinity;
import com.spotify.i18n.locales.common.model.RelatedReferenceLocale;
import com.spotify.i18n.locales.utils.languagetag.LanguageTagUtils;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@AutoValue
public abstract class RelatedReferenceLocalesCalculatorBaseImpl
    implements RelatedReferenceLocalesCalculator {

  /** Prepared {@link LocaleMatcher}, ready to find the best matching reference locale */
  private static final LocaleMatcher REFERENCE_LOCALE_MATCHER =
      LocaleMatcher.builder()
          .setSupportedULocales(RelatedReferenceLocale.availableReferenceLocales())
          .setNoDefaultLocale()
          .build();

  @Override
  public List<RelatedReferenceLocale> getRelatedReferenceLocales(
      @Nullable final String languageTag) {
    return LanguageTagUtils.parse(languageTag)
        .map(parsedLocale -> getCorrespondingReferenceLocales(parsedLocale))
        .orElse(Collections.emptyList());
  }

  private List<RelatedReferenceLocale> getCorrespondingReferenceLocales(
      final ULocale parsedLocale) {
    final LocaleAffinityCalculator affinityCalculator = buildAffinityCalculator(parsedLocale);
    return RelatedReferenceLocale.availableReferenceLocales().stream()
        .map(
            refLocale ->
                RelatedReferenceLocale.builder()
                    .referenceLocale(refLocale)
                    .affinity(affinityCalculator.calculate(refLocale.toLanguageTag()).affinity())
                    .build())
        // We only retain reference locales with some affinity
        .filter(refLocale -> refLocale.affinity() != LocaleAffinity.NONE)
        // Sorting by descending affinity
        .sorted((rl1, rl2) -> rl2.affinity().compareTo(rl1.affinity()))
        .collect(Collectors.toList());
  }

  private LocaleAffinityCalculator buildAffinityCalculator(final ULocale parsedLocale) {
    return LocaleAffinityCalculatorBaseImpl.builder()
        .supportedLocales(Set.of(parsedLocale))
        .build();
  }

  @Override
  public Optional<ULocale> getBestMatchingReferenceLocale(@Nullable final String languageTag) {
    return LanguageTagUtils.parse(languageTag).map(REFERENCE_LOCALE_MATCHER::getBestMatch);
  }

  public static Builder builder() {
    return new AutoValue_RelatedReferenceLocalesCalculatorBaseImpl.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    Builder() {}

    abstract RelatedReferenceLocalesCalculatorBaseImpl autoBuild();

    public final RelatedReferenceLocalesCalculator build() {
      return autoBuild();
    }
  }
}
