/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackstirfutures;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionMarginTransactionBlackSmileMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesSmileProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueBlackSTIRFutureOptionCalculator extends InstrumentDerivativeVisitorAdapter<BlackSTIRFuturesSmileProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBlackSTIRFutureOptionCalculator INSTANCE = new PresentValueBlackSTIRFutureOptionCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueBlackSTIRFutureOptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueBlackSTIRFutureOptionCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureOptionMarginTransactionBlackSmileMethod METHOD_STIRFUT_MARGIN = InterestRateFutureOptionMarginTransactionBlackSmileMethod.getInstance();

  // -----     Futures     ------

  @Override
  public MultipleCurrencyAmount visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction futures, final BlackSTIRFuturesSmileProviderInterface black) {
    return METHOD_STIRFUT_MARGIN.presentValue(futures, black);
  }

}
