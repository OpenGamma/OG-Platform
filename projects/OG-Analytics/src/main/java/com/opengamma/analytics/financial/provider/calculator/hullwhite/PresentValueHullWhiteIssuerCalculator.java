/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.hullwhite;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.BondFuturesTransactionHullWhiteMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteIssuerProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueHullWhiteIssuerCalculator extends InstrumentDerivativeVisitorAdapter<HullWhiteIssuerProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueHullWhiteIssuerCalculator INSTANCE = new PresentValueHullWhiteIssuerCalculator();

  /**
   * Constructor.
   */
  private PresentValueHullWhiteIssuerCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueHullWhiteIssuerCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final BondFuturesTransactionHullWhiteMethod METHOD_BONDFUT_TRA = BondFuturesTransactionHullWhiteMethod.getInstance();

  //     -----     Futures     -----

  @Override
  public MultipleCurrencyAmount visitBondFuturesTransaction(final BondFuturesTransaction futures, final HullWhiteIssuerProviderInterface hullWhite) {
    return METHOD_BONDFUT_TRA.presentValue(futures, hullWhite);
  }

}
