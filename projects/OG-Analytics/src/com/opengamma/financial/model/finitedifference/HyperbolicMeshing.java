/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.math.TrigonometricFunctionUtils;

/** 
   
 */
public class HyperbolicMeshing extends MeshingFunction {

  private final double _alpha;
  private final double _beta;
  private final double _gamma;
  private final double _delta;

  /**
   * Creates a non-uniform set of points according to the formula x_i = alpha * beta*Sinh(i/N*gamma + delta), where the points run from 
   * x_0 to x_N (i.e. there are N+1 points) and the highest concentration is around some specified point (e.g. the strike for solving option problems)
   * @param lowerBound The value of x_0
   * @param upperBound The value of x_N  
   * @param concentration The value where the concentration of points is highest (<b>Note</b> there is no guarantee the a point will correspond exactly 
   * with this value)
   * @param bunching Bunching parameter. A value great than zero. Very small values gives a very high density of points around the specified point, with the
   * density quickly falling away in both directions (the total number of points is fixed), while the distribution tends to uniform for large values. Value 
   * greater than 1 are fairly uniform
   * @param nPoints Number of Points (equal to N+1 in the above formula)
   */
  public HyperbolicMeshing(final double lowerBound, final double upperBound, final double concentration, final double bunching, final int nPoints) {
    Validate.isTrue(upperBound > lowerBound, "need upperBound>lowerBound");
    Validate.isTrue(upperBound >= concentration && concentration >= lowerBound, "need concentration between upper and lower bounds");
    Validate.isTrue(nPoints > 1);
    Validate.isTrue(bunching > 0, "need bunching > 0");

    _alpha = concentration;
    _beta = bunching * (upperBound - lowerBound);
    _delta = TrigonometricFunctionUtils.asinh((lowerBound - concentration) / _beta);
    _gamma = (TrigonometricFunctionUtils.asinh((upperBound - concentration) / _beta) - _delta) / (nPoints - 1);
  }

  @Override
  public Double evaluate(Integer x) {
    return _alpha + _beta * Math.sinh(_gamma * x + _delta);
  }

}
