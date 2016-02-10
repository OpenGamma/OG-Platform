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
 * Computes the gamma for bond future options.
 */
public final class GammaBlackBondFuturesCalculator extends InstrumentDerivativeVisitorAdapter<BlackBondFuturesProviderInterface, Double> {

  /**
   * The singleton.
   */
  private static final GammaBlackBondFuturesCalculator INSTANCE = new GammaBlackBondFuturesCalculator();

  /**
   * Returns the singleton of the calculator.
   * @return the calculator.
   */
  public static GammaBlackBondFuturesCalculator getInstance() {
    return INSTANCE;
  }
  
  /**
   * Singleton constructor.
   */
  private GammaBlackBondFuturesCalculator() {
  }

  /** The method used to compute the future option price */
  private static final BondFutureOptionMarginSecurityBlackPriceMethod METHOD_FUTURE_OPTION_MARGIN = 
      BondFutureOptionMarginSecurityBlackPriceMethod.getInstance();
  private static final BondFuturesOptionPremiumSecurityBlackBondFuturesMethod METHOD_FUTURE_OPTION_PREMIUM = 
      BondFuturesOptionPremiumSecurityBlackBondFuturesMethod.getInstance();
  
  @Override
  public Double visitBondFuturesOptionMarginSecurity(BondFuturesOptionMarginSecurity option, 
      BlackBondFuturesProviderInterface data) {
    return METHOD_FUTURE_OPTION_MARGIN.gamma(option, data);
  }
  
  @Override
  public Double visitBondFuturesOptionMarginTransaction(BondFuturesOptionMarginTransaction option, 
      BlackBondFuturesProviderInterface data) {
    return METHOD_FUTURE_OPTION_MARGIN.gamma(option.getUnderlyingSecurity(), data);
  }
  
  @Override
  public Double visitBondFutureOptionPremiumSecurity(BondFuturesOptionPremiumSecurity option, 
      BlackBondFuturesProviderInterface data) {
    return METHOD_FUTURE_OPTION_PREMIUM.gamma(option, data);
  }
  
  @Override
  public Double visitBondFutureOptionPremiumTransaction(BondFuturesOptionPremiumTransaction option, 
      BlackBondFuturesProviderInterface data) {
    return METHOD_FUTURE_OPTION_PREMIUM.gamma(option.getUnderlyingOption(), data);
  }
  
}
