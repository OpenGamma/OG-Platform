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
public class ChebyshevMeshing extends MeshingFunction {

  private final double _a;
  private final double _r;
  private final int _n;

  /**
   * @param lowerBound The lower bound
   * @param upperBound The upper bound
   * @param nPoints The number of points
   */
  public ChebyshevMeshing(final double lowerBound, final double upperBound, final int nPoints) {
    super(nPoints);
    Validate.isTrue(upperBound > lowerBound, "need upperBound>lowerBound");
    _a = lowerBound;
    _r = (upperBound - lowerBound) / 2;
    _n = nPoints - 1;
  }

  @Override
  public Double evaluate(final Integer i) {
    Validate.isTrue(i >= 0 && i < getNumberOfPoints(), "i out of range");
    return _a + _r * (1 - Math.cos(i * Math.PI / _n));
  }

}
