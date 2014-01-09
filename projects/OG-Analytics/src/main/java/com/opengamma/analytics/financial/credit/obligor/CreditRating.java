/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.obligor;

/**
 * Enumerate the possible credit rating states for a reference entity (generic rating classifications).
 * The ratings are long term.
 * @deprecated Credit ratings have been promoted to objects. See {@link com.opengamma.analytics.financial.legalentity.CreditRating}
 */
@Deprecated
public enum CreditRating {
  /**
   * AAA rating
   */
  AAA,
  /**
   * AA rating
   */
  AA,
  /**
   * A rating
   */
  A,
  /**
   * BBB rating
   */
  BBB,
  /**
   * BB rating
   */
  BB,
  /**
   * B rating
   */
  B,
  /**
   * CCC rating
   */
  CCC,
  /**
   * CC rating
   */
  CC,
  /**
   * C rating
   */
  C,
  /**
   * Reference entity has already defaulted
   */
  DEFAULT,
  /**
   * No rating
   */
  NR;

  /** Agency name */
  private static final String AGENCY = "GENERIC";

  /**
   * Delegates to {@link com.opengamma.analytics.financial.legalentity.CreditRating}, with
   * the agency name set to GENERIC and the rating assumed to be long-term. The description
   * field is not set.
   * @return A credit rating object
   */
  public com.opengamma.analytics.financial.legalentity.CreditRating toCreditRating() {
    return com.opengamma.analytics.financial.legalentity.CreditRating.of(name(), AGENCY, true);
  }
}
