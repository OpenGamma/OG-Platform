package com.opengamma.math.interpolation;

import java.util.Map;

import com.opengamma.util.Pair;

/**
 * 
 * @author emcleod
 * 
 */

public abstract class Interpolator2D implements Interpolator<Map<Pair<Double, Double>, Double>, Pair<Double, Double>, Double> {

  @Override
  public abstract InterpolationResult<Double> interpolate(Map<Pair<Double, Double>, Double> data, Pair<Double, Double> value) throws InterpolationException;

}
