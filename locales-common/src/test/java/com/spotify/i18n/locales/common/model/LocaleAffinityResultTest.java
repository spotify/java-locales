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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LocaleAffinityResultTest {

  @Test
  void whenBuildingWithMissingRequiredProperties_buildFails() {
    IllegalStateException thrown =
        assertThrows(IllegalStateException.class, () -> LocaleAffinityResult.builder().build());

    assertEquals("Missing required properties: affinityScore", thrown.getMessage());
  }

  @ParameterizedTest
  @ValueSource(ints = {-128, -4, -1, 101, 10348})
  void whenProvidedScoreIsInvalid_buildFails(int invalidScore) {
    IllegalStateException thrown =
        assertThrows(
            IllegalStateException.class,
            () -> LocaleAffinityResult.builder().affinityScore(invalidScore).build());

    assertEquals(
        String.format("The affinity score must be between 0 and 100. Provided: %d.", invalidScore),
        thrown.getMessage());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 42, 100})
  void whenProvidedScoreIsValid_succeeds(int validScore) {
    assertEquals(
        validScore,
        LocaleAffinityResult.builder().affinityScore(validScore).build().affinityScore());
  }
}
