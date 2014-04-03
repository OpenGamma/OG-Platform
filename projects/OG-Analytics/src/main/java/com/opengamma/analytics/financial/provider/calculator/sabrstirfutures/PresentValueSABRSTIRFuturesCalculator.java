/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.sabrstirfutures;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionMarginTransactionSABRMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSTIRFuturesProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueSABRSTIRFuturesCalculator extends InstrumentDerivativeVisitorAdapter<SABRSTIRFuturesProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueSABRSTIRFuturesCalculator INSTANCE = new PresentValueSABRSTIRFuturesCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueSABRSTIRFuturesCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueSABRSTIRFuturesCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureOptionMarginTransactionSABRMethod METHOD_STRIRFUT_MARGIN = InterestRateFutureOptionMarginTransactionSABRMethod.getInstance();

  // -----     Futures     ------

  @Override
  public MultipleCurrencyAmount visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction futures, final SABRSTIRFuturesProviderInterface sabr) {
    return METHOD_STRIRFUT_MARGIN.presentValue(futures, sabr);
  }

}
