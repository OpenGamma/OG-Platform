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
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueBlackFlatBondFuturesOptionCalculator extends InstrumentDerivativeVisitorAdapter<BlackBondFuturesFlatProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBlackFlatBondFuturesOptionCalculator INSTANCE = new PresentValueBlackFlatBondFuturesOptionCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueBlackFlatBondFuturesOptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueBlackFlatBondFuturesOptionCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final FuturesTransactionBlackFlatBondFuturesMethod METHOD_FUT = new FuturesTransactionBlackFlatBondFuturesMethod();

  // -----     Futures     ------

  @Override
  public MultipleCurrencyAmount visitBondFuturesOptionMarginTransaction(final BondFuturesOptionMarginTransaction futures, final BlackBondFuturesFlatProviderInterface black) {
    return METHOD_FUT.presentValue(futures, black);
  }

}
