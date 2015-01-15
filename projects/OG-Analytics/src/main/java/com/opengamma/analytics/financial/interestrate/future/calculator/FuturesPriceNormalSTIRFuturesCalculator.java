/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionMarginSecurityNormalSmileMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Computes the price for different types of futures.
 */
public final class FuturesPriceNormalSTIRFuturesCalculator extends
    InstrumentDerivativeVisitorAdapter<NormalSTIRFuturesProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final FuturesPriceNormalSTIRFuturesCalculator INSTANCE = new FuturesPriceNormalSTIRFuturesCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPriceNormalSTIRFuturesCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FuturesPriceNormalSTIRFuturesCalculator() {
  }

  /** The Black function used in the pricing. */
  private static final InterestRateFutureOptionMarginSecurityNormalSmileMethod METHOD_RATE_FUTURE_OPTION = InterestRateFutureOptionMarginSecurityNormalSmileMethod
      .getInstance();

  //     -----     Futures options    -----

  @Override
  public Double visitInterestRateFutureOptionMarginSecurity(InterestRateFutureOptionMarginSecurity security,
      NormalSTIRFuturesProviderInterface normalData) {
    ArgumentChecker.notNull(security, "Option security");
    ArgumentChecker.notNull(normalData, "normal data");
    return METHOD_RATE_FUTURE_OPTION.price(security, normalData);
  }

  @Override
  public Double visitInterestRateFutureOptionMarginTransaction(InterestRateFutureOptionMarginTransaction option,
      NormalSTIRFuturesProviderInterface normalData) {
    return visitInterestRateFutureOptionMarginSecurity(option.getUnderlyingSecurity(), normalData);
  }
}
