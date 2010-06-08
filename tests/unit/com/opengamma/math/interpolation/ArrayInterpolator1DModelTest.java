/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

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

}
