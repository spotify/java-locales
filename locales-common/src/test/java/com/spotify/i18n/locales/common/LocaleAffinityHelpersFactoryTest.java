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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.impl.LocaleAffinityCalculatorBaseImpl;
import com.spotify.i18n.locales.common.model.LocaleAffinity;
import com.spotify.i18n.locales.common.model.RelatedReferenceLocale;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class LocaleAffinityHelpersFactoryTest {

  @ParameterizedTest
  @MethodSource
  public void whenBuildingInstanceForAcceptLanguage_returnsExpectedCalculator(
      final String acceptLanguage, final Set<String> expectedLanguageTagsForBuilder) {
    try (MockedStatic<LocaleAffinityCalculatorBaseImpl> calculatorStaticMock =
        Mockito.mockStatic(LocaleAffinityCalculatorBaseImpl.class)) {
      final LocaleAffinityCalculatorBaseImpl.Builder mockedBuilder = mock();
      final LocaleAffinityCalculator mockedCalculator = mock();
      calculatorStaticMock
          .when(() -> LocaleAffinityCalculatorBaseImpl.builder())
          .thenReturn(mockedBuilder);
      when(mockedBuilder.againstLocales(any())).thenReturn(mockedBuilder);
      when(mockedBuilder.build()).thenReturn(mockedCalculator);

      final LocaleAffinityCalculator built =
          LocaleAffinityHelpersFactory.getDefaultInstance()
              .buildAffinityCalculatorForAcceptLanguage(acceptLanguage);

      assertEquals(mockedCalculator, built);
      verify(mockedBuilder)
          .againstLocales(
              expectedLanguageTagsForBuilder.stream()
                  .map(ULocale::forLanguageTag)
                  .collect(Collectors.toSet()));
    }
  }

  public static Stream<Arguments>
      whenBuildingInstanceForAcceptLanguage_returnsExpectedCalculator() {
    return Stream.of(
        Arguments.of(null, Collections.emptySet()),
        Arguments.of("", Collections.emptySet()),
        Arguments.of("This is an invalid Accept-Language value.", Collections.emptySet()),
        Arguments.of(
            "JA_jp@calendar=buddhist-u-timezone-CET, FR_be;q=0.3,       ZH-Hant-u-coucou; q=0.2, fr-CA@calendar=gregorian     ",
            Set.of("ja-JP", "fr-CA", "fr-BE", "zh-Hant")));
  }

  @ParameterizedTest
  @MethodSource
  public void whenBuildingInstanceForLanguageTagsSet_returnsExpectedCalculator(
      final Set<String> languageTags, final Set<String> expectedLanguageTagsForBuilder) {
    try (MockedStatic<LocaleAffinityCalculatorBaseImpl> calculatorStaticMock =
        Mockito.mockStatic(LocaleAffinityCalculatorBaseImpl.class)) {
      final LocaleAffinityCalculatorBaseImpl.Builder mockedBuilder = mock();
      final LocaleAffinityCalculator mockedCalculator = mock();
      calculatorStaticMock
          .when(() -> LocaleAffinityCalculatorBaseImpl.builder())
          .thenReturn(mockedBuilder);
      when(mockedBuilder.againstLocales(any())).thenReturn(mockedBuilder);
      when(mockedBuilder.build()).thenReturn(mockedCalculator);

      final LocaleAffinityCalculator built =
          LocaleAffinityHelpersFactory.getDefaultInstance()
              .buildAffinityCalculatorForLanguageTags(languageTags);

      assertEquals(mockedCalculator, built);
      verify(mockedBuilder)
          .againstLocales(
              expectedLanguageTagsForBuilder.stream()
                  .map(ULocale::forLanguageTag)
                  .collect(Collectors.toSet()));
    }
  }

  public static Stream<Arguments>
      whenBuildingInstanceForLanguageTagsSet_returnsExpectedCalculator() {
    return Stream.of(
        Arguments.of(Collections.emptySet(), Collections.emptySet()),
        Arguments.of(
            Set.of("", "This is an invalid Accept-Language value."), Collections.emptySet()),
        Arguments.of(
            Set.of("", "JA_jp", "This is an invalid Accept-Language value.", "fr-latn_be"),
            Set.of("ja-JP", "fr-Latn-BE")));
  }

  @ParameterizedTest
  @MethodSource
  public void whenBuildingInstanceForLocalesSet_returnsExpectedCalculator(
      final Set<ULocale> locales) {
    try (MockedStatic<LocaleAffinityCalculatorBaseImpl> calculatorStaticMock =
        Mockito.mockStatic(LocaleAffinityCalculatorBaseImpl.class)) {
      final LocaleAffinityCalculatorBaseImpl.Builder mockedBuilder = mock();
      final LocaleAffinityCalculator mockedCalculator = mock();
      calculatorStaticMock
          .when(() -> LocaleAffinityCalculatorBaseImpl.builder())
          .thenReturn(mockedBuilder);
      when(mockedBuilder.againstLocales(any())).thenReturn(mockedBuilder);
      when(mockedBuilder.build()).thenReturn(mockedCalculator);

      final LocaleAffinityCalculator built =
          LocaleAffinityHelpersFactory.getDefaultInstance()
              .buildAffinityCalculatorForLocales(locales);

      assertEquals(mockedCalculator, built);
      verify(mockedBuilder).againstLocales(locales);
    }
  }

  public static Stream<Arguments> whenBuildingInstanceForLocalesSet_returnsExpectedCalculator() {
    return Stream.of(
        Arguments.of(Collections.emptySet(), Collections.emptySet()),
        Arguments.of(Set.of(ULocale.ROOT, ULocale.FRENCH, ULocale.JAPANESE)));
  }

  @Test
  void whenBuildingRelatedReferenceLocalesCalculator_returnsExpectedCalculator() {
    assertTrue(
        LocaleAffinityHelpersFactory.getDefaultInstance().buildRelatedReferenceLocalesCalculator()
            instanceof ReferenceLocalesCalculator);
  }

  @ParameterizedTest
  @MethodSource
  void
      whenJoiningDatasetsUsingReferenceLocalesCalculator_joinsBasedOnExpectedRelatedReferenceLocale(
          final String languageTagInDataset1,
          final String languageTagInDataset2,
          final String expectedReferenceLanguageTag,
          final LocaleAffinity expectedAffinity) {
    ReferenceLocalesCalculator calculator =
        LocaleAffinityHelpersFactory.getDefaultInstance().buildRelatedReferenceLocalesCalculator();

    List<RelatedReferenceLocale> relatedReferenceLocales =
        calculator.calculateRelatedReferenceLocales(languageTagInDataset1);
    Optional<ULocale> referenceLocale =
        calculator.calculateBestMatchingReferenceLocale(languageTagInDataset2);

    assertEquals(
        RelatedReferenceLocale.builder()
            .referenceLocale(ULocale.forLanguageTag(expectedReferenceLanguageTag))
            .affinity(expectedAffinity)
            .build(),
        relatedReferenceLocales.stream()
            .filter(rrl -> rrl.referenceLocale().equals(referenceLocale.get()))
            .findFirst()
            .get());
  }

  public static Stream<Arguments>
      whenJoiningDatasetsUsingReferenceLocalesCalculator_joinsBasedOnExpectedRelatedReferenceLocale() {
    return Stream.of(
        // Chinese (Hong-Kong), Chinese (Traditional) -> Chinese (Taiwan)
        Arguments.of("zh-HK", "zh-Hant", "zh-TW", LocaleAffinity.SAME_OR_MUTUALLY_INTELLIGIBLE),

        // Chinese (Hong-Kong), Cantonese (Hong-Kong) -> Cantonese
        Arguments.of("zh-HK", "yue-HK", "yue", LocaleAffinity.HIGH),

        // Dutch (Belgium), Dutch (Netherlands) -> Dutch
        Arguments.of("nl-BE", "nl-NL", "nl", LocaleAffinity.SAME_OR_MUTUALLY_INTELLIGIBLE),

        // French (Switzerland), French (Canada) -> French
        Arguments.of("fr-CH", "fr-CA", "fr-CA", LocaleAffinity.SAME_OR_MUTUALLY_INTELLIGIBLE));
  }
}
