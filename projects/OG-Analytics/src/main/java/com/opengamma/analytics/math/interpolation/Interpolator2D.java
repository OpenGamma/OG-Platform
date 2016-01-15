/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.io.Serializable;
import java.util.Map;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.tuple.DoublesPair;

/**
 * A base class for two-dimensional interpolation.
 */
public abstract class Interpolator2D implements Interpolator<Map<Double, Interpolator1DDataBundle>, DoublesPair>, Serializable {

  /**
   * @param dataBundle
   *          A map of (x, y) pairs to z values.
   * @param value
   *          The (x, y) value for which an interpolated value for z is to be
   *          found.
   * @return The value of z
   */
  @Override
  public abstract Double interpolate(Map<Double, Interpolator1DDataBundle> dataBundle, DoublesPair value);
  
  public abstract Map<DoublesPair, Double> getNodeSensitivitiesForValue(final Map<Double, Interpolator1DDataBundle> dataBundle, final DoublesPair value);

}
