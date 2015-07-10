/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackbondfutures;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFuturesOptionPremiumTransactionBlackBondFuturesMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.FuturesTransactionBlackBondFuturesMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueBlackBondFuturesOptionCalculator 
    extends InstrumentDerivativeVisitorAdapter<BlackBondFuturesProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBlackBondFuturesOptionCalculator INSTANCE = 
      new PresentValueBlackBondFuturesOptionCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueBlackBondFuturesOptionCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueBlackBondFuturesOptionCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final FuturesTransactionBlackBondFuturesMethod METHOD_FUT = 
      new FuturesTransactionBlackBondFuturesMethod();
  private static final BondFuturesOptionPremiumTransactionBlackBondFuturesMethod METHOD_OPT_BND_FUT_PREMIUM =
      BondFuturesOptionPremiumTransactionBlackBondFuturesMethod.getInstance();

  // -----     Futures     ------

  @Override
  public MultipleCurrencyAmount visitBondFuturesOptionMarginTransaction(final BondFuturesOptionMarginTransaction futures, 
      final BlackBondFuturesProviderInterface black) {
    return METHOD_FUT.presentValue(futures, black);
  }

  @Override
  public MultipleCurrencyAmount visitBondFutureOptionPremiumTransaction(final BondFuturesOptionPremiumTransaction futures, 
      final BlackBondFuturesProviderInterface black) {
    return METHOD_OPT_BND_FUT_PREMIUM.presentValue(futures, black);
  }

}
