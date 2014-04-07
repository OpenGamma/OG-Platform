/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackbondfutures;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.FuturesTransactionBlackBondFuturesMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueCurveSensitivityBlackBondFuturesOptionCalculator extends
    InstrumentDerivativeVisitorAdapter<BlackBondFuturesProviderInterface, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueCurveSensitivityBlackBondFuturesOptionCalculator INSTANCE = new PresentValueCurveSensitivityBlackBondFuturesOptionCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueCurveSensitivityBlackBondFuturesOptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueCurveSensitivityBlackBondFuturesOptionCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final FuturesTransactionBlackBondFuturesMethod METHOD_FUT = new FuturesTransactionBlackBondFuturesMethod();

  // -----     Futures     ------

  @Override
  public MultipleCurrencyMulticurveSensitivity visitBondFuturesOptionMarginTransaction(final BondFuturesOptionMarginTransaction futures, final BlackBondFuturesProviderInterface black) {
    return METHOD_FUT.presentValueCurveSensitivity(futures, black);
  }

}
