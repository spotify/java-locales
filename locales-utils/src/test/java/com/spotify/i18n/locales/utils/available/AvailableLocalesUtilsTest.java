/*-
 * -\-\-
 * locales-utils
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

package com.spotify.i18n.locales.utils.available;

import static com.spotify.i18n.locales.utils.hierarchy.LocalesHierarchyUtils.isSameLocale;
import static org.junit.jupiter.api.Assertions.*;

import com.ibm.icu.util.ULocale;
import org.junit.jupiter.api.Test;

class AvailableLocalesUtilsTest {

  @Test
  void cldrLocalesDoNotContainRoot() {
    assertFalse(AvailableLocalesUtils.getCldrLocales().contains(ULocale.ROOT));
  }

  @Test
  void cldrLocalesDoNotContainEnUsPOSIX() {
    assertFalse(
        AvailableLocalesUtils.getCldrLocales().contains(ULocale.forLanguageTag("en-US-POSIX")));
  }

  @Test
  void referenceLocalesDoNotContainRoot() {
    assertFalse(AvailableLocalesUtils.getReferenceLocales().contains(ULocale.ROOT));
  }

  @Test
  void referenceLocalesDoNotContainEnUsPOSIX() {
    assertFalse(
        AvailableLocalesUtils.getReferenceLocales()
            .contains(ULocale.forLanguageTag("en-US-POSIX")));
  }

  @Test
  void whenGettingReferenceLocales_allAreMinimized() {
    for (ULocale referenceLocale : AvailableLocalesUtils.getReferenceLocales()) {
      assertTrue(isSameLocale(ULocale.minimizeSubtags(referenceLocale), referenceLocale));
    }
  }
}
