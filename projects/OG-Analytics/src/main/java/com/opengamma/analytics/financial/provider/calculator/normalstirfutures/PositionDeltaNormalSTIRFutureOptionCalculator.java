/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.normalstirfutures;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionMarginTransactionNormalSmileMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesProviderInterface;

/**
 * Calculates the present value delta (first derivative of the present value with respect to the underlying future price) for interest rate
 * future options.
 */
public final class PositionDeltaNormalSTIRFutureOptionCalculator extends
    InstrumentDerivativeVisitorAdapter<NormalSTIRFuturesProviderInterface, Double> {
  /**
   * The unique instance of the calculator.
   */
  private static final PositionDeltaNormalSTIRFutureOptionCalculator INSTANCE = new PositionDeltaNormalSTIRFutureOptionCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PositionDeltaNormalSTIRFutureOptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PositionDeltaNormalSTIRFutureOptionCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureOptionMarginTransactionNormalSmileMethod METHOD_STIR = InterestRateFutureOptionMarginTransactionNormalSmileMethod
      .getInstance();

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginTransaction futures,
      NormalSTIRFuturesProviderInterface normal) {
    return METHOD_STIR.presentValueDelta(futures, normal);
  }
}
