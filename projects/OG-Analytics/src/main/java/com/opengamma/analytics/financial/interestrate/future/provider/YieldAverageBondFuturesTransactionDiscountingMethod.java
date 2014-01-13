/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

/**
 * Pricing method for bond futures transactions with settlement based on an average yield (used in particular for SFE-AUD bond futures).
 */
public final class YieldAverageBondFuturesTransactionDiscountingMethod extends FuturesTransactionMethod {

  /**
   * Creates the method unique instance.
   */
  private static final YieldAverageBondFuturesTransactionDiscountingMethod INSTANCE = new YieldAverageBondFuturesTransactionDiscountingMethod();

  /**
   * Return the method unique instance.
   * @return The instance.
   */
  public static YieldAverageBondFuturesTransactionDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private YieldAverageBondFuturesTransactionDiscountingMethod() {
    super(YieldAverageBondFuturesSecurityDiscountingMethod.getInstance());
  }
  
}
