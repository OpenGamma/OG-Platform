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
import com.opengamma.analytics.financial.interestrate.future.provider.BondFutureOptionMarginSecurityBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFuturesOptionPremiumSecurityBlackBondFuturesMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Computes the delta for bond future options.
 */
public final class DeltaBlackBondFuturesCalculator extends InstrumentDerivativeVisitorAdapter<BlackBondFuturesProviderInterface, Double> {

  /**
   * The singleton.
   */
  private static final DeltaBlackBondFuturesCalculator INSTANCE = new DeltaBlackBondFuturesCalculator();
  
  /**
   * Returns the calculator instance.
   * @return the calculator.
   */
  public static DeltaBlackBondFuturesCalculator getInstance() {
    return INSTANCE;
  }
  
  /**
   * Singleton constructor.
   */
  private DeltaBlackBondFuturesCalculator() {
  }
  
  /** The method used to compute the future option price */
  private static final BondFutureOptionMarginSecurityBlackSmileMethod METHOD_FUTURE_OPTION_MARGIN = 
      BondFutureOptionMarginSecurityBlackSmileMethod.getInstance();
  private static final BondFuturesOptionPremiumSecurityBlackBondFuturesMethod METHOD_FUTURE_OPTION_PREMIUM = 
      BondFuturesOptionPremiumSecurityBlackBondFuturesMethod.getInstance();

  @Override
  public Double visitBondFuturesOptionMarginSecurity(BondFuturesOptionMarginSecurity option, 
      BlackBondFuturesProviderInterface data) {
    ArgumentChecker.notNull(option, "security");
    ArgumentChecker.notNull(data, "data");
    return METHOD_FUTURE_OPTION_MARGIN.delta(option, data);
  }
  
  @Override
  public Double visitBondFuturesOptionMarginTransaction(BondFuturesOptionMarginTransaction option, 
      BlackBondFuturesProviderInterface data) {
    ArgumentChecker.notNull(option, "security");
    ArgumentChecker.notNull(data, "data");
    return METHOD_FUTURE_OPTION_MARGIN.delta(option.getUnderlyingSecurity(), data);
  }

  @Override
  public Double visitBondFutureOptionPremiumSecurity(BondFuturesOptionPremiumSecurity option, 
      BlackBondFuturesProviderInterface data) {
    ArgumentChecker.notNull(option, "security");
    ArgumentChecker.notNull(data, "data");
    return METHOD_FUTURE_OPTION_PREMIUM.delta(option, data);
  }
  
  @Override
  public Double visitBondFutureOptionPremiumTransaction(BondFuturesOptionPremiumTransaction option, 
      BlackBondFuturesProviderInterface data) {
    ArgumentChecker.notNull(option, "security");
    ArgumentChecker.notNull(data, "data");
    return METHOD_FUTURE_OPTION_PREMIUM.delta(option.getUnderlyingOption(), data);
  }
  
}
