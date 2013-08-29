/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

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

  /** The methods used in the calculator. */
  private static final SwaptionPhysicalFixedIborBlackMethod METHOD_SWAPTION_PHYSICAL = SwaptionPhysicalFixedIborBlackMethod.getInstance();

  @Override
  public Double visitSwaptionPhysicalFixedIbor(final SwaptionPhysicalFixedIbor swaption, final YieldCurveBundle curves) {
    return METHOD_SWAPTION_PHYSICAL.presentValue(swaption, curves).getAmount(); // TODO Confirm this is the output the user would expect, wrt scaling of Annuity/PVBP
  }


}
