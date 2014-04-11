/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.singlevalue;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueBlackBondFuturesCubeSensitivity;

/**
 * Computes the par rate for different instrument. The meaning of "par rate" is instrument dependent.
 */
public final class FuturesPVBlackBondFuturesSensitivityFromPriceBlackSensitivityCalculator
    extends InstrumentDerivativeVisitorAdapter<PresentValueBlackBondFuturesCubeSensitivity, PresentValueBlackBondFuturesCubeSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final FuturesPVBlackBondFuturesSensitivityFromPriceBlackSensitivityCalculator INSTANCE = new FuturesPVBlackBondFuturesSensitivityFromPriceBlackSensitivityCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPVBlackBondFuturesSensitivityFromPriceBlackSensitivityCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FuturesPVBlackBondFuturesSensitivityFromPriceBlackSensitivityCalculator() {

  }

  //     -----     Futures options    -----

  @Override
  public PresentValueBlackBondFuturesCubeSensitivity visitBondFuturesOptionMarginTransaction(final BondFuturesOptionMarginTransaction futures,
      final PresentValueBlackBondFuturesCubeSensitivity priceSensitivity) {
    return priceSensitivity.multipliedBy(futures.getUnderlyingSecurity().getUnderlyingFuture().getNotional() * futures.getQuantity());
  }

}
