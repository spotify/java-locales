package com.spotify.i18n.locales.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.model.ReferenceLocale;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LocaleAffinityJoinUtilsTest {

  @ParameterizedTest
  @MethodSource
  public void getGettingCorrespondingReferenceLocales_returnsExpected(
      final String input, final Set<String> referenceLocales) {
    List<ReferenceLocale> correspondingReferenceLocales =
        LocaleAffinityJoinUtils.getCorrespondingReferenceLocales(input);
    assertFalse(correspondingReferenceLocales.stream().anyMatch(rl -> rl.affinityScore() == 0));

    assertEquals(referenceLocales.size(), correspondingReferenceLocales.size());
    assertTrue(
        correspondingReferenceLocales.stream()
            .map(ReferenceLocale::referenceLocale)
            .map(ULocale::toLanguageTag)
            .allMatch(referenceLocales::contains));
  }

  public static Stream<Arguments> getGettingCorrespondingReferenceLocales_returnsExpected() {
    return Stream.of(
        Arguments.of("da-SE", danish()),
        Arguments.of("de-NL", german()),
        Arguments.of("en-US", english()),
        Arguments.of("fr-BE", french()),
        Arguments.of("it-SE", italian()),
        Arguments.of("nb", norwegian()),
        Arguments.of("sr-RS", serbian()),
        Arguments.of("sv-Latn-SE", swedish()),
        Arguments.of("ZH_us", chineseTraditional()),
        Arguments.of("zh-Hant", chineseTraditional()),
        Arguments.of("zh-Hant-HK", chineseTraditional()),
        Arguments.of("zh-Hant-CN", chineseTraditional()));
  }

  @ParameterizedTest
  @MethodSource
  public void getBestMatchingReferenceLocale_returnsExpected(
      final String input, final String expectedLanguageTag) {
    assertThat(
        LocaleAffinityJoinUtils.getBestMatchingReferenceLocale(input),
        is(Optional.of(ULocale.forLanguageTag(expectedLanguageTag))));
  }

  public static Stream<Arguments> getBestMatchingReferenceLocale_returnsExpected() {
    return Stream.of(
        Arguments.of("ZH_us", "zh-TW"),
        Arguments.of("zh-Hant", "zh-TW"),
        Arguments.of("zh-Hant-HK", "zh-HK"),
        Arguments.of("zh-Hant-CN", "zh-TW"),
        Arguments.of("fr-Latn-FR", "fr"),
        Arguments.of("fr-BE", "fr-BE"),
        Arguments.of("sr-RS", "sr"),
        Arguments.of("en-CA", "en-CA"),
        Arguments.of("en-US", "en"));
  }

  private static Set<String> chineseTraditional() {
    return Set.of(
        // Traditional Chinese
        "zh-HK",
        "zh-TW",
        "zh-MO",
        // Cantonese
        "yue");
  }

  private static Set<String> danish() {
    return Set.of(
        // Danish
        "da",
        "da-GL",
        // Faroese
        "fo",
        "fo-DK",
        // Bokmål
        "nb",
        "nb-SJ",
        // Norwegian
        "no");
  }

  private static Set<String> english() {
    return Set.of(
        // Welsh
        "cy",
        // English
        "en",
        "en-001",
        "en-150",
        "en-AE",
        "en-AG",
        "en-AI",
        "en-AS",
        "en-AT",
        "en-AU",
        "en-BB",
        "en-BE",
        "en-BI",
        "en-BM",
        "en-BS",
        "en-BW",
        "en-BZ",
        "en-CA",
        "en-CC",
        "en-CH",
        "en-CK",
        "en-CM",
        "en-CX",
        "en-CY",
        "en-DE",
        "en-DG",
        "en-DK",
        "en-DM",
        "en-ER",
        "en-FI",
        "en-FJ",
        "en-FK",
        "en-FM",
        "en-GB",
        "en-GD",
        "en-GG",
        "en-GH",
        "en-GI",
        "en-GM",
        "en-GU",
        "en-GY",
        "en-HK",
        "en-ID",
        "en-IE",
        "en-IL",
        "en-IM",
        "en-IN",
        "en-IO",
        "en-JE",
        "en-JM",
        "en-KE",
        "en-KI",
        "en-KN",
        "en-KY",
        "en-LC",
        "en-LR",
        "en-LS",
        "en-MG",
        "en-MH",
        "en-MO",
        "en-MP",
        "en-MS",
        "en-MT",
        "en-MU",
        "en-MV",
        "en-MW",
        "en-MY",
        "en-NA",
        "en-NF",
        "en-NG",
        "en-NL",
        "en-NR",
        "en-NU",
        "en-NZ",
        "en-PG",
        "en-PH",
        "en-PK",
        "en-PN",
        "en-PR",
        "en-PW",
        "en-RW",
        "en-SB",
        "en-SC",
        "en-SD",
        "en-SE",
        "en-SG",
        "en-SH",
        "en-SI",
        "en-SL",
        "en-SS",
        "en-SX",
        "en-SZ",
        "en-TC",
        "en-TK",
        "en-TO",
        "en-TT",
        "en-TV",
        "en-TZ",
        "en-UG",
        "en-UM",
        "en-VC",
        "en-VG",
        "en-VI",
        "en-VU",
        "en-WS",
        "en-ZA",
        "en-ZM",
        "en-ZW",
        // Irish
        "ga",
        "ga-GB",
        // Scottish Gaelic
        "gd",
        // Hawaiian
        "haw",
        // Icelandic (!?) Might be a bug related to
        // https://github.com/unicode-org/cldr/blame/main/common/supplemental/languageInfo.xml#L80
        "is",
        // Maori
        "mi",
        // Nigerian Pidgin
        "pcm");
  }

  private static Set<String> french() {
    return Set.of(
        // Breton
        "br",
        // French
        "fr",
        "fr-BE",
        "fr-BF",
        "fr-BI",
        "fr-BJ",
        "fr-BL",
        "fr-CA",
        "fr-CD",
        "fr-CF",
        "fr-CG",
        "fr-CH",
        "fr-CI",
        "fr-CM",
        "fr-DJ",
        "fr-DZ",
        "fr-GA",
        "fr-GF",
        "fr-GN",
        "fr-GP",
        "fr-GQ",
        "fr-HT",
        "fr-KM",
        "fr-LU",
        "fr-MA",
        "fr-MC",
        "fr-MF",
        "fr-MG",
        "fr-ML",
        "fr-MQ",
        "fr-MR",
        "fr-MU",
        "fr-NC",
        "fr-NE",
        "fr-PF",
        "fr-PM",
        "fr-RE",
        "fr-RW",
        "fr-SC",
        "fr-SN",
        "fr-SY",
        "fr-TD",
        "fr-TG",
        "fr-TN",
        "fr-VU",
        "fr-WF",
        "fr-YT",
        // Occitan
        "oc",
        "oc-ES");
  }

  private static Set<String> german() {
    return Set.of(
        // German
        "de",
        "de-AT",
        "de-BE",
        "de-CH",
        "de-IT",
        "de-LI",
        "de-LU",
        // Swiss German
        "gsw",
        "gsw-FR",
        "gsw-LI",
        // Luxembourgian
        "lb",
        // Romansh
        "rm");
  }

  private static Set<String> italian() {
    return Set.of("it", "it-CH", "it-SM", "it-VA");
  }

  private static Set<String> norwegian() {
    return Set.of(
        // Danish
        "da",
        "da-GL",
        // Bokmål
        "nb",
        "nb-SJ",
        // Norwegian
        "no",
        // Nynorsk
        "nn");
  }

  private static Set<String> serbian() {
    return Set.of(
        "sr", "sr-BA", "sr-Cyrl-ME", "sr-Latn", "sr-Latn-BA", "sr-Latn-XK", "sr-ME", "sr-XK");
  }

  private static Set<String> swedish() {
    return Set.of("sv", "sv-AX", "sv-FI");
  }
}
