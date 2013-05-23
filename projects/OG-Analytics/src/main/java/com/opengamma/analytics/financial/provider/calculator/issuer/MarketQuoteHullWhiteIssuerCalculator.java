/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.issuer;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFuturesSecurityHullWhiteMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteIssuerProviderInterface;

/**
 * Calculate the market quote of instruments dependent of a Hull-White one factor provider.
 */
public final class MarketQuoteHullWhiteIssuerCalculator extends InstrumentDerivativeVisitorAdapter<HullWhiteIssuerProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final MarketQuoteHullWhiteIssuerCalculator INSTANCE = new MarketQuoteHullWhiteIssuerCalculator();

  /**
   * Constructor.
   */
  private MarketQuoteHullWhiteIssuerCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static MarketQuoteHullWhiteIssuerCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final BondFuturesSecurityHullWhiteMethod METHOD_BNDFUT_SEC = BondFuturesSecurityHullWhiteMethod.getInstance();

  //     -----     Futures     -----

  @Override
  public Double visitBondFuturesSecurity(final BondFuturesSecurity futures, final HullWhiteIssuerProviderInterface hullWhite) {
    return METHOD_BNDFUT_SEC.price(futures, hullWhite);
  }

}
