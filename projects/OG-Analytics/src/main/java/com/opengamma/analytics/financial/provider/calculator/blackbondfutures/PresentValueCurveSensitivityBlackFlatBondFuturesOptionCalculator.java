/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackbondfutures;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.FuturesTransactionBlackFlatBondFuturesMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesFlatProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueCurveSensitivityBlackFlatBondFuturesOptionCalculator extends
    InstrumentDerivativeVisitorAdapter<BlackBondFuturesFlatProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityBlackFlatBondFuturesOptionCalculator INSTANCE = new PresentValueCurveSensitivityBlackFlatBondFuturesOptionCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityBlackFlatBondFuturesOptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityBlackFlatBondFuturesOptionCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final FuturesTransactionBlackFlatBondFuturesMethod METHOD_FUT = new FuturesTransactionBlackFlatBondFuturesMethod();

  // -----     Futures     ------

  @Override
  public MultipleCurrencyMulticurveSensitivity visitBondFuturesOptionMarginTransaction(final BondFuturesOptionMarginTransaction futures, final BlackBondFuturesFlatProviderInterface black) {
    return METHOD_FUT.presentValueCurveSensitivity(futures, black);
  }

}
