/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Map;

import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.tuple.DoublesPair;

/**
 * A base class for two-dimensional interpolation.
 * @param <T>
 */
public abstract class Interpolator2D<T extends Interpolator1DDataBundle> implements Interpolator<Map<Double, T>, DoublesPair> {

  /**
   * @param dataBundle
   *          A map of (x, y) pairs to z values.
   * @param value
   *          The (x, y) value for which an interpolated value for z is to be
   *          found.
   * @return The value of z
   */
  @Override
  public abstract Double interpolate(Map<Double, T> dataBundle, DoublesPair value);
}
