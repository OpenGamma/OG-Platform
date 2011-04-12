/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @param <T> Type of the data needed for the volatility function
 */
public interface VolatilityFunctionProvider<T> {

  /**
   * Returns a function that, given data of type T, calculates the volatility.
   * @param option The option, not null
   * @return Returns a function that, given data of type T, calculates the volatility
   */
  Function1D<T, Double> getVolatilityFunction(EuropeanVanillaOption option);
}
