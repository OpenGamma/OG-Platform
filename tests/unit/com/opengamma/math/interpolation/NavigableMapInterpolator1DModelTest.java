/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.junit.Test;

/**
 * 
 */
public class NavigableMapInterpolator1DModelTest extends Interpolator1DModelTestCase {

  @Override
  protected Interpolator1DModel createModel(double[] keys, double[] values) {
    NavigableMap<Double, Double> map = new TreeMap<Double, Double>();
    for (int i = 0; i < keys.length; i++) {
      map.put(keys[i], values[i]);
    }
    return new NavigableMapInterpolator1DModel(map);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void nullInputs() {
    new NavigableMapInterpolator1DModel(null);
  }

}
