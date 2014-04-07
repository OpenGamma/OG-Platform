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
public final class FuturesPVBlackSensitivityFromPriceBlackSensitivityCalculator
    extends InstrumentDerivativeVisitorAdapter<PresentValueBlackBondFuturesCubeSensitivity, PresentValueBlackBondFuturesCubeSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final FuturesPVBlackSensitivityFromPriceBlackSensitivityCalculator INSTANCE = new FuturesPVBlackSensitivityFromPriceBlackSensitivityCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPVBlackSensitivityFromPriceBlackSensitivityCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FuturesPVBlackSensitivityFromPriceBlackSensitivityCalculator() {

  }

  //     -----     Futures options    -----

  @Override
  public PresentValueBlackBondFuturesCubeSensitivity visitBondFuturesOptionMarginTransaction(final BondFuturesOptionMarginTransaction futures,
      final PresentValueBlackBondFuturesCubeSensitivity priceSensitivity) {
    return priceSensitivity.multipliedBy(futures.getUnderlyingFuture().getUnderlyingFuture().getNotional() * futures.getQuantity());
  }

}
