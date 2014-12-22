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
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueCurveSensitivityNormalSTIRFuturesCalculator extends
    InstrumentDerivativeVisitorAdapter<NormalSTIRFuturesProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityNormalSTIRFuturesCalculator INSTANCE = new PresentValueCurveSensitivityNormalSTIRFuturesCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityNormalSTIRFuturesCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityNormalSTIRFuturesCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final InterestRateFutureOptionMarginTransactionNormalSmileMethod METHOD_STRIRFUT_MARGIN = InterestRateFutureOptionMarginTransactionNormalSmileMethod.getInstance();

  // -----     Futures     ------

  @Override
  public MultipleCurrencyMulticurveSensitivity visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction futures,
      final NormalSTIRFuturesProviderInterface normal) {
    return METHOD_STRIRFUT_MARGIN.presentValueCurveSensitivity(futures, normal);
  }

}
