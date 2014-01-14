/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.singlevalue;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Computes the par rate for different instrument. The meaning of "par rate" is instrument dependent.
 */
public final class FuturesPVCurveSensitivityFromPriceCurveSensitivityCalculator extends InstrumentDerivativeVisitorAdapter<MulticurveSensitivity, MultipleCurrencyMulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final FuturesPVCurveSensitivityFromPriceCurveSensitivityCalculator INSTANCE = new FuturesPVCurveSensitivityFromPriceCurveSensitivityCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPVCurveSensitivityFromPriceCurveSensitivityCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FuturesPVCurveSensitivityFromPriceCurveSensitivityCalculator() {
  }

  //     -----     Futures     -----

  @Override
  public MultipleCurrencyMulticurveSensitivity visitBondFuturesTransaction(final BondFuturesTransaction futures, final MulticurveSensitivity priceSensitivity) {
    return MultipleCurrencyMulticurveSensitivity.of(futures.getUnderlyingFuture().getCurrency(),
        priceSensitivity.multipliedBy(futures.getUnderlyingFuture().getNotional() * futures.getQuantity()));
  }

}
