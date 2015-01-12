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
 * Calculates the gamma (second derivative of the price with respect to the underlying future price) for interest
 * rate future options in normal model.
 */
public final class GammaNormalSTIRFutureOptionCalculator extends
    InstrumentDerivativeVisitorAdapter<NormalSTIRFuturesProviderInterface, Double> {
  /**
   * The unique instance of the calculator.
   */
  private static final GammaNormalSTIRFutureOptionCalculator INSTANCE = new GammaNormalSTIRFutureOptionCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static GammaNormalSTIRFutureOptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor. 
   */
  private GammaNormalSTIRFutureOptionCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureOptionMarginSecurityNormalSmileMethod METHOD_STIR_NORMAL = InterestRateFutureOptionMarginSecurityNormalSmileMethod
      .getInstance();

  @Override
  public Double visitInterestRateFutureOptionMarginSecurity(InterestRateFutureOptionMarginSecurity futures,
      NormalSTIRFuturesProviderInterface normal) {
    return METHOD_STIR_NORMAL.priceGamma(futures, normal);
  }

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginTransaction futures,
      NormalSTIRFuturesProviderInterface normal) {
    return METHOD_STIR_NORMAL.priceGamma(futures.getUnderlyingSecurity(), normal);
  }
}
