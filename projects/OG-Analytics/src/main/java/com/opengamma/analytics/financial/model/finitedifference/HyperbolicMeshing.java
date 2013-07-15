/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.TrigonometricFunctionUtils;
import com.opengamma.util.ArgumentChecker;

/** 
   
 */
public class HyperbolicMeshing extends MeshingFunction {

  private final UniformMeshing _um;
  private final double[] _fpValues;
  private final double _alpha;
  private final double _beta;
  private final double _gamma;
  private final double _delta;
  private final double _l;
  private final double _r;

  /**
   * Creates a non-uniform set of points according to the formula x_i = alpha * beta*Sinh(i/N*gamma + delta), where the points run from 
   * x_0 to x_N (i.e. there are N+1 points) and the highest concentration is around some specified point (e.g. the strike for solving option problems)
   * @param xMin The value of x_0
   * @param xMax The value of x_N  
   * @param xCent The value where the concentration of points is highest (<b>Note</b> there is no guarantee the a point will correspond exactly 
   * with this value)
   * @param nPoints Number of Points (equal to N+1 in the above formula)
   * @param beta Bunching parameter. A value great than zero. Very small values gives a very high density of points around the specified point, with the
   * density quickly falling away in both directions (the total number of points is fixed), while the distribution tends to uniform for large values. Value 
   * greater than 1 are fairly uniform
   */
  public HyperbolicMeshing(final double xMin, final double xMax, final double xCent, final int nPoints, final double beta) {
    super(nPoints);
    Validate.isTrue(xMax > xMin, "need xMax > xMin");
    Validate.isTrue(xMax >= xCent && xCent >= xMin, "need xCent between upper and lower bounds");
    Validate.isTrue(beta > 0, "need beta > 0");

    _l = xMin;
    _r = xMax;
    _alpha = xCent;
    _beta = beta * (xMax - xMin);
    _delta = TrigonometricFunctionUtils.asinh((xMin - xCent) / _beta);
    _gamma = (TrigonometricFunctionUtils.asinh((xMax - xCent) / _beta) - _delta);
    _um = new UniformMeshing(nPoints);
    _fpValues = null;
  }

  public HyperbolicMeshing(final double xMin, final double xMax, final double xCent, final int nPoints, final double beta, final double[] fixedPoints) {
    super(nPoints);
    Validate.isTrue(xMax > xMin, "need xMax > xMin");
    Validate.isTrue(xMax >= xCent && xCent >= xMin, "need xCent between upper and lower bounds");
    Validate.isTrue(beta > 0, "need beta > 0");
    ArgumentChecker.notNull(fixedPoints, "null fixedPoints");

    _l = xMin;
    _r = xMax;
    _alpha = xCent;
    _beta = beta * (xMax - xMin);
    _delta = TrigonometricFunctionUtils.asinh((xMin - xCent) / _beta);
    _gamma = (TrigonometricFunctionUtils.asinh((xMax - xCent) / _beta) - _delta);

    _fpValues = FunctionUtils.unique(fixedPoints);
    int m = _fpValues.length;
    final double[] fp = new double[m];
    for (int ii = 0; ii < m; ii++) {
      fp[ii] = (TrigonometricFunctionUtils.asinh((_fpValues[ii] - _alpha) / _beta) - _delta) / _gamma;
    }

    _um = new UniformMeshing(nPoints, fp);
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

    //short cut if required point is one of the specified fixed points 
    if (_fpValues != null) {
      int index = _um.getFixedPointIndex(i);
      if (index >= 0) {
        return _fpValues[index];
      }
    }

    final double z = _um.evaluate(i);
    return _alpha + _beta * Math.sinh(_gamma * z + _delta);
  }

}
