/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class ExponentalMeshing extends MeshingFunction {

  private final double _theta;
  private final double _eta;
  private final double _lambda;
  private final boolean _linear;

  /**
   * creates a non-uniform set of points according to the formula x_i = theta * eta*Exp(i/N*lambda), where the points run from 
   * x_0 to x_N (i.e. there are N+1 points)
   * @param lowerBound The value of x_0
   * @param upperBound The value of x_N
   * @param nPoints Number of Points (equal to N+1 in the above formula)
   * @param lambda Bunching parameter. lambda = 0 is uniform, lambda > 0 gives a high density of points near X_0 and lambda < 0 gives a high density
   * of points near x_N
   */
  public ExponentalMeshing(final double lowerBound, final double upperBound, final int nPoints, final double lambda) {
    Validate.isTrue(upperBound > lowerBound, "need upperBound>lowerBound");
    Validate.isTrue(nPoints > 1, "Need more than 1 point for a mesh");

    if (lambda == 0.0) {
      _linear = true;
      _theta = lowerBound;
      _eta = (upperBound - lowerBound) / (nPoints - 1);
      _lambda = 0.0;
    } else {
      _linear = false;
      _eta = (upperBound - lowerBound) / (Math.exp(lambda) - 1);
      _theta = lowerBound - _eta;
      _lambda = lambda / (nPoints - 1);
    }

  }

  @Override
  public Double evaluate(final Integer i) {
    if (_linear) {
      return _theta + _eta * i;
    }
    return _theta + _eta * Math.exp(i * _lambda);
  }

}
