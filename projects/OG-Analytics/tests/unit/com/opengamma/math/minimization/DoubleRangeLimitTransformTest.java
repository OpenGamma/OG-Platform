/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.junit.Test;

/**
 * 
 */
public class DoubleRangeLimitTransformTest extends ParameterLimitsTransformTestCase {
  private static final double A = -2.5;
  private static final double B = 1.0;
  private static final ParameterLimitsTransform RANGE_LIMITS = new DoubleRangeLimitTransform(A, B);

  @Test(expected = IllegalArgumentException.class)
  public void testOutOfRange1() {
    RANGE_LIMITS.transform(-3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOutOfRange2() {
    RANGE_LIMITS.transform(1.01);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOutOfRange3() {
    RANGE_LIMITS.transformGradient(-3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOutOfRange4() {
    RANGE_LIMITS.transformGradient(1.01);
  }

  @Test
  public void test() {
    for (int i = 0; i < 10; i++) {
      double x = A + (B - A) * RANDOM.nextDouble();
      double y = 5 * NORMAL.nextRandom();
      testRoundTrip(RANGE_LIMITS, x);
      testReverseRoundTrip(RANGE_LIMITS, y);

      testGradient(RANGE_LIMITS, x);
      testInverseGradient(RANGE_LIMITS, y);

      testGradientRoundTrip(RANGE_LIMITS, x);
    }
  }

}
