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

/**
 * Represents a resolver of locales. All implementations of this interface must return a non-null
 * {@link ResolvedLocale}, even when the given input is null or empty.
 *
 * @author Eric Fjøsne
 */
@FunctionalInterface
public interface LocalesResolver {

  /**
   * Returns the {@link ResolvedLocale} for the given input
   *
   * @return the resolved locale
   */
  ResolvedLocale resolve(final String input);
}
