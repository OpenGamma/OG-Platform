/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackbondfutures;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.FuturesSecurityIssuerMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.FuturesTransactionBlackBondFuturesMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueCurveSensitivityBlackBondFuturesOptionCalculator extends
    InstrumentDerivativeVisitorAdapter<BlackBondFuturesProviderInterface, MultipleCurrencyMulticurveSensitivity> {
  
  private final FuturesTransactionBlackBondFuturesMethod _methodFutures;

  /**
   * Default constructor.
   */
  public PresentValueCurveSensitivityBlackBondFuturesOptionCalculator() {
    _methodFutures = new FuturesTransactionBlackBondFuturesMethod();
  }
  
  
  /**
   * Constructor from a futures method.
   */
  public PresentValueCurveSensitivityBlackBondFuturesOptionCalculator(FuturesSecurityIssuerMethod methodFutures) {
    _methodFutures = new FuturesTransactionBlackBondFuturesMethod(methodFutures);
  }

  // -----     Futures     ------

  @Override
  public MultipleCurrencyMulticurveSensitivity visitBondFuturesOptionMarginTransaction(
      final BondFuturesOptionMarginTransaction futures, final BlackBondFuturesProviderInterface black) {
    return _methodFutures.presentValueCurveSensitivity(futures, black);
  }

}
