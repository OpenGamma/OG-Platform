/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.calculator;


/**
 * Calculator of the present value curve sensitivity as a multiple currency sensitivity object with Hull-White one factor model.
 */
public class PresentValueCurveSensitivityHullWhiteCalculator extends PresentValueCurveSensitivityMCSCalculator {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityHullWhiteCalculator s_instance = new PresentValueCurveSensitivityHullWhiteCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityHullWhiteCalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  public PresentValueCurveSensitivityHullWhiteCalculator() {
  }

  //  /**
  //   * The methods used by the different instruments.
  //   */
  //  private static final InterestRateFutureHullWhiteMethod METHOD_IR_FUTURES = InterestRateFutureHullWhiteMethod.getInstance();

  // -----     Futures     ------

  //  @Override
  //  public MultipleCurrencyInterestRateCurveSensitivity visitInterestRateFuture(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
  //    return MultipleCurrencyInterestRateCurveSensitivity.of(future.getCurrency(), METHOD_IR_FUTURES.presentValueCurveSensitivity(future, curves));
  //  }

}
