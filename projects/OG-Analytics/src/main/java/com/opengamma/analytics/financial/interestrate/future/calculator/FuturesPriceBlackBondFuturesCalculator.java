/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.financial.interestrate.future.provider.FuturesSecurityIssuerMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Computes the price for different types of futures. Calculator using a multi-curve and issuer provider.
 */
public final class FuturesPriceBlackBondFuturesCalculator 
    extends InstrumentDerivativeVisitorAdapter<BlackBondFuturesProviderInterface, Double> {

  /** The default instance of the calculator. */
  private static final FuturesPriceBlackBondFuturesCalculator DEFAULT = new FuturesPriceBlackBondFuturesCalculator();
  
  /** The method used to compute futures option */
  private final BondFutureOptionMarginSecurityBlackSmileMethod _methodFuturesOptionMargin;
  private static final BondFuturesOptionPremiumSecurityBlackBondFuturesMethod METHOD_FUT_OPT_PREMIUM =
      BondFuturesOptionPremiumSecurityBlackBondFuturesMethod.getInstance();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPriceBlackBondFuturesCalculator getInstance() {
    return DEFAULT;
  }

  /**
   * Default constructor.
   */
  private FuturesPriceBlackBondFuturesCalculator() {
    _methodFuturesOptionMargin = BondFutureOptionMarginSecurityBlackSmileMethod.getInstance();
  }

  /**
   * Constructor from a particular bond futures method. The method is used to compute the price and price curve
   * sensitivity of the underlying futures.
   * @param methodFutures The method used to compute futures option.
   */
  public FuturesPriceBlackBondFuturesCalculator(FuturesSecurityIssuerMethod methodFutures) {
    _methodFuturesOptionMargin = new BondFutureOptionMarginSecurityBlackSmileMethod(methodFutures);
  }

  //     -----     Futures options    -----

  @Override
  public Double visitBondFuturesOptionMarginSecurity(final BondFuturesOptionMarginSecurity security, 
      final BlackBondFuturesProviderInterface black) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(black, "black");
    return _methodFuturesOptionMargin.price(security, black);
  }
  
  @Override
  public Double visitBondFuturesOptionMarginTransaction(BondFuturesOptionMarginTransaction option, 
      BlackBondFuturesProviderInterface data) {
    return visitBondFuturesOptionMarginSecurity(option.getUnderlyingSecurity(), data);
  }

  @Override
  public Double visitBondFutureOptionPremiumSecurity(final BondFuturesOptionPremiumSecurity security, 
      final BlackBondFuturesProviderInterface black) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(black, "black");
    return METHOD_FUT_OPT_PREMIUM.price(security, black);
  }
  
  @Override
  public Double visitBondFutureOptionPremiumTransaction(BondFuturesOptionPremiumTransaction option, 
      BlackBondFuturesProviderInterface black) {
    return METHOD_FUT_OPT_PREMIUM.price(option.getUnderlyingOption(), black);
  }
  
}
