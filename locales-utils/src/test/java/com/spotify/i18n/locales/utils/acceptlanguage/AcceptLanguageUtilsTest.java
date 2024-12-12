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

package com.spotify.i18n.locales.utils.acceptlanguage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class AcceptLanguageUtilsTest {

  @Test
  public void whenNormalizingNull_succeeds() {
    assertEquals("", AcceptLanguageUtils.normalize(null));
  }

  @ParameterizedTest
  @MethodSource
  public void whenNormalizingGivenValue_returnsExpectedOne(
      final String givenValue, final String expectedNormalizedValue) {
    assertEquals(expectedNormalizedValue, AcceptLanguageUtils.normalize(givenValue));
  }

  static Stream<Arguments> whenNormalizingGivenValue_returnsExpectedOne() {
    Map<String, String> m = new HashMap<>();
    m.put("", "");
    m.put("en", "en");
    m.put("fr-BE;q=0.1,    JA_jp, ZH_hk;q=0.5", "ja-jp,zh-hk;q=0.5,fr-be;q=0.1");
    m.put("fr-BE", "fr-be");
    m.put("fr_BE@calendar=gregorian", "fr-be");
    m.put("ZH_hk;q=0.5", "zh-hk;q=0.5");
    m.put("en_SG@calendar=buddhist", "en-sg");
    m.put(
        "JA_jp@calendar=buddhist-u-timezone-CET, FR_be;q=0.3,       ZH-u-coucou; q=0.2, fr-CA@calendar=gregorian     ",
        "ja-jp,fr-ca,fr-be;q=0.3,zh;q=0.2");
    // Negative q values are not authorized ... but we are accepting them and normalizing them to
    // zero.
    m.put("fr-BE ; q = -0.1", "fr-be;q=0.0");

    // This test is a bit nasty as he-SE will generate he-SE + iw-SE when processed by
    // LanguageRange, but iw-SE will be normalized as he-SE. We therefore get 2 entries for he-SE in
    // the normalized output.
    // https://docs.oracle.com/javase%2F8%2Fdocs%2Fapi%2F%2F/java/util/Locale.LanguageRange.html#parse-java.lang.String-
    m.put(
        "en-SE;q=1.000, he-SE;q=0.833, sV-SE;q=0.667, zh-Hans-SE;q=0.500, tlh-SE;q=0.333, en;q=0.167",
        "en-se,he-se;q=0.833,iw-se;q=0.833,sv-se;q=0.667,zh-hans-se;q=0.5,tlh-se;q=0.333,i-klingon-se;q=0.333,en;q=0.167");
    m.put("Ø", "");
    return m.entrySet().stream().map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
  }

  @Test
  public void whenParsingNullValue_returnsEmptyList() {
    assertThat(AcceptLanguageUtils.parse(null), is(Collections.emptyList()));
  }

  @ParameterizedTest
  @MethodSource
  public void whenParsingGivenValue_returnsExpectedList(
      final String givenValue, final List<LanguageRange> expectedList) {
    // LanguageRange comparison is based on the object hash code, which isn't reliable ... we need
    // to compare list entries one by one
    List<LanguageRange> actualList = AcceptLanguageUtils.parse(givenValue);
    assertEquals(actualList.size(), expectedList.size());

    for (int i = 0; i < actualList.size(); i++) {
      LanguageRange actualEntry = actualList.get(i);
      LanguageRange expectedEntry = expectedList.get(i);
      assertEquals(expectedEntry.getRange(), actualEntry.getRange());
      assertEquals(
          Double.valueOf(expectedEntry.getWeight()), Double.valueOf(actualEntry.getWeight()));
    }
  }

  static Stream<Arguments> whenParsingGivenValue_returnsExpectedList() {
    Map<String, List<LanguageRange>> m = new HashMap<>();
    m.put("", Collections.emptyList());
    m.put("en", List.of(lr("en", 1.0)));
    m.put(
        "fr-BE;q=0.1,    JA_jp, ZH_hk;q=0.5",
        List.of(lr("ja-JP", 1.0), lr("zh-HK", 0.5), lr("fr-BE", 0.1)));
    m.put("fr-BE", List.of(lr("fr-BE", 1.0)));
    // Duplicate entries
    m.put("fr-BE;q=1.0,fr-BE;q=0.4", List.of(lr("fr-BE", 1.0)));
    m.put("fr_BE@calendar=gregorian", List.of(lr("fr-BE", 1.0)));
    m.put("ZH_hk;q=0.5", List.of(lr("zh-HK", 0.5)));
    m.put("en_SG@calendar=buddhist", List.of(lr("en-SG", 1.0)));
    m.put(
        "JA_jp@calendar=buddhist-u-timezone-CET, FR_be;q=0.3,       ZH-u-coucou; q=0.2, fr-CA@calendar=gregorian     ",
        List.of(lr("ja-JP", 1.0), lr("fr-CA", 1.0), lr("fr-BE", 0.3), lr("zh", 0.2)));
    // Negative q values are not authorized ... but we are accepting them and normalizing them to
    // zero.
    m.put("fr-BE ; q= -0.1 ", List.of(lr("fr-BE", 0.0)));

    // This test is a bit nasty as he-SE will generate he-SE + iw-SE when processed by LanguageRange
    // https://docs.oracle.com/javase%2F8%2Fdocs%2Fapi%2F%2F/java/util/Locale.LanguageRange.html#parse-java.lang.String-
    m.put(
        "en-SE;q=1.000, he-SE;q=0.833, sV-SE;q=0.667, zh-Hans-SE;q=0.500, tlh-SE;q=0.333, en;q=0.167",
        List.of(
            lr("en-SE", 1.0),
            lr("he-SE", 0.833),
            lr("iw-SE", 0.833),
            lr("sv-SE", 0.667),
            lr("zh-Hans-SE", 0.5),
            lr("tlh-SE", 0.333),
            lr("i-klingon-SE", 0.333),
            lr("en", 0.167)));
    m.put("Ø", Collections.emptyList());
    return m.entrySet().stream().map(entry -> Arguments.of(entry.getKey(), entry.getValue()));
  }

  private static LanguageRange lr(String languageTag, double weight) {
    return new LanguageRange(languageTag, weight);
  }
}
