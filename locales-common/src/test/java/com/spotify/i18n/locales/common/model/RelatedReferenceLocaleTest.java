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

import static org.junit.jupiter.api.Assertions.*;

import com.ibm.icu.util.ULocale;
import org.junit.jupiter.api.Test;

class RelatedReferenceLocaleTest {

  @Test
  void whenBuildingWithMissingRequiredProperties_buildFails() {
    IllegalStateException thrown =
        assertThrows(IllegalStateException.class, () -> RelatedReferenceLocale.builder().build());

    assertEquals("Missing required properties: referenceLocale affinity", thrown.getMessage());
  }

  @Test
  void whenBuildingWithInvalidReferenceLocale_buildFails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () ->
                RelatedReferenceLocale.builder()
                    .affinity(LocaleAffinity.HIGH)
                    .referenceLocale(ULocale.forLanguageTag("zh-Hant-TW"))
                    .build());

    assertEquals(
        "Given parameter locale could not be matched with an available reference locale: zh-Hant-TW",
        thrown.getMessage());
  }

  @Test
  void whenBuildingWithValidParameters_buildSucceeds() {
    RelatedReferenceLocale.builder()
        .affinity(LocaleAffinity.HIGH)
        .referenceLocale(ULocale.forLanguageTag("zh-TW"))
        .build();
  }
}
