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
public class SingleRangeLimitTransformTest extends ParameterLimitsTransformTestCase {

  private static final double A = -2.5;
  private static final double B = 1.0;
  private static final ParameterLimitsTransform LOWER_LIMIT = new SingleRangeLimitTransform(B, true);
  private static final ParameterLimitsTransform UPPER_LIMIT = new SingleRangeLimitTransform(A, false);

  @Test(expected = IllegalArgumentException.class)
  public void testOutOfRange1() {
    LOWER_LIMIT.transform(-3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOutOfRange2() {
    UPPER_LIMIT.transform(1.01);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOutOfRange3() {
    LOWER_LIMIT.transformGrdient(-3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOutOfRange4() {
    UPPER_LIMIT.transformGrdient(1.01);
  }

  @Test
  public void testLower() {
    for (int i = 0; i < 10; i++) {
      double x = B - 5 * Math.log(RANDOM.nextDouble());
      double y = 5 * NORMAL.nextRandom();
      testRoundTrip(LOWER_LIMIT, x);
      testReverseRoundTrip(LOWER_LIMIT, y);
      testGradient(LOWER_LIMIT, x);
      testInverseGradient(LOWER_LIMIT, y);
      testGradientRoundTrip(LOWER_LIMIT, x);
    }
  }

  @Test
  public void testUpper() {
    for (int i = 0; i < 10; i++) {
      double x = A + 5 * Math.log(RANDOM.nextDouble());
      double y = 5 * NORMAL.nextRandom();
      testRoundTrip(UPPER_LIMIT, x);
      testReverseRoundTrip(UPPER_LIMIT, y);
      testGradient(UPPER_LIMIT, x);
      testInverseGradient(UPPER_LIMIT, y);
      testGradientRoundTrip(UPPER_LIMIT, x);
    }
  }
}
