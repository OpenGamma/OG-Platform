package com.opengamma.math.interpolation;

import java.util.Map;
import java.util.TreeMap;

public class CubicSplineInterpolator1D extends Interpolator1D {

  @Override
  public InterpolationResult<Double> interpolate(Map<Double, Double> data, Double value) throws InterpolationException {
    TreeMap<Double, Double> sorted = initData(data);
    Double lowX = getLowerBoundKey(sorted, value);
    Double nextX = sorted.higherKey(lowX);
    Double lowY = sorted.get(lowX);
    Double nextY = sorted.get(nextX);
    double diff = nextY - lowY;
    if (Math.abs(diff) < EPS)
      throw new InterpolationException("Points were not distinct: " + nextX + " and " + lowX);
    double a = (nextX - value) / diff;
    double b = (value - lowX) / diff;
    Map<Double, Double> secondDeriv = getSecondDerivatives(sorted);
    double y = a * lowY + b * nextY + (a * a * a - a) * secondDeriv.get(lowX) + (b * b * b - b) * secondDeriv.get(nextX) * (diff * diff) / 6.;
    return new InterpolationResult<Double>(y);
  }

  private Map<Double, Double> getSecondDerivatives(TreeMap<Double, Double> data) {
    TreeMap<Double, Double> result = new TreeMap<Double, Double>();
    int n = data.size();
    double a, b;
    double previousU = 0, u, nextU;
    for (int i = 1; i < n; i++) {

    }
    return result;
  }
}
