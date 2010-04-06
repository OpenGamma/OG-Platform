/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Map;
import java.util.TreeMap;

/**
 * A one-dimensional linear interpolator. The interpolated value of the function
 * <i>y</i> at <i>x</i> between two data points <i>(x<sub>1</sub>,
 * y<sub>1</sub></i> and <i>(x<sub>2</sub>, y<sub>2</sub></i> is given by:<br>
 * <i>y = y<sub>1</sub> (y<sub>2</sub> / y<sub>1</sub>) ^ ((x - x<sub>1</sub>) /
 * (x<sub>2</sub> - x<sub>1</sub>))<br>
 * It is the equivalent of performing a linear interpolation on a data set after
 * taking the logarithm of the y-values.
 * 
 * @author emcleod
 */

public class LogLinearInterpolator1D extends Interpolator1D {

  /**
   * 
   * @param data
   *          A map containing the (x, y) data points.
   * @param value
   *          The value of x for which the interpolated point y is required.
   * @return An InterpolationResult containing the value of the interpolated
   *         point and an interpolation error of zero (log-linear interpolation
   *         is by definition exact).
   * @throws IllegalArgumentException
   *           If the x-value is null.
   */
  @Override
  public InterpolationResult<Double> interpolate(final Map<Double, Double> data, final Double value) {
    if (value == null)
      throw new IllegalArgumentException("x value was null");
    final TreeMap<Double, Double> sorted = initData(data);
    final Double x1 = getLowerBoundKey(sorted, value);
    if (x1.equals(sorted.lastKey()))
      return new InterpolationResult<Double>(sorted.lastEntry().getValue());
    final Double x2 = sorted.higherKey(x1);
    final Double y1 = sorted.get(x1);
    final Double y2 = sorted.get(x2);
    final double result = Math.pow(y2 / y1, (value - x1) / (x2 - x1)) * y1;
    return new InterpolationResult<Double>(result);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null)
      return false;
    if (o == this)
      return true;
    if (!(o instanceof LogLinearInterpolator1D))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

}
