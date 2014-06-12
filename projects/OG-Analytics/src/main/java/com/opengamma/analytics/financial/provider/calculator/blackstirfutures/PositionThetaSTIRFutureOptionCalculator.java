/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackstirfutures;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionMarginTransactionBlackSmileMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;

/**
 * Calculates the position theta for the interest rate future option.
 */
public final class PositionThetaSTIRFutureOptionCalculator extends InstrumentDerivativeVisitorAdapter<BlackSTIRFuturesProviderInterface, Double> {

  /**
   * The singleton.
   */
  private static final PositionThetaSTIRFutureOptionCalculator INSTANCE = new PositionThetaSTIRFutureOptionCalculator();
  
  /**
   * Gets the singleton of the calculator.
   * @return the calculator.
   */
  public static PositionThetaSTIRFutureOptionCalculator getInstance() {
    return INSTANCE;
  }
  
  /**
   * Singleton constructor.
   */
  private PositionThetaSTIRFutureOptionCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureOptionMarginTransactionBlackSmileMethod METHOD_STIR = InterestRateFutureOptionMarginTransactionBlackSmileMethod.getInstance();
  
  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginTransaction option, BlackSTIRFuturesProviderInterface data) {
    return METHOD_STIR.presentValueTheta(option, data);
  }
}
