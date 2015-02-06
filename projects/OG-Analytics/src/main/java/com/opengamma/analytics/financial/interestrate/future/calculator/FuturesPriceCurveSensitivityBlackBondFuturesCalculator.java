/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFutureOptionMarginSecurityBlackSmileMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackBondFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;

/**
 * Computes the price for different types of futures. Calculator using a multi-curve and issuer provider.
 */
public final class FuturesPriceCurveSensitivityBlackBondFuturesCalculator extends InstrumentDerivativeVisitorAdapter<BlackBondFuturesProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final FuturesPriceCurveSensitivityBlackBondFuturesCalculator INSTANCE = new FuturesPriceCurveSensitivityBlackBondFuturesCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static FuturesPriceCurveSensitivityBlackBondFuturesCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FuturesPriceCurveSensitivityBlackBondFuturesCalculator() {
  }

  /** The method used to compute futures option */
  private static final BondFutureOptionMarginSecurityBlackSmileMethod METHOD_FUTURE_OPTION = BondFutureOptionMarginSecurityBlackSmileMethod
      .getInstance();

  //     -----     Futures options    -----

  @Override
  public MulticurveSensitivity visitBondFuturesOptionMarginSecurity(final BondFuturesOptionMarginSecurity security, final BlackBondFuturesProviderInterface black) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(black, "Black  data");
    return METHOD_FUTURE_OPTION.priceCurveSensitivity(security, black);
  }

}
