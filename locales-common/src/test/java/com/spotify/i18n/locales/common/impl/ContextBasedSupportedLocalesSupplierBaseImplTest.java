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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.spotify.i18n.locales.common.ContextBasedSupportedLocalesSupplier;
import com.spotify.i18n.locales.common.SupportedLocalesSupplier;
import com.spotify.i18n.locales.common.impl.model.LocalesResolutionContext;
import com.spotify.i18n.locales.common.impl.model.SupportedLocalesAudience;
import com.spotify.i18n.locales.common.model.SupportedLocale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContextBasedSupportedLocalesSupplierBaseImplTest {

  @Mock LocalesResolutionContext mockedContext;

  @Mock SupportedLocalesSupplier mockedLocalesSupplier;

  @Mock Function<SupportedLocalesAudience, SupportedLocalesSupplier> audienceToSupplierMocked;

  @Mock
  Function<LocalesResolutionContext, CompletionStage<SupportedLocalesAudience>>
      mockedContextToAudience;

  @Mock SupportedLocalesAudience mockedAudience;

  @Test
  void whenBuildingWithMissingRequiredProperties_buildFails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () -> ContextBasedSupportedLocalesSupplierBaseImpl.builder().build());

    assertEquals(
        "Missing required properties: audienceToSupplier contextToAudience", thrown.getMessage());
  }

  @Test
  void whenAllIsConfiguredProperly_theRightSuppliersAreInvoked() {

    Set<SupportedLocale> resultingSet =
        Set.of(SupportedLocale.fromLanguageTag("fr"), SupportedLocale.fromLanguageTag("en"));

    when(audienceToSupplierMocked.apply(mockedAudience)).thenReturn(mockedLocalesSupplier);

    when(mockedLocalesSupplier.get()).thenReturn(CompletableFuture.completedFuture(resultingSet));

    when(mockedContextToAudience.apply(any()))
        .thenReturn(CompletableFuture.completedFuture(mockedAudience));

    ContextBasedSupportedLocalesSupplier<LocalesResolutionContext> supplier =
        ContextBasedSupportedLocalesSupplierBaseImpl.builder()
            .audienceToSupplier(audienceToSupplierMocked)
            .contextToAudience(mockedContextToAudience)
            .build();

    assertThat(supplier.get(mockedContext), stageCompletedWithValueThat(is(resultingSet)));
  }
}
