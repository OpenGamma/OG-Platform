/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.tuple.Pair;

/**
 * abstraction for any model that takes set of parameters and produces a volatility surface 
 */
public interface VolatilitySurfaceProvider {

  /**
   * Create a volatility surface according to the supplied model parameters 
   * @param modelParameters model parameters
   * @return a volatility surface
   */
  VolatilitySurface getVolSurface(final DoubleMatrix1D modelParameters);

  /**
   * Create a 'surface' that represents the volatility's sensitivity to the model parameters at a particular point
   * (strike and expiry) 
   * @param modelParameters model parameters
   * @return volatility's sensitivity to the model parameters  
   */
  Surface<Double, Double, DoubleMatrix1D> getParameterSensitivitySurface(final DoubleMatrix1D modelParameters);

  /**
   * Create a 'surface' that represents a volatility and its sensitivity to the model parameters at a particular point
   * (strike and expiry). <P>
   * The point of this method is that we often want both the value (the volatility) and its sensitivity, and it is usually
   * cheaper to calculate these together  
   * @param modelParameters model parameters
   * @return volatility and its sensitivity to the model parameters (as a {@link Pair} with the first value the volatility
   * and the second the volatility's sensitivity to the model parameters 
   */
  Surface<Double, Double, Pair<Double, DoubleMatrix1D>> getVolAndParameterSensitivitySurface(final DoubleMatrix1D modelParameters);

  /**
   * Get the number of model parameters 
   * @return The number of model parameters
   */
  int getNumModelParameters();

}
