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

/**
 * An enum that represents the affinity between 2 locales.
 *
 * @author Eric Fjøsne
 */
public enum LocaleAffinity {

  /** Locales are totally unrelated */
  NONE,

  /**
   * Locales are somewhat related, meaning they either have low similarities from a linguistic
   * perspective or co-exist in given geopolitical or cultural contexts.
   */
  LOW,

  /** Locales are quite related, meaning they have similarities from a linguistic perspective. */
  HIGH,

  /**
   * Locales identify languages that are similar to a point where a person should understand both if
   * they understand one of them.
   */
  MUTUALLY_INTELLIGIBLE,

  /** Locales identify the same language. */
  SAME
}
