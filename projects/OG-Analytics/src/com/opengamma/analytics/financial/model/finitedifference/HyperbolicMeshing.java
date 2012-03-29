/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.TrigonometricFunctionUtils;

/** 
   
 */
public class HyperbolicMeshing extends MeshingFunction {

  private final double _alpha;
  private final double _beta;
  private final double _gamma;
  private final double _delta;
  private final double _l;
  private final double _r;

  /**
   * Creates a non-uniform set of points according to the formula x_i = alpha * beta*Sinh(i/N*gamma + delta), where the points run from 
   * x_0 to x_N (i.e. there are N+1 points) and the highest concentration is around some specified point (e.g. the strike for solving option problems)
   * @param lowerBound The value of x_0
   * @param upperBound The value of x_N  
   * @param heart The value where the concentration of points is highest (<b>Note</b> there is no guarantee the a point will correspond exactly 
   * with this value)
   * @param nPoints Number of Points (equal to N+1 in the above formula)
   * @param bunching Bunching parameter. A value great than zero. Very small values gives a very high density of points around the specified point, with the
   * density quickly falling away in both directions (the total number of points is fixed), while the distribution tends to uniform for large values. Value 
   * greater than 1 are fairly uniform
   */
  public HyperbolicMeshing(final double lowerBound, final double upperBound, final double heart, final int nPoints, final double bunching) {
    super(nPoints);
    Validate.isTrue(upperBound > lowerBound, "need upperBound>lowerBound");
    Validate.isTrue(upperBound >= heart && heart >= lowerBound, "need heart between upper and lower bounds");
    Validate.isTrue(bunching > 0, "need bunching > 0");

    _l = lowerBound;
    _r = upperBound;
    _alpha = heart;
    _beta = bunching * (upperBound - lowerBound);
    _delta = TrigonometricFunctionUtils.asinh((lowerBound - heart) / _beta);
    _gamma = (TrigonometricFunctionUtils.asinh((upperBound - heart) / _beta) - _delta) / (nPoints - 1);
  }

  @Override
  public Double evaluate(Integer i) {
    Validate.isTrue(i >= 0 && i < getNumberOfPoints(), "i out of range");
    if (i == 0) {
      return _l;
    }
    if (i == getNumberOfPoints() - 1) {
      return _r;
    }
    return _alpha + _beta * Math.sinh(_gamma * i + _delta);
  }

}
