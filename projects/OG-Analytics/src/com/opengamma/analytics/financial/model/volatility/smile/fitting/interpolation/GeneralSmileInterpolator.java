/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import com.opengamma.analytics.math.function.Function1D;

/**
 * Any class implementing this must match the implied volatilities at all the strikes, and provide sensible extrapolation (i.e. the smile must be at least
 * continuous in the first derivative $(C^1)$ with the volatility never going below zero or towards infinity)
 */
// TODO rename this after analytics reshuffle
public interface GeneralSmileInterpolator {

  Function1D<Double, Double> getVolatilityFunction(final double forward, final double[] strikes, final double expiry, final double[] impliedVols);

}
