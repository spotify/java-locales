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

/**
 * Represents a factory of locales helpers.
 *
 * @param <CONTEXT> the type of the context based on which helpers created by this factory will
 *     operate.
 * @author Eric Fjøsne
 */
public interface LocalesHelpersFactory<CONTEXT> {

  /**
   * Returns a preconfigured, ready-to-use instance of {@link ContextBasedSupportedLocalesSupplier}.
   *
   * <p>This built supplier will return the matching {@link SupportedLocale}s for the given {@link
   * CONTEXT}.
   *
   * @return Pre configured supplier
   * @see SupportedLocale
   * @see ContextBasedSupportedLocalesSupplier
   */
  ContextBasedSupportedLocalesSupplier<CONTEXT> buildSupportedLocalesSupplier();

  /**
   * Returns a preconfigured, ready-to-use instance of {@link ContextBasedLocalesResolver}.
   *
   * <p>This resolver will return the matching {@link ResolvedLocale} for the given {@link CONTEXT}.
   *
   * @return Pre configured resolver
   * @see ContextBasedLocalesResolver
   */
  ContextBasedLocalesResolver<CONTEXT> buildLocalesResolver();
}
