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
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class LocaleAffinityCalculatorFactoryTest {

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
      when(mockedBuilder.supportedLocales(any())).thenReturn(mockedBuilder);
      when(mockedBuilder.build()).thenReturn(mockedCalculator);

      final LocaleAffinityCalculator built =
          LocaleAffinityCalculatorFactory.getDefaultInstance()
              .buildLocaleAffinityCalculatorForAcceptLanguage(acceptLanguage);

      assertEquals(mockedCalculator, built);
      verify(mockedBuilder)
          .supportedLocales(
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
      when(mockedBuilder.supportedLocales(any())).thenReturn(mockedBuilder);
      when(mockedBuilder.build()).thenReturn(mockedCalculator);

      final LocaleAffinityCalculator built =
          LocaleAffinityCalculatorFactory.getDefaultInstance()
              .buildLocaleAffinityCalculatorForLanguageTags(languageTags);

      assertEquals(mockedCalculator, built);
      verify(mockedBuilder)
          .supportedLocales(
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
      when(mockedBuilder.supportedLocales(any())).thenReturn(mockedBuilder);
      when(mockedBuilder.build()).thenReturn(mockedCalculator);

      final LocaleAffinityCalculator built =
          LocaleAffinityCalculatorFactory.getDefaultInstance()
              .buildLocaleAffinityCalculatorForLocales(locales);

      assertEquals(mockedCalculator, built);
      verify(mockedBuilder).supportedLocales(locales);
    }
  }

  public static Stream<Arguments> whenBuildingInstanceForLocalesSet_returnsExpectedCalculator() {
    return Stream.of(
        Arguments.of(Collections.emptySet(), Collections.emptySet()),
        Arguments.of(Set.of(ULocale.ROOT, ULocale.FRENCH, ULocale.JAPANESE)));
  }
}
