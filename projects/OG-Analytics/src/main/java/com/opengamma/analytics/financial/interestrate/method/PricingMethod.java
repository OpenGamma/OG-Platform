/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.method;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Interface for interest rate pricing methods.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public interface PricingMethod {

  /**
   * Computes the present value of the instrument.
   * @param instrument The instrument.
   * @param curves The yield curves.
   * @return The present value.
   */
  CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves);

}
