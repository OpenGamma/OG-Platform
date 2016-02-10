/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionPremiumSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFutureOptionMarginSecurityBlackPriceMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFuturesOptionPremiumSecurityBlackBondFuturesMethod;
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
  private static final BondFutureOptionMarginSecurityBlackPriceMethod METHOD_FUTURE_OPTION_MARGIN = 
      BondFutureOptionMarginSecurityBlackPriceMethod.getInstance();
  private static final BondFuturesOptionPremiumSecurityBlackBondFuturesMethod METHOD_FUTURE_OPTION_PREMIUM = 
      BondFuturesOptionPremiumSecurityBlackBondFuturesMethod.getInstance();
  
  @Override
  public Double visitBondFuturesOptionMarginSecurity(BondFuturesOptionMarginSecurity option, 
      BlackBondFuturesProviderInterface data) {
    return METHOD_FUTURE_OPTION_MARGIN.vega(option, data);
  }
  
  @Override
  public Double visitBondFuturesOptionMarginTransaction(BondFuturesOptionMarginTransaction option, 
      BlackBondFuturesProviderInterface data) {
    return METHOD_FUTURE_OPTION_MARGIN.vega(option.getUnderlyingSecurity(), data);
  }
  
  @Override
  public Double visitBondFutureOptionPremiumSecurity(BondFuturesOptionPremiumSecurity option, 
      BlackBondFuturesProviderInterface data) {
    return METHOD_FUTURE_OPTION_PREMIUM.vega(option, data);
  }
  
  @Override
  public Double visitBondFutureOptionPremiumTransaction(BondFuturesOptionPremiumTransaction option, 
      BlackBondFuturesProviderInterface data) {
    return METHOD_FUTURE_OPTION_PREMIUM.vega(option.getUnderlyingOption(), data);
  }
  
}
