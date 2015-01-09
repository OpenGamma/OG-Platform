/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.normalstirfutures;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionMarginSecurityNormalSmileMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesProviderInterface;

/**
 * Calculates the theta (the first derivative of the price with respect to time) for interest rate
 * future options in normal model.
 */
public final class ThetaNormalSTIRFutureOptionCalculator extends
    InstrumentDerivativeVisitorAdapter<NormalSTIRFuturesProviderInterface, Double> {
  /**
   * The unique instance of the calculator.
   */
  private static final ThetaNormalSTIRFutureOptionCalculator INSTANCE = new ThetaNormalSTIRFutureOptionCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ThetaNormalSTIRFutureOptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ThetaNormalSTIRFutureOptionCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureOptionMarginSecurityNormalSmileMethod METHOD_STIR_NORMAL = InterestRateFutureOptionMarginSecurityNormalSmileMethod
      .getInstance();

  @Override
  public Double visitInterestRateFutureOptionMarginSecurity(InterestRateFutureOptionMarginSecurity futures,
      NormalSTIRFuturesProviderInterface normal) {
    return METHOD_STIR_NORMAL.priceTheta(futures, normal);
  }

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginTransaction futures,
      NormalSTIRFuturesProviderInterface normal) {
    return METHOD_STIR_NORMAL.priceTheta(futures.getUnderlyingSecurity(), normal);
  }
}
