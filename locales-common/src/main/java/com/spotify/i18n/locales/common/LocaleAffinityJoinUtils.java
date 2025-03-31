package com.spotify.i18n.locales.common;

import com.ibm.icu.util.LocaleMatcher;
import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.impl.LocalesMatcherBaseImpl;
import com.spotify.i18n.locales.common.model.ReferenceLocale;
import com.spotify.i18n.locales.utils.languagetag.LanguageTagUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class LocaleAffinityJoinUtils {

  /** Prepared {@link LocaleMatcher}, ready to find the best matching reference locale */
  private static final LocaleMatcher REFERENCE_LOCALE_MATCHER =
      LocaleMatcher.builder()
          .setSupportedULocales(ReferenceLocale.availableReferenceLocales())
          .setNoDefaultLocale()
          .build();

  private LocaleAffinityJoinUtils() {}

  public static List<ReferenceLocale> getCorrespondingReferenceLocales(final String languageTag) {
    return LanguageTagUtils.parse(languageTag)
        .map(parsedLocale -> getCorrespondingReferenceLocales(parsedLocale))
        .orElse(Collections.emptyList());
  }

  private static List<ReferenceLocale> getCorrespondingReferenceLocales(
      final ULocale parsedLocale) {
    final LocalesMatcher matcher =
        LocalesMatcherBaseImpl.builder().supportedLocales(Set.of(parsedLocale)).build();
    return (ReferenceLocale.availableReferenceLocales().stream()
        .map(
            refLocale ->
                ReferenceLocale.builder()
                    .referenceLocale(refLocale)
                    .affinityScore(matcher.match(refLocale.toLanguageTag()).matchingScore())
                    .build())
        .filter(refLocale -> refLocale.affinityScore() != 0)
        .sorted((rl1, rl2) -> Integer.compare(rl2.affinityScore(), rl1.affinityScore()))
        .collect(Collectors.toList()));
  }

  public static Optional<ULocale> getBestMatchingReferenceLocale(final String languageTag) {
    return LanguageTagUtils.parse(languageTag).map(REFERENCE_LOCALE_MATCHER::getBestMatch);
  }
}
