/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ExponentialMeshing extends MeshingFunction {
  private final double[] _fpValues;
  private final UniformMeshing _um;
  private final double _theta;
  private final double _eta;
  private final double _lambda;
  private final boolean _linear;
  private final double _l;
  private final double _r;

  /**
   * creates a non-uniform set of points according to the formula $x_i = \theta + \eta*\exp(\lambda z_i)$, where the points run from 
   * $x_0$ to $x_{N-1}$ (i.e. there are N points), $\eta = (x_{N-1} - x_0)/(\exp(\lambda) - 1)$ and $\theta = x_0 - \eta$. The points $z_i$ are uniform on (0,1) and 
   * given by $z_i = i/(N-1)$. 
   * @param lowerBound The value of $x_0$
   * @param upperBound The value of $x_{N-1}$
   * @param nPoints Number of Points (equal to N in the above formula)
   * @param lambda Bunching parameter. $\lambda = 0$ is uniform, $\lambda > 0$ gives a high density of points near $x_0$ and $\lambda < 0$ gives a high density
   * of points near $x_{N-1}$
   */
  public ExponentialMeshing(final double lowerBound, final double upperBound, final int nPoints, final double lambda) {
    super(nPoints);
    Validate.isTrue(upperBound > lowerBound, "need upperBound>lowerBound");
    _l = lowerBound;
    _r = upperBound;
    _lambda = lambda;

    if (lambda == 0.0) {
      _linear = true;
      _theta = lowerBound;
      _eta = (upperBound - lowerBound);
    } else {
      _linear = false;
      _eta = (upperBound - lowerBound) / (Math.exp(lambda) - 1);
      _theta = lowerBound - _eta;
    }
    _um = new UniformMeshing(nPoints);
    _fpValues = null;
  }

  /**
   * creates a non-uniform set of points according to the formula $x_i = \theta + \eta*\exp(\lambda z_i)$, where the points run from 
   * $x_0$ to $x_{N-1}$ (i.e. there are N points), $\eta = (x_{N-1} - x_0)/(\exp(\lambda) - 1)$ and $\theta = x_0 - \eta$. 
   * The points $z_i$ are are close as possible to uniform on (0,1) while allowing the <em>fixedPoints</em> to be in the set of points.
   * @param lowerBound The value of $x_0$
   * @param upperBound The value of $x_{N-1}$
   * @param nPoints Number of Points (equal to N in the above formula). The number of points must exceed the number of fixed points by at least 2.
   * @param lambda Bunching parameter. $\lambda = 0$ is uniform, $\lambda > 0$ gives a high density of points near $x_0$ and $\lambda < 0$ gives a high density
   * of points near $x_{N-1}$
   * @param fixedPoints set of points that must be included. These must be within the lower and upper bound (exclusive) 
   */
  public ExponentialMeshing(final double lowerBound, final double upperBound, final int nPoints, final double lambda, final double[] fixedPoints) {
    super(nPoints);
    Validate.isTrue(upperBound > lowerBound, "need upperBound>lowerBound");
    ArgumentChecker.notNull(fixedPoints, "null fixedPoints");
    _lambda = lambda;
    _l = lowerBound;
    _r = upperBound;

    _fpValues = FunctionUtils.unique(fixedPoints);

    int m = _fpValues.length;
    final double[] fp = new double[m];

    if (lambda == 0.0) {
      _linear = true;
      _theta = lowerBound;
      _eta = (upperBound - lowerBound);
      for (int ii = 0; ii < m; ii++) {
        fp[ii] = (fixedPoints[ii] - _theta) / _eta;
      }
    } else {
      _linear = false;
      _eta = (upperBound - lowerBound) / (Math.exp(lambda) - 1);
      _theta = lowerBound - _eta;
      for (int ii = 0; ii < m; ii++) {
        fp[ii] = Math.log((_fpValues[ii] - _theta) / _eta) / lambda;
      }
    }
    _um = new UniformMeshing(nPoints, fp);
  }

  @Override
  public Double evaluate(final Integer i) {
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
    if (_linear) {
      return _theta + _eta * z;
    }
    return _theta + _eta * Math.exp(z * _lambda);
  }

}
