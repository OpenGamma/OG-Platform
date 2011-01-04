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
public class KrigingInterpolatorDataBundleTest extends InterpolatorNDTestCase {

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    new KrigingInterpolatorDataBundle(null, 1.5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLowBeta() {
    new KrigingInterpolatorDataBundle(COS_EXP_DATA, -1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighBeta() {
    new KrigingInterpolatorDataBundle(COS_EXP_DATA, 2);
  }

}
