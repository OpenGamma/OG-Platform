/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.data;

import org.junit.Test;

import com.opengamma.math.interpolation.InterpolatorNDTestCase;
import com.opengamma.math.interpolation.data.KrigingInterpolatorDataBundle;

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
