/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackstirfutures;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionMarginTransactionBlackSmileMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;
import com.opengamma.analytics.util.amount.SurfaceValue;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueBlackSensitivityBlackSTIRFutureOptionCalculator extends InstrumentDerivativeVisitorAdapter<BlackSTIRFuturesProviderInterface, SurfaceValue> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBlackSensitivityBlackSTIRFutureOptionCalculator INSTANCE = new PresentValueBlackSensitivityBlackSTIRFutureOptionCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueBlackSensitivityBlackSTIRFutureOptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueBlackSensitivityBlackSTIRFutureOptionCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureOptionMarginTransactionBlackSmileMethod METHOD_STIRFUT_MARGIN = InterestRateFutureOptionMarginTransactionBlackSmileMethod.getInstance();

  // -----     Futures     ------

  @Override
  public SurfaceValue visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction futures, final BlackSTIRFuturesProviderInterface black) {
    return METHOD_STIRFUT_MARGIN.presentValueBlackSensitivity(futures, black);
  }
}
