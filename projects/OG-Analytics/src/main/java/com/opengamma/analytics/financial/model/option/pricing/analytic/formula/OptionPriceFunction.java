/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.formula;

import com.opengamma.analytics.math.function.Function1D;

/**
 * 
 * @param <T> Type of the data the price function requires
 */
public interface OptionPriceFunction<T> {

  Function1D<T, Double> getPriceFunction(EuropeanVanillaOption option);
}
