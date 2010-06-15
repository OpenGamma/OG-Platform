/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Map;

import com.opengamma.util.tuple.Pair;

/**
 * A base class for two-dimensional interpolation.
 */

public abstract class Interpolator2D implements Interpolator<Map<Pair<Double, Double>, Double>, Pair<Double, Double>, Double> {

  /**
   * @param data
   *          A map of (x, y) pairs to z values.
   * @param value
   *          The (x, y) value for which an interpolated value for z is to be
   *          found.
   * @return The value of z
   */
  @Override
  public abstract Double interpolate(Map<Pair<Double, Double>, Double> data, Pair<Double, Double> value);
}
