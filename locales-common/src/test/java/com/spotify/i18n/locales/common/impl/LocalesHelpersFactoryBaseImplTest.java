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

import com.spotify.i18n.locales.common.ContextBasedLocalesResolver;
import com.spotify.i18n.locales.common.ContextBasedSupportedLocalesSupplier;
import com.spotify.i18n.locales.common.SupportedLocalesSupplier;
import com.spotify.i18n.locales.common.model.ResolvedLocale;
import com.spotify.i18n.locales.common.model.SupportedLocale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocalesHelpersFactoryBaseImplTest {

  private static class TestContext {
    public TestContext(TestAudience audience, Optional<String> acceptLanguage) {
      this.audience = audience;
      this.acceptLanguage = acceptLanguage;
    }

    final TestAudience audience;
    final Optional<String> acceptLanguage;
  }

  private enum TestAudience {
    DEFAULT,
    INTERNAL
  }

  private static final Function<TestContext, CompletionStage<TestAudience>> CONTEXT_TO_AUDIENCE =
      ctx -> CompletableFuture.completedFuture(ctx.audience);
  private static final Function<TestContext, CompletionStage<Optional<String>>>
      CONTEXT_TO_ACCEPT_LANGUAGE = ctx -> CompletableFuture.completedFuture(ctx.acceptLanguage);

  @Test
  void buildWithMissingProperties_fails() {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class, () -> LocalesHelpersFactoryBaseImpl.builder().build());

    assertEquals(
        "Missing required properties: contextToAudience audienceToSupportedLocalesSupplier contextToAcceptLanguage contextToDefaultResolvedLocale",
        thrown.getMessage());
  }

  @Test
  void whenBuiltWithValidProperties_builtSupplierWorks() {

    Set<SupportedLocale> defaultSupportedLocalesForTranslations =
        Set.of("en", "es", "fr", "zh-Hant").stream()
            .map(SupportedLocale::fromLanguageTag)
            .collect(Collectors.toSet());

    Set<SupportedLocale> internalSupportedLocalesForTranslations =
        Set.of("en", "es", "fr", "ja", "nl", "zh-Hant").stream()
            .map(SupportedLocale::fromLanguageTag)
            .collect(Collectors.toSet());

    SupportedLocalesSupplier supportedLocalesSupplierForDefaultAudience =
        SupportedLocalesSupplierBaseImpl.builder()
            .supportedLocales(defaultSupportedLocalesForTranslations)
            .build();

    SupportedLocalesSupplier supportedLocalesSupplierForInternalAudience =
        SupportedLocalesSupplierBaseImpl.builder()
            .supportedLocales(internalSupportedLocalesForTranslations)
            .build();

    Map<TestAudience, SupportedLocalesSupplier> supportedLocalesSupplierMap =
        Map.of(
            TestAudience.DEFAULT,
            supportedLocalesSupplierForDefaultAudience,
            TestAudience.INTERNAL,
            supportedLocalesSupplierForInternalAudience);

    Function<TestAudience, SupportedLocalesSupplier> audienceToSupportedLocalesSupplier =
        (audience) ->
            supportedLocalesSupplierMap.getOrDefault(
                audience, supportedLocalesSupplierForDefaultAudience);

    Function<TestContext, ResolvedLocale> contextToDefaultResolvedLocale =
        (context) -> ResolvedLocale.fromLanguageTags("en", "en");

    ContextBasedSupportedLocalesSupplier<TestContext> supplier =
        LocalesHelpersFactoryBaseImpl.builder()
            .audienceToSupportedLocalesSupplier(audienceToSupportedLocalesSupplier)
            .contextToDefaultResolvedLocale(contextToDefaultResolvedLocale)
            .contextToAudience(CONTEXT_TO_AUDIENCE)
            .contextToAcceptLanguage(CONTEXT_TO_ACCEPT_LANGUAGE)
            .build()
            .buildSupportedLocalesSupplier();

    assertThat(
        supplier.get(new TestContext(TestAudience.DEFAULT, Optional.empty())),
        stageCompletedWithValueThat(is(defaultSupportedLocalesForTranslations)));

    assertThat(
        supplier.get(new TestContext(TestAudience.INTERNAL, Optional.empty())),
        stageCompletedWithValueThat(is(internalSupportedLocalesForTranslations)));
  }

  @Test
  void localesResolverWithValidConfigurationInEnvironment_succeeds() {

    Set<SupportedLocale> defaultSupportedLocalesForTranslations =
        Set.of("en", "es", "fr", "zh-Hant").stream()
            .map(SupportedLocale::fromLanguageTag)
            .collect(Collectors.toSet());

    Set<SupportedLocale> internalSupportedLocalesForTranslations =
        Set.of("en", "es", "fr", "ja", "nl", "zh-Hant").stream()
            .map(SupportedLocale::fromLanguageTag)
            .collect(Collectors.toSet());

    SupportedLocalesSupplier supportedLocalesSupplierForDefaultAudience =
        SupportedLocalesSupplierBaseImpl.builder()
            .supportedLocales(defaultSupportedLocalesForTranslations)
            .build();

    SupportedLocalesSupplier supportedLocalesSupplierForInternalAudience =
        SupportedLocalesSupplierBaseImpl.builder()
            .supportedLocales(internalSupportedLocalesForTranslations)
            .build();

    Map<TestAudience, SupportedLocalesSupplier> supportedLocalesSupplierMap =
        Map.of(
            TestAudience.DEFAULT,
            supportedLocalesSupplierForDefaultAudience,
            TestAudience.INTERNAL,
            supportedLocalesSupplierForInternalAudience);

    Function<TestAudience, SupportedLocalesSupplier> audienceToSupportedLocalesSupplier =
        (audience) ->
            supportedLocalesSupplierMap.getOrDefault(
                audience, supportedLocalesSupplierForDefaultAudience);

    Function<TestContext, CompletionStage<ResolvedLocale>> contextToDefaultResolvedLocale =
        ctx -> CompletableFuture.completedFuture(ResolvedLocale.fromLanguageTags("en", "en"));

    ContextBasedLocalesResolver<TestContext> resolver =
        LocalesHelpersFactoryBaseImpl.builder()
            .audienceToSupportedLocalesSupplier(audienceToSupportedLocalesSupplier)
            .contextToDefaultResolvedLocale(contextToDefaultResolvedLocale)
            .contextToAudience(CONTEXT_TO_AUDIENCE)
            .contextToAcceptLanguage(CONTEXT_TO_ACCEPT_LANGUAGE)
            .build()
            .buildLocalesResolver();

    assertThat(
        resolver.resolve(new TestContext(TestAudience.DEFAULT, Optional.of("zh_US"))),
        stageCompletedWithValueThat(is(ResolvedLocale.fromLanguageTags("zh-Hant", "zh-Hant-TW"))));

    assertThat(
        resolver.resolve(new TestContext(TestAudience.DEFAULT, Optional.of("nl-BE"))),
        stageCompletedWithValueThat(is(ResolvedLocale.fromLanguageTags("en", "en-BE"))));

    assertThat(
        resolver.resolve(new TestContext(TestAudience.INTERNAL, Optional.of("zh_US"))),
        stageCompletedWithValueThat(is(ResolvedLocale.fromLanguageTags("zh-Hant", "zh-Hant-TW"))));

    assertThat(
        resolver.resolve(new TestContext(TestAudience.INTERNAL, Optional.of("nl-BE"))),
        stageCompletedWithValueThat(is(ResolvedLocale.fromLanguageTags("nl", "nl-BE"))));

    assertThat(
        resolver.resolve(new TestContext(TestAudience.INTERNAL, Optional.of("ko-KR"))),
        stageCompletedWithValueThat(is(ResolvedLocale.fromLanguageTags("en", "en-GB"))));
  }

  @Test
  void whenResolvingObsoleteLocalesIdentifiers_resolverWorks() {
    SupportedLocalesSupplier supportedLocalesSupplier =
        SupportedLocalesSupplierBaseImpl.builder()
            .supportedLocales(
                Set.of("en", "es", "fr", "he", "id", "yi", "zh-Hant").stream()
                    .map(SupportedLocale::fromLanguageTag)
                    .collect(Collectors.toSet()))
            .build();

    // Ensure that mitigation for changed locales identifiers between Java 11 and Java 17 is in
    // place. Read more about it: https://bugs.openjdk.org/browse/JDK-8263202
    Function<String, CompletionStage<TestAudience>> contextToDefaultAudience =
        ctx -> CompletableFuture.completedFuture(TestAudience.DEFAULT);
    Function<String, CompletionStage<Optional<String>>> contextToAcceptLanguage =
        ctx -> CompletableFuture.completedFuture(Optional.of(ctx));
    Function<String, CompletionStage<ResolvedLocale>> contextToDefaultResolvedLocale =
        ctx -> CompletableFuture.completedFuture(ResolvedLocale.fromLanguageTags("en", "en"));

    final ContextBasedLocalesResolver<String> resolver =
        LocalesHelpersFactoryBaseImpl.builder()
            .audienceToSupportedLocalesSupplier((audience) -> supportedLocalesSupplier)
            .contextToDefaultResolvedLocale(contextToDefaultResolvedLocale)
            .contextToAudience(contextToDefaultAudience)
            .contextToAcceptLanguage(contextToAcceptLanguage)
            .build()
            .buildLocalesResolver();

    // Hebrew
    final ResolvedLocale resolvedLocaleForHebrew = ResolvedLocale.fromLanguageTags("he", "he");
    assertThat(
        resolver.resolve("iw"), // obsolete identifier
        stageCompletedWithValueThat(is(resolvedLocaleForHebrew)));
    assertThat(
        resolver.resolve("he"), // current identifier
        stageCompletedWithValueThat(is(resolvedLocaleForHebrew)));

    // Indonesian
    final ResolvedLocale resolvedLocaleForIndonesian = ResolvedLocale.fromLanguageTags("id", "id");
    assertThat(
        resolver.resolve("in"), // obsolete identifier
        stageCompletedWithValueThat(is(resolvedLocaleForIndonesian)));
    assertThat(
        resolver.resolve("id"), // current identifier
        stageCompletedWithValueThat(is(resolvedLocaleForIndonesian)));

    // Yiddish (although unsupported at this stage, we add this here as fail-safe for later)
    final ResolvedLocale resolvedLocaleForYiddish = ResolvedLocale.fromLanguageTags("yi", "yi");
    assertThat(
        resolver.resolve("yi"), // obsolete identifier
        stageCompletedWithValueThat(is(resolvedLocaleForYiddish)));
    assertThat(
        resolver.resolve("ji"), // current identifier
        stageCompletedWithValueThat(is(resolvedLocaleForYiddish)));
  }
}
