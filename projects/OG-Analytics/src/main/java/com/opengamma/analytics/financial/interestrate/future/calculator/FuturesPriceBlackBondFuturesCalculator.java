/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFuturesOptionMarginSecurityBlackBondFuturesMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Computes the price for different types of futures. Calculator using a multi-curve and issuer provider.
 */
public final class FuturesPriceBlackBondFuturesCalculator extends InstrumentDerivativeVisitorAdapter<BlackBondFuturesProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final FuturesPriceBlackBondFuturesCalculator INSTANCE = new FuturesPriceBlackBondFuturesCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPriceBlackBondFuturesCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FuturesPriceBlackBondFuturesCalculator() {
  }

  /** The method used to compute futures option */
  private static final BondFuturesOptionMarginSecurityBlackBondFuturesMethod METHOD_FUTURE_OPTION = BondFuturesOptionMarginSecurityBlackBondFuturesMethod
      .getInstance();

  //     -----     Futures options    -----

  @Override
  public Double visitBondFuturesOptionMarginSecurity(final BondFuturesOptionMarginSecurity security, final BlackBondFuturesProviderInterface black) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(black, "black");
    return METHOD_FUTURE_OPTION.price(security, black);
  }
  
  @Override
  public Double visitBondFuturesOptionMarginTransaction(BondFuturesOptionMarginTransaction option, BlackBondFuturesProviderInterface data) {
    return visitBondFuturesOptionMarginSecurity(option.getUnderlyingSecurity(), data);
  }
}
