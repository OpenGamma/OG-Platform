package com.opengamma.math.interpolation;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class CubicSplineInterpolator1D extends Interpolator1D {

  @Override
  public InterpolationResult<Double> interpolate(Map<Double, Double> data, Double value) {
    final TreeMap<Double, Double> sorted = initData(data);
    final Double lowX = getLowerBoundKey(sorted, value);
    final Double nextX = sorted.higherKey(lowX);
    final Double lowY = sorted.get(lowX);
    final Double nextY = sorted.get(nextX);
    final double diff = nextY - lowY;
    if (Math.abs(diff) < EPS) {
      throw new InterpolationException("Points were not distinct: " + nextX + " and " + lowX);
    }
    final double a = (nextX - value) / diff;
    final double b = (value - lowX) / diff;
    final Map<Double, Double> secondDeriv = getSecondDerivatives(sorted);
    final double y = a * lowY + b * nextY + (a * a * a - a) * secondDeriv.get(lowX) + (b * b * b - b) * secondDeriv.get(nextX) * diff * diff / 6.;
    return new InterpolationResult<Double>(y);
  }

  private Map<Double, Double> getSecondDerivatives(TreeMap<Double, Double> data) {
    final TreeMap<Double, Double> result = new TreeMap<Double, Double>();
    final TreeMap<Double, Double> u = new TreeMap<Double, Double>();
    double previousX, x, nextX, previousY, y, nextY;
    Iterator<Map.Entry<Double, Double>> iter = data.entrySet().iterator();
    Iterator<Map.Entry<Double, Double>> uIter = u.entrySet().iterator();
    Map.Entry<Double, Double> entry = iter.next();
    Map.Entry<Double, Double> uEntry;
    previousX = entry.getKey();
    previousY = entry.getValue();
    entry = iter.next();
    x = entry.getKey();
    y = entry.getValue();
    u.put(previousX, 0.);
    double a, b, c;
    while (iter.hasNext()) {
      entry = iter.next();
      uEntry = uIter.next();
      nextX = entry.getKey();
      nextY = entry.getValue();
      a = (x - previousX) / (nextX - x);
      b = a * previousY + 2.;
      c = (nextY - y) / (nextX - x) - (y - previousY) / (x - previousX);
      c = (6. * c / (nextX - previousX) - a * uEntry.getValue()) / b;
      result.put(x, a - 1. / b);
      u.put(x, c);
      previousX = x;
      previousY = y;
      x = nextX;
      y = nextY;
    }
    result.put(data.lastKey(), 0.);
    iter = result.entrySet().iterator();
    uIter = u.entrySet().iterator();
    entry = iter.next();
    double secondDeriv = entry.getValue();
    iter.next();
    while (iter.hasNext()) {
      entry = iter.next();
      uEntry = uIter.next();
      secondDeriv = secondDeriv * entry.getValue() + uEntry.getValue();
      result.put(uEntry.getKey(), secondDeriv);
    }
    return result;
  }
}
