/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.calculator;


/**
 * Calculator of the present value as a multiple currency amount with Hull-White one factor model.
 */
public class PresentValueHullWhiteCalculator extends PresentValueMCACalculator {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueHullWhiteCalculator s_instance = new PresentValueHullWhiteCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueHullWhiteCalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  public PresentValueHullWhiteCalculator() {
  }

  //  /**
  //   * The methods used by the different instruments.
  //   */
  //  private static final InterestRateFutureHullWhiteMethod METHOD_IR_FUTURES = InterestRateFutureHullWhiteMethod.getInstance();
  //
  //  // -----     Futures     ------
  //
  //  @Override
  //  public MultipleCurrencyAmount visitInterestRateFuture(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
  //    return MultipleCurrencyAmount.of(METHOD_IR_FUTURES.presentValue(future, curves));
  //  }

}
