package com.opengamma.math.interpolation;

import java.util.Map;
import java.util.TreeMap;

public class LinearInterpolator1D extends Interpolator1D {

  @Override
  public InterpolationResult<Double> interpolate(Map<Double, Double> data, Double value) throws InterpolationException {
    TreeMap<Double, Double> sorted = initData(data);
    Double lowX = getLowerBoundKey(sorted, value);
    Double nextX = sorted.higherKey(lowX);
    Double lowY = sorted.get(lowX);
    Double nextY = sorted.get(nextX);
    double result = lowY + ((value - lowX) / (nextX - lowX)) * (nextY - lowY);
    return new InterpolationResult<Double>(result, 0.);
  }
}
