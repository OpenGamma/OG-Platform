/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;

/**
 * Pricing method for vanilla Equity Index Option transactions with Black function.
 */
public final class EquityIndexOptionBlackMethod {

  private static final EquityIndexOptionBlackMethod INSTANCE = new EquityIndexOptionBlackMethod();

  public static EquityIndexOptionBlackMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private EquityIndexOptionBlackMethod() {
  }

  /**
   * The Black function used in the pricing. Works on EuropeanVanillaOption.
   * TODO !!! Use this? Or BlackFormulaRepository
   */
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  public Double presentValue(final EquityIndexOption option, final YieldCurveBundle mktData) {
    return null;
  }

  /** What else?
   * Delta wrt Spot
   * Delta wrt Fwd
   * Delta wrt Strike (DualDelta)
   * Gamma (spot, fwd)
   * ImpliedVol
   * Vega (wrt single impliedVol)
   * Vega (wrt impliedVol surface)
   * Rates Delta (again, single rate, and curve)
   * 
   * 
   */

}
