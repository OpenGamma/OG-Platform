/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureOptionMarginSecurityBlackSurfaceMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.method.SwaptionPhysicalFixedIborBlackMethod;

/**
 * Interpolates, for interest rate instruments using Black model, and returns the implied volatility required.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public final class BlackPriceCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> {

  /** The method unique instance.*/
  private static final BlackPriceCalculator INSTANCE = new BlackPriceCalculator();

  /** @return the unique instance of the class. */
  public static BlackPriceCalculator getInstance() {
    return INSTANCE;
  }

  /** Constructor. */
  BlackPriceCalculator() {
  }

  /** The physical swaption pricer */
  private static final SwaptionPhysicalFixedIborBlackMethod SWAPTION_PHYSICAL = SwaptionPhysicalFixedIborBlackMethod.getInstance();
  /** The margined interest rate future option pricer */
  private static final InterestRateFutureOptionMarginSecurityBlackSurfaceMethod IR_FUTURE_OPTION_SECURITY = InterestRateFutureOptionMarginSecurityBlackSurfaceMethod.getInstance();

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    return SWAPTION_PHYSICAL.presentValue(swaption, curves).getAmount(); // TODO Confirm this is the output the user would expect, wrt scaling of Annuity/PVBP
  }

  @Override
  public Double visitInterestRateFutureOptionMarginSecurity(final InterestRateFutureOptionMarginSecurity irFutureOption, final YieldCurveBundle curves) {
    return IR_FUTURE_OPTION_SECURITY.presentValue(irFutureOption, curves).getAmount();
  }

}
