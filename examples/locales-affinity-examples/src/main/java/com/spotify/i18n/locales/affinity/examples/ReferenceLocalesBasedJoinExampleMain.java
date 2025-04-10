package com.spotify.i18n.locales.affinity.examples;

import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.LocaleAffinityHelpersFactory;
import com.spotify.i18n.locales.common.ReferenceLocalesCalculator;
import com.spotify.i18n.locales.common.model.RelatedReferenceLocale;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Showcase implementation of Java-locales affinity calculation
 *
 * @author Eric Fjøsne
 */
public class ReferenceLocalesBasedJoinExampleMain {

  /** Create a {@link ReferenceLocalesCalculator} instance out of the factory */
  private static final ReferenceLocalesCalculator REFERENCE_LOCALES_CALCULATOR =
      LocaleAffinityHelpersFactory.getDefaultInstance().buildRelatedReferenceLocalesCalculator();

  /**
   * Example logic which attempts to join 2 sets of language tags.
   *
   * <p>Possible joins in the execution output are:
   *
   * <ul>
   *   <li>(de, de-AT), join on reference locale [de-AT] with SAME_OR_INTERCHANGEABLE affinity
   *   <li>(en-GB, en-JP), join on reference locale [en-GB] with SAME_OR_INTERCHANGEABLE affinity
   *   <li>(en-GB, en-SE), join on reference locale [en-SE] with SAME_OR_INTERCHANGEABLE affinity
   *   <li>(es-BE, ca), join on reference locale [ca] with LOW affinity
   *   <li>(fr-SE, fr-BE-u-ca-gregorian), join on reference locale [fr-BE] with
   *       SAME_OR_INTERCHANGEABLE affinity
   *   <li>(fr-SE, fr-CA), join on reference locale [fr-CA] with SAME_OR_INTERCHANGEABLE affinity
   *   <li>(ja-IT, ja@calendar=buddhist), join on reference locale [ja] with SAME_OR_INTERCHANGEABLE
   *       affinity
   *   <li>(nl-BE, nl-ZA), join on reference locale [nl] with SAME_OR_INTERCHANGEABLE affinity
   *   <li>(zh-Hans-US, zh-CN), join on reference locale [zh] with SAME_OR_INTERCHANGEABLE affinity
   * </ul>
   *
   * @param args
   */
  public static void main(String[] args) {
    final List<String> languageTagsInOriginDataset =
        List.of(
            "de", // German
            "en-GB", // English (Great-Britain)
            "es-BE", // Spanish (Belgium)
            "fr-SE", // French (Sweden)
            "it-CH", // Italian (Switzerland)
            "ja-IT", // Japanese (Italy)
            "nl-BE", // Dutch (Belgium)
            "zh-Hans-US", // Chinese (Simplified) (USA)
            "zh-HK" // Chinese (Hong-Kong)
            );
    final List<String> languageTagsInTargetDataset =
        List.of(
            "ca", // Catalan
            "de-AT", // German (Austria)
            "en-JP", // English (Japan)
            "en-SE", // English (Sweden)
            "fr-BE-u-ca-gregorian", // French (Belgium), with gregorian calendar extension
            "fr-CA", // French (Canada)
            "ja@calendar=buddhist", // Japanese, with buddhist calendar extension
            "nl-ZA", // Dutch (South Africa)
            "pt-US", // Portuguese (USA)
            "zh-CN" // Chinese (Mainland China)
            );

    // Iterate through all possible combinations
    for (String languageTagInOriginDataset : languageTagsInOriginDataset) {
      for (String languageTagInTargetDataset : languageTagsInTargetDataset) {
        // Retrieve the optional related reference locale based on which a join operation can be
        // performed, and display the outcome in the execution output.
        getRelatedReferenceLocaleForJoin(languageTagInOriginDataset, languageTagInTargetDataset)
            .ifPresentOrElse(
                (rrl) ->
                    System.out.println(
                        String.format(
                            "(%s, %s), join on reference locale [%s] with %s affinity",
                            languageTagInOriginDataset,
                            languageTagInTargetDataset,
                            rrl.referenceLocale().toLanguageTag(),
                            rrl.affinity())),
                () ->
                    System.out.println(
                        String.format(
                            "(%s, %s), no join possible.",
                            languageTagInOriginDataset, languageTagInTargetDataset)));
      }
    }
  }

  /**
   * Returns the optional {@link RelatedReferenceLocale} based on which the 2 language tags can be
   * joined together.
   */
  public static Optional<RelatedReferenceLocale> getRelatedReferenceLocaleForJoin(
      final String languageTagInOriginDataset, final String languageTagInTargetDataset) {
    Optional<ULocale> bestMatchingReferenceLocale =
        REFERENCE_LOCALES_CALCULATOR.calculateBestMatchingReferenceLocale(
            languageTagInTargetDataset);
    if (bestMatchingReferenceLocale.isEmpty()) {
      return Optional.empty();
    } else {
      List<RelatedReferenceLocale> relatedReferenceLocales =
          REFERENCE_LOCALES_CALCULATOR
              .calculateRelatedReferenceLocales(languageTagInOriginDataset)
              .stream()
              .collect(Collectors.toList());
      return relatedReferenceLocales.stream()
          .filter(rrl -> rrl.referenceLocale().equals(bestMatchingReferenceLocale.get()))
          .findFirst();
    }
  }
}
