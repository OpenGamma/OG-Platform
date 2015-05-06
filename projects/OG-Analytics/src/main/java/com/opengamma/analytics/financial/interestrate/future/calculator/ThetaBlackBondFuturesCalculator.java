package com.opengamma.analytics.financial.interestrate.future.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFutureOptionMarginSecurityBlackSmileMethod;
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
  private static final BondFutureOptionMarginSecurityBlackSmileMethod METHOD = BondFutureOptionMarginSecurityBlackSmileMethod
      .getInstance();
  
  @Override
  public Double visitBondFuturesOptionMarginSecurity(BondFuturesOptionMarginSecurity option, BlackBondFuturesProviderInterface data) {
    return METHOD.theta(option, data);
  }
  
  @Override
  public Double visitBondFuturesOptionMarginTransaction(BondFuturesOptionMarginTransaction option, BlackBondFuturesProviderInterface data) {
    return METHOD.theta(option.getUnderlyingSecurity(), data);
  }
}
