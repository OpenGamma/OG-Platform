/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.normalstirfutures;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionMarginTransactionNormalSmileMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSTIRFuturesProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueNormalSTIRFuturesCalculator extends
    InstrumentDerivativeVisitorAdapter<NormalSTIRFuturesProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueNormalSTIRFuturesCalculator INSTANCE = new PresentValueNormalSTIRFuturesCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueNormalSTIRFuturesCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueNormalSTIRFuturesCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureOptionMarginTransactionNormalSmileMethod METHOD_STRIRFUT_MARGIN = InterestRateFutureOptionMarginTransactionNormalSmileMethod.getInstance();

  // -----     Futures     ------

  @Override
  public MultipleCurrencyAmount visitInterestRateFutureOptionMarginTransaction(
      final InterestRateFutureOptionMarginTransaction futures, final NormalSTIRFuturesProviderInterface black) {
    return METHOD_STRIRFUT_MARGIN.presentValue(futures, black);
  }

}
