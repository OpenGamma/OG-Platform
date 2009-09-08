package com.opengamma.math.interpolation;

import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * @author emcleod
 * 
 */

public abstract class Interpolator1D implements Interpolator<Map<Double, Double>, Double, Double> {
  protected static final double EPS = 1e-12;

  public abstract InterpolationResult<Double> interpolate(Map<Double, Double> data, Double value) throws InterpolationException;

  protected TreeMap<Double, Double> initData(Map<Double, Double> data) {
    checkData(data);
    return new TreeMap<Double, Double>(data);
  }

  protected Double getLowerBoundKey(TreeMap<Double, Double> data, Double value) throws InterpolationException {
    Double lower = data.floorKey(value);
    if (lower == null)
      throw new InterpolationException("Value was less than the lowest data point for x");
    if (lower.equals(data.lastKey()))
      throw new InterpolationException("Value was greater than the largest data point for x");
    return lower;
  }

  private void checkData(Map<Double, Double> data) {
    if (data == null)
      throw new IllegalArgumentException("Data map was null");
    if (data.size() < 2)
      throw new IllegalArgumentException("Need at least two points to perform interpolation");
  }
}
