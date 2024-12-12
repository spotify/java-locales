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

import com.spotify.i18n.locales.common.ContextBasedLocalesResolver;
import com.spotify.i18n.locales.common.impl.model.LocalesResolutionContext;
import com.spotify.i18n.locales.common.model.ResolvedLocale;
import com.spotify.i18n.locales.common.model.SupportedLocale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContextBasedLocalesResolverBaseImplTest {

  @Mock LocalesResolutionContext context;

  @Mock Function<Object, CompletionStage<Set<SupportedLocale>>> contextToSupportedLocalesMocked;
  @Mock Function<Object, CompletionStage<Optional<String>>> contextToAcceptLanguageMocked;
  @Mock Function<Object, CompletionStage<ResolvedLocale>> contextToDefaultResolvedLocaleMocked;

  @Test
  void whenBuildingWithMissingRequiredProperties_buildFails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () -> ContextBasedLocalesResolverBaseImpl.builder().build());

    assertEquals(
        thrown.getMessage(),
        "Missing required properties: contextToSupportedLocales contextToAcceptLanguage contextToDefaultResolvedLocale");
  }

  @Test
  void whenAllIsConfiguredProperly_allWorksAsExpected() {
    ContextBasedLocalesResolver<LocalesResolutionContext> resolver =
        ContextBasedLocalesResolverBaseImpl.builder()
            .contextToSupportedLocales(contextToSupportedLocalesMocked)
            .contextToAcceptLanguage(contextToAcceptLanguageMocked)
            .contextToDefaultResolvedLocale(contextToDefaultResolvedLocaleMocked)
            .build();

    Set<SupportedLocale> supportedLocales = Set.of(SupportedLocale.fromLanguageTag("en"));

    ResolvedLocale resolvedLocale = ResolvedLocale.fromLanguageTags("en", "en");

    when(contextToSupportedLocalesMocked.apply(any()))
        .thenReturn(CompletableFuture.completedFuture(supportedLocales));
    when(contextToAcceptLanguageMocked.apply(any()))
        .thenReturn(CompletableFuture.completedFuture(Optional.of("en")));
    when(contextToDefaultResolvedLocaleMocked.apply(any()))
        .thenReturn(CompletableFuture.completedFuture(resolvedLocale));

    assertThat(resolver.resolve(context), stageCompletedWithValueThat(is(resolvedLocale)));
  }
}
