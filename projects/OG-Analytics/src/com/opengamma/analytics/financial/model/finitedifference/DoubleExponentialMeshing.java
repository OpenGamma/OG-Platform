/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class DoubleExponentialMeshing extends MeshingFunction {

  private final ExponentialMeshing _lowerMesh;
  private final ExponentialMeshing _upperMesh;
  private final int _nPointsLower;

  /**
   * creates a non-uniform set of points according by joining two ExponentialMeshing
   * @param lowerBound The value of x_0
   * @param upperBound The value of x_N
   * @param centre the value where we switch from the lower to upper ExponentialMeshing
   * @param nPoints Number of Points
   * @param lambdaLower Bunching parameter. lambda = 0 is uniform, lambda > 0 gives a high density of points near X_0 and lambda < 0 gives a high density
   * of points near centre
   *  @param lambdaUpper Bunching parameter. lambda = 0 is uniform, lambda > 0 gives a high density of points near centre and lambda < 0 gives a high density
   * of points near x_N
   */
  public DoubleExponentialMeshing(final double lowerBound, final double upperBound, final double centre, final int nPoints, final double lambdaLower, final double lambdaUpper) {
    super(nPoints);
    Validate.isTrue(centre > lowerBound, "need centre > lowerBound");
    Validate.isTrue(centre < upperBound, "need centre < upperBound");
    final double frac = (centre - lowerBound) / (upperBound - lowerBound);
    final int nPointsLower = (int) (frac * nPoints);
    final int nPointUpper = nPoints - nPointsLower + 1;
    _nPointsLower = nPointsLower;
    _lowerMesh = new ExponentialMeshing(lowerBound, centre, nPointsLower, lambdaLower);
    _upperMesh = new ExponentialMeshing(centre, upperBound, nPointUpper, lambdaUpper);
  }

  @Override
  public Double evaluate(final Integer i) {
    if (i < _nPointsLower) {
      return _lowerMesh.evaluate(i);
    }
    return _upperMesh.evaluate(i - _nPointsLower + 1);
  }

}
