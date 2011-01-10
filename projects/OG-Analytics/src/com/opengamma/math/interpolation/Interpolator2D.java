/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Map;

import com.opengamma.util.tuple.DoublesPair;

/**
 * A base class for two-dimensional interpolation.
 */

public abstract class Interpolator2D implements Interpolator<Map<DoublesPair, Double>, DoublesPair> {

  /**
   * @param data
   *          A map of (x, y) pairs to z values.
   * @param value
   *          The (x, y) value for which an interpolated value for z is to be
   *          found.
   * @return The value of z
   */
  @Override
  public abstract Double interpolate(Map<DoublesPair, Double> data, DoublesPair value);
}
