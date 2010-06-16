/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 */
public class ArrayInterpolator1DModelTest extends Interpolator1DModelTestCase {

  @Override
  protected Interpolator1DModel createModel(double[] keys, double[] values) {
    return new ArrayInterpolator1DModel(keys, values);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void nullKeys() {
    new ArrayInterpolator1DModel(null, new double[] {1., 2.});
  }
  
  @Test
  public void particularSort() {
    double[] keys = new double[] {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1};
    double[] values = new double[] {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1};
    
    Interpolator1DModel model = new ArrayInterpolator1DModel(keys, values);
    double[] resultKeys = model.getKeys();
    assertEquals(0.0, resultKeys[0], 1e-10);
  }

  @Test
  public void brokenSort_ANA_102() {
    double[] keys = new double[] {0.4, 0.3, 0.2, 0.1, 0.0, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1};
    double[] values = new double[] {0.4, 0.3, 0.2, 0.1, 0.0, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1};
    Interpolator1DModel model = new ArrayInterpolator1DModel(keys, values);
    // If the array isn't sorted properly, the binary search doesn't find the keys
    for (double key : keys) {
      assertTrue (model.containsKey (key));
    }
  }

}
