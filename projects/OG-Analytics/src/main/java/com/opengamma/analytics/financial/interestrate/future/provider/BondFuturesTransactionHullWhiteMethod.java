/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

/**
 * Method to compute the bond futures transaction results with the price computed as the cheapest forward.
 */
public final class BondFuturesTransactionHullWhiteMethod extends FuturesTransactionHullWhiteIssuerMethod {

  /**
   * Creates the method unique instance.
   */
  private static final BondFuturesTransactionHullWhiteMethod INSTANCE = new BondFuturesTransactionHullWhiteMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static BondFuturesTransactionHullWhiteMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private BondFuturesTransactionHullWhiteMethod() {
  }

}
