package com.spotify.i18n.locales.common.model;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.ibm.icu.util.ULocale;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@AutoValue
public abstract class ReferenceLocale implements Comparable<ReferenceLocale> {

  public static final ULocale EN_US_POSIX = ULocale.forLanguageTag("en-US-POSIX");

  /**
   * Set containing all reference locales, which are all CLDR available {@link ULocale} without
   * duplicate entries.
   */
  private static final Set<ULocale> REFERENCE_LOCALES =
      Arrays.stream(ULocale.getAvailableLocales())
          .filter(l -> !l.equals(EN_US_POSIX))
          .map(ULocale::minimizeSubtags)
          .collect(Collectors.toSet());

  public static Set<ULocale> availableReferenceLocales() {
    return REFERENCE_LOCALES;
  }

  private static final int MAX_SCORE = 100;
  private static final int MIN_SCORE = 0;

  public abstract ULocale referenceLocale();

  public abstract int affinityScore();

  @Override
  public int compareTo(ReferenceLocale o) {
    return referenceLocale().compareTo(o.referenceLocale());
  }

  /**
   * Returns a {@link ReferenceLocale.Builder} instance that will allow you to manually create a
   * {@link ReferenceLocale} instance.
   *
   * @return The builder
   */
  public static ReferenceLocale.Builder builder() {
    return new AutoValue_ReferenceLocale.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    Builder() {} // package private constructor

    public abstract Builder affinityScore(final int affinityScore);

    public abstract Builder referenceLocale(final ULocale referenceLocale);

    abstract ReferenceLocale autoBuild(); // not public

    /**
     * Builds a {@link ReferenceLocale} out of this builder.
     *
     * <p>This is safe to be called several times on the same builder.
     *
     * @throws IllegalStateException if any of the builder property does not match the requirements.
     */
    public final ReferenceLocale build() {
      final ReferenceLocale mrl = autoBuild();
      final int score = mrl.affinityScore();
      Preconditions.checkState(
          score >= MIN_SCORE && score <= MAX_SCORE,
          String.format(
              "The affinity score must be between %d and %d. Provided: %d.",
              MIN_SCORE, MAX_SCORE, score));

      Preconditions.checkState(
          REFERENCE_LOCALES.contains(mrl.referenceLocale()),
          "Given parameter referenceLocale could not be matched with an available reference locale: %s",
          mrl.referenceLocale().toLanguageTag());

      return mrl;
    }
  }
}
