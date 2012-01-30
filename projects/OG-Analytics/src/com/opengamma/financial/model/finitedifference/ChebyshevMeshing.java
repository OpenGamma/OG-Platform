/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class ChebyshevMeshing extends MeshingFunction {

  private double _a;
  private double _r;
  private int _n;

  /**
   * @param nPoints
   */
  public ChebyshevMeshing(final double lowerBound, final double upperBound, final int nPoints) {
    super(nPoints);
    Validate.isTrue(upperBound > lowerBound, "need upperBound>lowerBound");
    _a = lowerBound;
    _r = (upperBound - lowerBound) / 2;
    _n = nPoints - 1;
  }

  @Override
  public Double evaluate(Integer i) {
    Validate.isTrue(i >= 0 && i < getNumberOfPoints(), "i out of range");
    return _a + _r * (1 - Math.cos(i * Math.PI / _n));
  }

}
