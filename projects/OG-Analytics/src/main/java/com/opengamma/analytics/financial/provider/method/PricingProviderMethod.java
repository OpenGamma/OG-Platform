/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.method;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Interface for interest rate pricing methods.
 * TODO: Do we need this interface?
 */
public interface PricingProviderMethod {

  /**
   * Computes the present value of an instrument.
   * @param instrument The instrument.
   * @param multicurve The multi-curve provider.
   * @return The present value.
   */
  MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final MulticurveProviderInterface multicurve);

}
