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

package com.spotify.i18n.locales.common;

import com.spotify.i18n.locales.common.model.LocaleAffinityResult;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Represents an engine that calculates the locale affinity between two given language tags. All
 * implementations of this interface must return a non-null {@link LocaleAffinityResult}, even when
 * the given language tags are null or empty.
 *
 * @author Eric Fjøsne
 */
public interface LocaleAffinityBiCalculator {

  /**
   * Returns the calculated {@link LocaleAffinityResult} for the two given language tags
   *
   * @return the locale affinity result
   */
  LocaleAffinityResult calculate(
      @Nullable final String languageTag1, @Nullable final String languageTag2);
}
