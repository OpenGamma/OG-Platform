/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.obligor;

/**
 * Enumerate the possible credit rating states for a reference entity (Standard & Poor's rating classifications).
 * The ratings are long term.
 * <p>
 * This is an incomplete list of the long-term S&P ratings classifications, but this enum is deprecated
 * and will not be completed. The DEFAULT value has been mapped to D.
 * @deprecated Credit ratings have been promoted to objects. See {@link com.opengamma.analytics.financial.legalentity.CreditRating}
 */
@Deprecated
public enum CreditRatingStandardAndPoors {
  /**
   * AAA rating
   */
  AAA("AAA", "Prime"),
  /**
   * AA rating
   */
  AA("AA", "High grade"),
  /**
   * A rating
   */
  A("A", "Upper medium grade"),
  /**
   * BBB rating
   */
  BBB("BBB", "Lower medium grade"),
  /**
   * BB rating
   */
  BB("BB", "Non-investment grade speculative"),
  /**
   * B rating
   */
  B("B", "Highly speculative"),
  /**
   * CCC rating
   */
  CCC("CCC", "Substantial risks"),
  /**
   * CC rating
   */
  CC("CC", "Extremely speculative"),
  /**
   * C rating
   */
  C("C", "Extremely speculative"),
  /**
   * D; reference entity has already defaulted
   */
  DEFAULT("D", "In default"),
  /**
   * No rating
   */
  NR("NR", "No rating");

  /** Agency name */
  private static final String AGENCY = "S&P";
  /** The rating */
  private final String _rating;
  /** The rating description */
  private final String _description;

  /**
   * @param rating The rating
   * @param description The rating description
   */
  private CreditRatingStandardAndPoors(final String rating, final String description) {
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
