/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;


/**
 * Method to compute the bond futures security results with the price computed as the cheapest forward.
 */
public final class BondFuturesSecurityHullWhiteMethod extends FuturesSecurityHullWhiteIssuerMethod {

  /**
   * Creates the method unique instance.
   */
  private static final BondFuturesSecurityHullWhiteMethod INSTANCE = new BondFuturesSecurityHullWhiteMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static BondFuturesSecurityHullWhiteMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private BondFuturesSecurityHullWhiteMethod() {
  }

}
