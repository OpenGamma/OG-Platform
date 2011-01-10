/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * 
 */
public class Interpolator1DFactoryTest {

  @Test(expected = IllegalArgumentException.class)
  public void testBadName() {
    Interpolator1DFactory.getInterpolator("x");
  }

  @Test
  public void testNullCalculator() {
    assertNull(Interpolator1DFactory.getInterpolatorName(null));
  }

  @Test
  public void test() {
    assertEquals(Interpolator1DFactory.LINEAR, Interpolator1DFactory.getInterpolatorName(Interpolator1DFactory.getInterpolator(Interpolator1DFactory.LINEAR)));
    assertEquals(Interpolator1DFactory.EXPONENTIAL, Interpolator1DFactory.getInterpolatorName(Interpolator1DFactory.getInterpolator(Interpolator1DFactory.EXPONENTIAL)));
    assertEquals(Interpolator1DFactory.LOG_LINEAR, Interpolator1DFactory.getInterpolatorName(Interpolator1DFactory.getInterpolator(Interpolator1DFactory.LOG_LINEAR)));
    assertEquals(Interpolator1DFactory.NATURAL_CUBIC_SPLINE, Interpolator1DFactory.getInterpolatorName(Interpolator1DFactory.getInterpolator(Interpolator1DFactory.NATURAL_CUBIC_SPLINE)));
    assertEquals(Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.getInterpolatorName(Interpolator1DFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC)));
    assertEquals(Interpolator1DFactory.STEP, Interpolator1DFactory.getInterpolatorName(Interpolator1DFactory.getInterpolator(Interpolator1DFactory.STEP)));
  }
}
