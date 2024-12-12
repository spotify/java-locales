/*-
 * -\-\-
 * locales-utils
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

package com.spotify.i18n.locales.utils.languagetag;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.ibm.icu.util.ULocale;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LanguageTagUtilsTest {

  @Test
  public void canNormalizeNullValue() {
    assertThat(LanguageTagUtils.normalize(null), is("und"));
  }

  @Test
  public void testNormalize() {
    assertThat(LanguageTagUtils.normalize(""), is("und"));
    assertThat(LanguageTagUtils.normalize("JA_JP"), is("ja-JP"));
    assertThat(LanguageTagUtils.normalize("EN_us"), is("en-US"));
    assertThat(LanguageTagUtils.normalize("ca_BE"), is("ca-BE"));
    assertThat(LanguageTagUtils.normalize("pouet pouet"), is("und"));
  }

  @ParameterizedTest
  @MethodSource
  public void testParse(
      final String givenLanguageTag, final Optional<ULocale> expectedOptionalULocale) {
    assertThat(LanguageTagUtils.parse(givenLanguageTag), is(expectedOptionalULocale));
  }

  static Stream<Arguments> testParse() {
    Map<String, Optional<ULocale>> m = new HashMap<>();

    // Nice values, with wrong case or with _ instead of -
    m.put("en-us", Optional.of(ULocale.US));
    m.put("EN-us", Optional.of(ULocale.US));
    m.put("EN-US", Optional.of(ULocale.US));
    m.put("en_150", Optional.of(ULocale.forLanguageTag("en-150")));
    m.put("en_us", Optional.of(ULocale.US));
    m.put("en_US", Optional.of(ULocale.US));
    m.put("EN_us", Optional.of(ULocale.US));
    m.put("EN_US", Optional.of(ULocale.US));
    m.put("fil-ph", Optional.of(ULocale.forLanguageTag("fil-PH")));
    m.put("FIL-ph", Optional.of(ULocale.forLanguageTag("fil-PH")));
    m.put("FIL-PH", Optional.of(ULocale.forLanguageTag("fil-PH")));
    m.put("fil_ph", Optional.of(ULocale.forLanguageTag("fil-PH")));
    m.put("fil_PH", Optional.of(ULocale.forLanguageTag("fil-PH")));
    m.put("FIL_ph", Optional.of(ULocale.forLanguageTag("fil-PH")));
    m.put("FIL_PH", Optional.of(ULocale.forLanguageTag("fil-PH")));
    m.put("fr-fr", Optional.of(ULocale.FRANCE));
    m.put("FR-fr", Optional.of(ULocale.FRANCE));
    m.put("FR-FR", Optional.of(ULocale.FRANCE));
    m.put("fr_fr", Optional.of(ULocale.FRANCE));
    m.put("fr_FR", Optional.of(ULocale.FRANCE));
    m.put("FR_fr", Optional.of(ULocale.FRANCE));
    m.put("FR_FR", Optional.of(ULocale.FRANCE));
    m.put("sr", Optional.of(ULocale.forLanguageTag("sr")));

    // Missing scripts
    m.put("sr-SR", Optional.of(ULocale.forLanguageTag("sr-SR")));
    m.put("zh", Optional.of(ULocale.forLanguageTag("zh")));
    m.put("zh-CN", Optional.of(ULocale.forLanguageTag("zh-CN")));
    m.put("zh-HK", Optional.of(ULocale.forLanguageTag("zh-HK")));

    // Invalid language + region combinations
    m.put("bho-AR", Optional.of(ULocale.forLanguageTag("bho-AR")));
    m.put("bs-MK", Optional.of(ULocale.forLanguageTag("bs-MK")));
    m.put("ca-BE", Optional.of(ULocale.forLanguageTag("ca-BE")));
    m.put("ja-BE", Optional.of(ULocale.forLanguageTag("ja-BE")));
    m.put("sr-BE", Optional.of(ULocale.forLanguageTag("sr-BE")));
    m.put("zh-Hans-JP", Optional.of(ULocale.forLanguageTag("zh-Hans-JP")));

    // Nasty cases, not parseable
    m.put("-US", Optional.empty());
    m.put("_US", Optional.empty());

    // Nasty cases, still parseable
    m.put("en-", Optional.of(ULocale.ENGLISH));
    m.put("en-GB-", Optional.of(ULocale.UK));
    m.put("en-GB;q=1.0", Optional.of(ULocale.ENGLISH));
    m.put("en-GB;q=1.0,fr;q=0.5", Optional.of(ULocale.ENGLISH));
    m.put("en-GB_", Optional.of(ULocale.UK));
    m.put("en_", Optional.of(ULocale.ENGLISH));

    // values with extensions
    m.put(
        "en_AU@calendar=buddhist",
        Optional.of(ULocale.forLanguageTag("en-AU-u-calendar-buddhist")));
    m.put(
        "en_US-u-calendar=buddhist",
        Optional.of(ULocale.forLanguageTag("en-US-u-calendar-buddhist")));
    m.put(
        "en_US@calendar=japanese",
        Optional.of(ULocale.forLanguageTag("en-US-u-calendar-japanese")));
    m.put(
        "fr_BE-u-calendar-gregorian",
        Optional.of(ULocale.forLanguageTag("fr-BE-u-calendar-gregorian")));

    // Invalid value, but parseable
    m.put("hello-world", Optional.of(ULocale.forLanguageTag("hello-WORLD")));
    m.put("hello-world", Optional.of(ULocale.forLanguageTag("hello-WORLD")));
    m.put("hello_world", Optional.of(ULocale.forLanguageTag("hello-WORLD")));

    // Invalid value
    m.put("test test", Optional.empty());

    return m.entrySet().stream().map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
  }

  private static ULocale forLanguageTagWithAttribute(String languageTag, String calendar) {
    return new ULocale.Builder()
        .setLanguageTag(languageTag)
        // .setUnicodeLocaleKeyword("ca", "calendar=" + calendar)
        .build();
  }
}
