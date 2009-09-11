package com.opengamma.math.interpolation;

import java.util.Map;
import java.util.TreeMap;

import com.opengamma.util.Pair;

/**
 * 
 * @author emcleod
 * 
 */

public abstract class Interpolator2D implements Interpolator<Map<Pair<Double, Double>, Double>, Pair<Double, Double>, Double> {

  @Override
  public abstract InterpolationResult<Double> interpolate(Map<Pair<Double, Double>, Double> data, Pair<Double, Double> value) throws InterpolationException;

  protected TreeMap<Double, Double> initData(Map<Pair<Double, Double>, Double> data) {
    return null;
  }

  private void checkData(Map<Pair<Double, Double>, Double> data) {
    if (data == null)
      throw new IllegalArgumentException("Data map was null");
    if (data.size() < 4)
      throw new IllegalArgumentException("Need at least four points to perform interpolation");
  }
}
