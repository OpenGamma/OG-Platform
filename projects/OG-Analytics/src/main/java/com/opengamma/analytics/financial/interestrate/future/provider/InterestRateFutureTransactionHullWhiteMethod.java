/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;


/**
 * Method to compute the price for an interest rate future with discounting (like a forward).
 * No convexity adjustment is done.
 */
public final class InterestRateFutureTransactionHullWhiteMethod extends FuturesTransactionHullWhiteMethod {

  /**
   * The unique instance of the calculator.
   */
  private static final InterestRateFutureTransactionHullWhiteMethod INSTANCE = new InterestRateFutureTransactionHullWhiteMethod();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static InterestRateFutureTransactionHullWhiteMethod getInstance() {
    return INSTANCE;
  }

}
