/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;


/**
 * Methods for the pricing of Federal Funds futures by discounting (using average of forward rates; not convexity adjustment).
 */
public final class FederalFundsFutureSecurityDiscountingMethod extends FuturesSecurityMulticurveMethod {

  /**
   * Creates the method unique instance.
   */
  private static final FederalFundsFutureSecurityDiscountingMethod INSTANCE = new FederalFundsFutureSecurityDiscountingMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static FederalFundsFutureSecurityDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FederalFundsFutureSecurityDiscountingMethod() {
  }

}
