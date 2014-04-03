/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.singlevalue;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableTransaction;
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
  public MultipleCurrencyMulticurveSensitivity visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction futures, final MulticurveSensitivity priceSensitivity) {
    return MultipleCurrencyMulticurveSensitivity.of(futures.getCurrency(),
        priceSensitivity.multipliedBy(futures.getUnderlyingFuture().getNotional() * futures.getQuantity() * futures.getUnderlyingFuture().getPaymentAccrualFactor()));
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitBondFuturesTransaction(final BondFuturesTransaction futures, final MulticurveSensitivity priceSensitivity) {
    return MultipleCurrencyMulticurveSensitivity.of(futures.getCurrency(), priceSensitivity.multipliedBy(futures.getUnderlyingFuture().getNotional() * futures.getQuantity()));
  }

  @Override
  public MultipleCurrencyMulticurveSensitivity visitSwapFuturesPriceDeliverableTransaction(final SwapFuturesPriceDeliverableTransaction futures, final MulticurveSensitivity priceSensitivity) {
    return MultipleCurrencyMulticurveSensitivity.of(futures.getCurrency(), priceSensitivity.multipliedBy(futures.getUnderlyingFuture().getNotional() * futures.getQuantity()));
  }

  //     -----     Futures options    -----

  @Override
  public MultipleCurrencyMulticurveSensitivity visitBondFuturesOptionMarginTransaction(final BondFuturesOptionMarginTransaction futures, final MulticurveSensitivity priceSensitivity) {
    return MultipleCurrencyMulticurveSensitivity.of(futures.getCurrency(), priceSensitivity.multipliedBy(futures.getUnderlyingFuture().getUnderlyingFuture().getNotional()
        * futures.getQuantity()));
  }
}
