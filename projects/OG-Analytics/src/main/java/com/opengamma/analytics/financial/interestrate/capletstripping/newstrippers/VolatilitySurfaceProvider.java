/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping.newstrippers;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.Surface;

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
   * Create a 'surface' that represents a volatility and its sensitivity to the model parameters at a particular point
   * (strike and expiry) - technically this is a 2D vector field  
   * @param modelParameters model parameters
   * @return volatility adjoint surface 
   */
  Surface<Double, Double, DoubleMatrix1D> getVolSurfaceAdjoint(final DoubleMatrix1D modelParameters);

  /**
   * 
   * @return The number of model parameters
   */
  int getNumModelParameters();

}
