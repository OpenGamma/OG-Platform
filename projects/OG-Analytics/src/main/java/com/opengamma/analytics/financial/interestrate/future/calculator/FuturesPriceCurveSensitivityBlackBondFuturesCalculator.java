/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFutureOptionMarginSecurityBlackSmileMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.FuturesSecurityIssuerMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;

/**
 * Computes the price for different types of futures. Calculator using a multi-curve and issuer provider.
 */
public final class FuturesPriceCurveSensitivityBlackBondFuturesCalculator 
    extends InstrumentDerivativeVisitorAdapter<BlackBondFuturesProviderInterface, MulticurveSensitivity> {

  /** The default instance of the calculator. */
  private static final FuturesPriceCurveSensitivityBlackBondFuturesCalculator DEFAULT = 
      new FuturesPriceCurveSensitivityBlackBondFuturesCalculator();
  
  /** The method used to compute futures option. */
  private final BondFutureOptionMarginSecurityBlackSmileMethod _methodFuturesOption;

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPriceCurveSensitivityBlackBondFuturesCalculator getInstance() {
    return DEFAULT;
  }

  /**
   * Constructor.
   */
  private FuturesPriceCurveSensitivityBlackBondFuturesCalculator() {
    _methodFuturesOption = BondFutureOptionMarginSecurityBlackSmileMethod.getInstance();
  }

  /**
   * Constructor from a particular bond futures method. The method is used to compute the price and price curve
   * sensitivity of the underlying futures.
   * @param methodFutures The method used to compute futures option.
   */
  public FuturesPriceCurveSensitivityBlackBondFuturesCalculator(FuturesSecurityIssuerMethod methodFutures) {
    _methodFuturesOption = new BondFutureOptionMarginSecurityBlackSmileMethod(methodFutures);
  }

  //     -----     Futures options    -----

  @Override
  public MulticurveSensitivity visitBondFuturesOptionMarginSecurity(final BondFuturesOptionMarginSecurity security, 
      final BlackBondFuturesProviderInterface black) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(black, "Black  data");
    return _methodFuturesOption.priceCurveSensitivity(security, black);
  }

}
