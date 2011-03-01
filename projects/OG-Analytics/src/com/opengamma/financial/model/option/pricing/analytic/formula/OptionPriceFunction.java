/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @param <T> Type of the data the price function requires
 */
public interface OptionPriceFunction<T> {

  Function1D<T, Double> getPriceFunction(EuropeanVanillaOption option);
}
