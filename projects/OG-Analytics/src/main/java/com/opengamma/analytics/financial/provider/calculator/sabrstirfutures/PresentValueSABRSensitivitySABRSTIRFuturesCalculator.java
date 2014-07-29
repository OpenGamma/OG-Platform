/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.sabrstirfutures;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureOptionMarginTransactionSABRMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSTIRFuturesProviderInterface;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueSABRSensitivitySABRSTIRFuturesCalculator extends InstrumentDerivativeVisitorAdapter<SABRSTIRFuturesProviderInterface, PresentValueSABRSensitivityDataBundle> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueSABRSensitivitySABRSTIRFuturesCalculator INSTANCE = new PresentValueSABRSensitivitySABRSTIRFuturesCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueSABRSensitivitySABRSTIRFuturesCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueSABRSensitivitySABRSTIRFuturesCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureOptionMarginTransactionSABRMethod METHOD_STRIRFUT_MARGIN = InterestRateFutureOptionMarginTransactionSABRMethod.getInstance();

  // -----     Futures     ------

  @Override
  public PresentValueSABRSensitivityDataBundle visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction futures, final SABRSTIRFuturesProviderInterface sabr) {
    return METHOD_STRIRFUT_MARGIN.presentValueSABRSensitivity(futures, sabr);
  }

}
