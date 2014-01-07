/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.obligor;

/**
 * Enumerate the possible credit rating states for a reference entity (Moody's rating classifications).
 * The ratings are long term.
 * <p>
 * This is an incomplete list of the long term Moody's rating classifications, but this enum is
 * deprecated and will not be completed. Some of the enum values have been remapped to their approximate
 * equivalents (e.g. AA &rarr Aa2).
 * @deprecated Credit ratings have been promoted to objects. See {@link com.opengamma.analytics.financial.legalentity.CreditRating}
 */
@Deprecated
public enum CreditRatingMoodys {
  /**
   * Aaa
   */
  AAA("Aaa", "Prime"),
  /**
   * Aa2
   */
  AA("Aa2", "High grade"),
  /**
   * A1
   */
  A("A1", "Upper medium grade"),
  /**
   * Baa2
   */
  BBB("Baa2", "Lower medium grade"),
  /**
   * Ba2
   */
  BB("Ba2", "Non-investment grade speculative"),
  /**
   * B2
   */
  B("B2", "Highly speculative"),
  /**
   * Caa2
   */
  CCC("Caa2", "Substantial risks"),
  /**
   * Ca
   */
  CC("Ca", "Extremely speculative"),
  /**
   * Ca
   */
  C("Ca", "Extremely speculative"),
  /**
   * C; reference entity has already defaulted
   */
  DEFAULT("C", "In default"),
  /**
   * No rating
   */
  NR("NR", "No rating");

  /** Agency name */
  private static final String AGENCY = "Moody's";
  /** The rating */
  private final String _rating;
  /** The rating description */
  private final String _description;

  /**
   * @param rating The rating
   * @param description The rating description
   */
  private CreditRatingMoodys(final String rating, final String description) {
    _rating = rating;
    _description = description;
  }

  /**
   * Gets the rating description.
   * @return The rating description
   */
  public String getRatingDescription() {
    return _description;
  }
  /**
   * Delegates to {@link com.opengamma.analytics.financial.legalentity.CreditRating}, with
   * the agency name set to Fitch and the rating assumed to be long-term.
   * @return A credit rating object
   */
  public com.opengamma.analytics.financial.legalentity.CreditRating toCreditRating() {
    return com.opengamma.analytics.financial.legalentity.CreditRating.of(_rating, _description, AGENCY, true);
  }

  @Override
  public String toString() {
    return _rating;
  }
}
