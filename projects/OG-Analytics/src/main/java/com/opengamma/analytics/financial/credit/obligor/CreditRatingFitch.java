/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.obligor;

/**
 * Enumerate the possible credit rating states for a reference entity (Fitch rating classifications).
 * The ratings are long term.
 * <p>
 * This is an incomplete list of the long-term Fitch ratings classifications, but this enum is
 * deprecated and will not be completed. Some of the enum values have been remapped to their approximate
 * equivalents (e.g. CC &rarr CCC).
 * @deprecated Credit ratings have been promoted to objects. See {@link com.opengamma.analytics.financial.legalentity.CreditRating}
 */
@Deprecated
public enum CreditRatingFitch {
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
  CCC("CCC", "Extremely speculative"),
  /**
   * CC rating
   */
  CC("CCC", "Extremely speculative"),
  /**
   * C rating
   */
  C("CCC", "Extremely speculative"),
  /**
   * DDD; reference entity has already defaulted
   */
  DEFAULT("DDD", "In default"),
  /**
   * No rating
   */
  NR("NR", "No rating");

  /** Agency name */
  private static final String AGENCY = "Fitch";
  /** The rating */
  private final String _rating;
  /** The rating description */
  private final String _description;

  /**
   * @param rating The rating
   * @param description The rating description
   */
  private CreditRatingFitch(final String rating, final String description) {
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
