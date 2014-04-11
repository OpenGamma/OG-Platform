/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.singlevalue;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueBlackSTIRFuturesCubeSensitivity;

/**
 * Computes the par rate for different instrument. The meaning of "par rate" is instrument dependent.
 */
public final class FuturesPVBlackSTIRFuturesSensitivityFromPriceBlackSensitivityCalculator
    extends InstrumentDerivativeVisitorAdapter<PresentValueBlackSTIRFuturesCubeSensitivity, PresentValueBlackSTIRFuturesCubeSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final FuturesPVBlackSTIRFuturesSensitivityFromPriceBlackSensitivityCalculator INSTANCE = new FuturesPVBlackSTIRFuturesSensitivityFromPriceBlackSensitivityCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPVBlackSTIRFuturesSensitivityFromPriceBlackSensitivityCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FuturesPVBlackSTIRFuturesSensitivityFromPriceBlackSensitivityCalculator() {

  }

  //     -----     Futures options    -----

  @Override
  public PresentValueBlackSTIRFuturesCubeSensitivity visitInterestRateFutureOptionMarginTransaction(final InterestRateFutureOptionMarginTransaction futures,
      final PresentValueBlackSTIRFuturesCubeSensitivity priceSensitivity) {
    return priceSensitivity.multipliedBy(futures.getUnderlyingSecurity().getUnderlyingFuture().getNotional()
        * futures.getUnderlyingSecurity().getUnderlyingFuture().getPaymentAccrualFactor() * futures.getQuantity());
  }

}
