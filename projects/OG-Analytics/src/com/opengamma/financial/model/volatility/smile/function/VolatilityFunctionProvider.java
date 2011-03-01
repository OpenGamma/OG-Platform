/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
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

  Function1D<T, Double> getVolatilityFunction(EuropeanVanillaOption option);
}
