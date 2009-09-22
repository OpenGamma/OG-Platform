package com.opengamma.math.interpolation;

import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * @author emcleod
 * 
 */

public class ExponentialInterpolator1D extends Interpolator1D {

  @Override
  public InterpolationResult<Double> interpolate(Map<Double, Double> data, Double value) {
    final TreeMap<Double, Double> sorted = initData(data);
    final Double x1 = getLowerBoundKey(sorted, value);
    final Double x2 = sorted.higherKey(x1);
    final Double y1 = sorted.get(x1);
    final Double y2 = sorted.get(x2);
    final double xDiff = x2 - x1;
    final double result = Math.pow(y1, value * (x2 - value) / xDiff / x1) * Math.pow(y2, value * (value - x1) / xDiff / x2);
    return new InterpolationResult<Double>(result);
  }

}
