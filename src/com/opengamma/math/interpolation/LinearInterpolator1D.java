/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Map;
import java.util.NavigableMap;

/**
 * A one-dimensional linear interpolator. The interpolated value of the function
 * <i>y</i> at <i>x</i> between two data points <i>(x<sub>1</sub>,
 * y<sub>1</sub>)</i> and <i>(x<sub>2</sub>, y<sub>2</sub>)</i> is given by:<br>
 * <i>y = y<sub>1</sub> + (x - x<sub>1</sub>) * (y<sub>2</sub> - y<sub>1</sub>)
 * / (x<sub>2</sub> - x<sub>1</sub>)</i>
 */
public class LinearInterpolator1D extends Interpolator1D {

  /**
   * 
   * @param data
   *          A map containing the (x, y) data points.
   * @param value
   *          The value of x for which the interpolated point y is required.
   * @return An InterpolationResult containing the value of the interpolated
   *         point and an interpolation error of zero (linear interpolation is
   *         by definition exact).
   * @throws IllegalArgumentException
   *           If the x-value is null.
   */
  @Override
  public InterpolationResult<Double> interpolate(final Map<Double, Double> data, final Double value) {
    if (value == null) {
      throw new IllegalArgumentException("x value to be interpolated was null");
    }
    final NavigableMap<Double, Double> sorted = initData(data);
    final Double x1 = getLowerBoundKey(sorted, value);
    if (x1.equals(sorted.lastKey())) {
      return new InterpolationResult<Double>(sorted.lastEntry().getValue());
    }
    final Double x2 = sorted.higherKey(x1);
    final Double y1 = sorted.get(x1);
    final Double y2 = sorted.get(x2);
    final double result = y1 + (value - x1) / (x2 - x1) * (y2 - y1);
    return new InterpolationResult<Double>(result);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof LinearInterpolator1D)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

}
