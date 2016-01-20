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
 * Calculator for bond future option's theta.
 */
public final class ThetaBlackBondFuturesCalculator extends InstrumentDerivativeVisitorAdapter<BlackBondFuturesProviderInterface, Double> {

  /**
   * The singleton.
   */
  private static final ThetaBlackBondFuturesCalculator INSTANCE = new ThetaBlackBondFuturesCalculator();
  
  /**
   * Returns a singleton of the calculator.
   * @return the calculator.
   */
  public static ThetaBlackBondFuturesCalculator getInstance() {
    return INSTANCE;
  }
  
  /**
   * Singleton constructor.
   */
  private ThetaBlackBondFuturesCalculator() {
  }
  
  /**
   * Pricing method for theta.
   */
  private static final BondFutureOptionMarginSecurityBlackPriceMethod METHOD_FUTURE_OPTION_MARGIN = 
      BondFutureOptionMarginSecurityBlackPriceMethod.getInstance();
  private static final BondFuturesOptionPremiumSecurityBlackBondFuturesMethod METHOD_FUTURE_OPTION_PREMIUM = 
      BondFuturesOptionPremiumSecurityBlackBondFuturesMethod.getInstance();
  
  @Override
  public Double visitBondFuturesOptionMarginSecurity(BondFuturesOptionMarginSecurity option, 
      BlackBondFuturesProviderInterface data) {
    return METHOD_FUTURE_OPTION_MARGIN.theta(option, data);
  }
  
  @Override
  public Double visitBondFuturesOptionMarginTransaction(BondFuturesOptionMarginTransaction option, 
      BlackBondFuturesProviderInterface data) {
    return METHOD_FUTURE_OPTION_MARGIN.theta(option.getUnderlyingSecurity(), data);
  }
  
  @Override
  public Double visitBondFutureOptionPremiumSecurity(BondFuturesOptionPremiumSecurity option, 
      BlackBondFuturesProviderInterface data) {
    return METHOD_FUTURE_OPTION_PREMIUM.theta(option, data);
  }
  
  @Override
  public Double visitBondFutureOptionPremiumTransaction(BondFuturesOptionPremiumTransaction option, 
      BlackBondFuturesProviderInterface data) {
    return METHOD_FUTURE_OPTION_PREMIUM.theta(option.getUnderlyingOption(), data);
  }
  
}
