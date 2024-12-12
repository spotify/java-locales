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

package com.spotify.i18n.locales.common;

import com.spotify.i18n.locales.common.model.ResolvedLocale;
import com.spotify.i18n.locales.common.model.SupportedLocale;
import java.util.concurrent.CompletionStage;

/**
 * Represents an asynchronous resolver of locale, for a given {@link CONTEXT}
 *
 * <p>This resolver will return the matching {@link ResolvedLocale}, with optional fallbacks, for a
 * given {@link CONTEXT}.
 *
 * @param <CONTEXT>
 * @see ResolvedLocale
 * @author Eric Fjøsne
 */
@FunctionalInterface
public interface ContextBasedLocalesResolver<CONTEXT> {

  /**
   * Returns a new CompletionStage that, when this stage completes normally, returns the matching
   * {@link SupportedLocale}s for the given {@link CONTEXT}.
   *
   * @param ctx the {@link CONTEXT}
   * @return the new CompletionStage that returns the supported locales
   */
  CompletionStage<ResolvedLocale> resolve(final CONTEXT ctx);
}
