/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.EquityOptionDataBundle;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;

import org.apache.commons.lang.Validate;

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
    Validate.notNull(option, "The derivative, EquityIndexOption, was null.");
    Validate.notNull(mktData, "DataBundle was null. Expecting an EquityOptionDataBundle");
    throw new OpenGammaRuntimeException("EquityIndexOptionBlackMethod requires a data bundle of type EquityOptionDataBundle. Found a YieldCurveBundle.");
  }

  public Double presentValue(EquityIndexOption derivative, EquityOptionDataBundle market) {
    return null;
  }

  // !!!!!!! SEE InterestRateFutureOptionMarginSecurityBlackSurfaceMethod

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
