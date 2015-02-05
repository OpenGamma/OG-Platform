/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFutureOptionMarginSecurityBlackSmileMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;

/**
 * Computes the vega for bond future options.
 */
public final class VegaBlackBondFuturesCalculator extends InstrumentDerivativeVisitorAdapter<BlackBondFuturesProviderInterface, Double> {

  /**
   * The singleton instance.
   */
  private static final VegaBlackBondFuturesCalculator INSTANCE = new VegaBlackBondFuturesCalculator();
  
  /**
   * Returns the singleton of the calculator instance.
   * @return the calculator.
   */
  public static VegaBlackBondFuturesCalculator getInstance() {
    return INSTANCE;
  }
  
  /**
   * Singleton constructor.
   */
  private VegaBlackBondFuturesCalculator() {
  }

  /** The method used to compute the future option price */
  private static final BondFutureOptionMarginSecurityBlackSmileMethod METHOD_FUTURE_OPTION = BondFutureOptionMarginSecurityBlackSmileMethod
      .getInstance();
  
  @Override
  public Double visitBondFuturesOptionMarginSecurity(BondFuturesOptionMarginSecurity option, BlackBondFuturesProviderInterface data) {
    return METHOD_FUTURE_OPTION.vega(option, data);
  }
  
  @Override
  public Double visitBondFuturesOptionMarginTransaction(BondFuturesOptionMarginTransaction option, BlackBondFuturesProviderInterface data) {
    return METHOD_FUTURE_OPTION.vega(option.getUnderlyingSecurity(), data);
  }
}
