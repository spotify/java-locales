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

package com.spotify.i18n.locales.common.model;

import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isSameLocale;
import static org.junit.jupiter.api.Assertions.*;

import com.ibm.icu.util.ULocale;
import org.junit.jupiter.api.Test;

class ReferenceLocaleTest {

  @Test
  void whenBuildingWithMissingRequiredProperties_buildFails() {
    IllegalStateException thrown =
        assertThrows(IllegalStateException.class, () -> ReferenceLocale.builder().build());

    assertEquals("Missing required properties: locale affinity", thrown.getMessage());
  }

  @Test
  void availableReferenceLocalesDoesNotContainRoot() {
    assertFalse(ReferenceLocale.availableReferenceLocales().contains(ULocale.ROOT));
  }

  @Test
  void whenGettingAvailableReferenceLocales_allAreMinimized() {
    for (ULocale referenceLocale : ReferenceLocale.availableReferenceLocales()) {
      assertTrue(isSameLocale(ULocale.minimizeSubtags(referenceLocale), referenceLocale));
    }
  }

  @Test
  void whenBuildingWithInvalidLocale_buildFails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                ReferenceLocale.builder()
                    .affinity(LocaleAffinity.HIGH)
                    .locale(ULocale.forLanguageTag("zh-Hant-TW"))
                    .build());

    assertEquals(
        "Given parameter locale could not be matched with an available reference locale: zh-Hant-TW",
        thrown.getMessage());
  }

  @Test
  void whenBuildingWithValidParameters_buildSucceeds() {
    ReferenceLocale.builder()
        .affinity(LocaleAffinity.HIGH)
        .locale(ULocale.forLanguageTag("zh-TW"))
        .build();
  }
}
