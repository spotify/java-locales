package com.spotify.i18n.locales.common;

import com.ibm.icu.util.ULocale;
import com.spotify.i18n.locales.common.model.RelatedReferenceLocale;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class JoinTest {
  @Test
  void testJoin() {
    RelatedReferenceLocalesCalculator calculator =
        LocaleAffinityHelpersFactory.getDefaultInstance().buildRelatedReferenceLocalesCalculator();

    String userLanguage = "zh-HK";
    String contentLanguage = "zh-Hant-CN";

    List<RelatedReferenceLocale> relatedReferenceLocales =
        calculator.getRelatedReferenceLocales(userLanguage);
    Optional<ULocale> referenceLocales = calculator.getBestMatchingReferenceLocale(contentLanguage);

    if (referenceLocales.isPresent()) {
      relatedReferenceLocales.stream()
          .filter(rrl -> rrl.referenceLocale().equals(referenceLocales.get()))
          .forEach(rrl -> System.out.println(rrl));
    } else {
      // Nothing to join
    }
  }
}
