/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Interface for Forex pricing methods.
 */
//TODO: Class name to be change! Pricing method interface should probably be the same for all asset classes.
public interface ForexPricingMethod {

  /**
   * Computes the present value of the instrument.
   * @param instrument The instrument.
   * @param curves The yield curves.
   * @return The present value.
   */
  MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves);

  /**
   * Computes the currency exposure of the instrument.
   * @param instrument The instrument.
   * @param curves The yield curves.
   * @return The currency exposure.
   */
  MultipleCurrencyAmount currencyExposure(InstrumentDerivative instrument, YieldCurveBundle curves);

}
