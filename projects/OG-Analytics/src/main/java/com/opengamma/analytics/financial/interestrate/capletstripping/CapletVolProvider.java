/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public interface CapletVolProvider {

  /**
   * An abstraction for any model that takes a set of parameters and returns a set of (caplet) volatilities. 
   * @param modelParams The model parameters
   * @return the caplet volatilities 
   */
  DoubleMatrix1D getVolatilities(final DoubleMatrix1D modelParams);

  /**
   * An abstraction for any model that takes a set of parameters and returns the matrix (Jacobian) of sensitivities 
   * of the (caplet) volatilities (produced by the model) to the model parameters 
   * @param modelParams The model parameters
   * @return the caplet volatility Jacobian 
   */
  DoubleMatrix2D getVolJacobian(final DoubleMatrix1D modelParams);

}
