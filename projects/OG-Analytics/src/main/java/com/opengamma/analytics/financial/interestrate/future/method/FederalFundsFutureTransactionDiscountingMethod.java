/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;


/**
 * Methods for the pricing of Federal Funds futures transactions by discounting (no convexity adjustment).
 * @deprecated Use {@link com.opengamma.analytics.financial.interestrate.future.provider.FederalFundsFutureTransactionDiscountingMethod}
 */
@Deprecated
public final class FederalFundsFutureTransactionDiscountingMethod extends FederalFundsFutureTransactionMethod {

  /**
   * Creates the method unique instance.
   */
  private static final FederalFundsFutureTransactionDiscountingMethod INSTANCE = new FederalFundsFutureTransactionDiscountingMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static FederalFundsFutureTransactionDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FederalFundsFutureTransactionDiscountingMethod() {
    setMethodSecurity(FederalFundsFutureSecurityDiscountingMethod.getInstance());
  }

}
