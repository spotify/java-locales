/*-
 * -\-\-
 * locales-common
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

package com.spotify.i18n.locales.common.impl;

import static com.spotify.hamcrest.future.CompletableFutureMatchers.stageCompletedWithValueThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.SupportedLocalesSupplier;
import com.spotify.i18n.locales.common.model.SupportedLocale;
import java.util.Set;
import java.util.stream.Collectors;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class SupportedLocalesSupplierBaseImplTest {

  @Test
  void whenBuildingWithMissingRequiredProperties_buildFails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class, () -> SupportedLocalesSupplierBaseImpl.builder().build());

    assertEquals(thrown.getMessage(), "Missing required properties: supportedLocales");
  }

  @Test
  void whenProvidedLocalesAreEmpty_buildFails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () -> SupportedLocalesSupplierBaseImpl.builder().supportedLocales(Set.of()).build());

    assertEquals(thrown.getMessage(), "There must be at least one supported locale.");
  }

  @Test
  void whenSetsAreCompliantWithRequirements_buildSucceeds() {
    SupportedLocale fr =
        SupportedLocale.builder()
            .localeForTranslations(ULocale.forLanguageTag("fr"))
            .relatedLocalesForFormatting(
                Set.of("fr", "fr-BE", "fr-FR", "fr-CA").stream()
                    .map(ULocale::forLanguageTag)
                    .collect(Collectors.toSet()))
            .build();

    SupportedLocale frCa =
        SupportedLocale.builder()
            .localeForTranslations(ULocale.forLanguageTag("fr-CA"))
            .relatedLocalesForFormatting(
                Set.of("fr-CA").stream().map(ULocale::forLanguageTag).collect(Collectors.toSet()))
            .build();
    SupportedLocalesSupplier localesSupplier =
        SupportedLocalesSupplierBaseImpl.builder().supportedLocales(Set.of(fr, frCa)).build();

    MatcherAssert.assertThat(
        localesSupplier.get(), stageCompletedWithValueThat(is(Set.of(frCa, fr))));
  }
}
