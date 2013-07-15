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
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;

/**
 * Calculate the market quote of instruments dependent of a Hull-White one factor provider.
 */
public final class MarketQuoteCurveSensitivityHullWhiteIssuerCalculator extends InstrumentDerivativeVisitorAdapter<HullWhiteIssuerProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final MarketQuoteCurveSensitivityHullWhiteIssuerCalculator INSTANCE = new MarketQuoteCurveSensitivityHullWhiteIssuerCalculator();

  /**
   * Constructor.
   */
  private MarketQuoteCurveSensitivityHullWhiteIssuerCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static MarketQuoteCurveSensitivityHullWhiteIssuerCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final BondFuturesSecurityHullWhiteMethod METHOD_BNDFUT_SEC = BondFuturesSecurityHullWhiteMethod.getInstance();

  //     -----     Futures     -----

  @Override
  public MulticurveSensitivity visitBondFuturesSecurity(final BondFuturesSecurity futures, final HullWhiteIssuerProviderInterface hullWhite) {
    return METHOD_BNDFUT_SEC.priceCurveSensitivity(futures, hullWhite);
  }

}
