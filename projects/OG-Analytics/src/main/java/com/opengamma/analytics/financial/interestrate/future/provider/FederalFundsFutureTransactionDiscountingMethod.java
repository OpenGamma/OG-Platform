/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;


/**
 * Methods for the pricing of Federal Funds futures transactions by discounting (no convexity adjustment).
 */
public final class FederalFundsFutureTransactionDiscountingMethod extends FuturesTransactionMulticurveMethod {

  /**
   * Creates the method unique instance.
   */
  private static final FederalFundsFutureTransactionDiscountingMethod INSTANCE = new FederalFundsFutureTransactionDiscountingMethod();

  /**
   * Constructor.
   */
  private FederalFundsFutureTransactionDiscountingMethod() {
  }

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static FederalFundsFutureTransactionDiscountingMethod getInstance() {
    return INSTANCE;
  }

}
