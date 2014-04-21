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
public final class SwapFuturesPriceDeliverableTransactionHullWhiteMethod extends FuturesTransactionHullWhiteMethod {

  /**
   * The unique instance of the calculator.
   */
  private static final SwapFuturesPriceDeliverableTransactionHullWhiteMethod INSTANCE = new SwapFuturesPriceDeliverableTransactionHullWhiteMethod();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static SwapFuturesPriceDeliverableTransactionHullWhiteMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private SwapFuturesPriceDeliverableTransactionHullWhiteMethod() {
  }

}
