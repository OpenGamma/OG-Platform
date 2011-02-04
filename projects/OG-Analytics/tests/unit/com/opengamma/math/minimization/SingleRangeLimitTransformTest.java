/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.junit.Test;

import com.opengamma.math.minimization.ParameterLimitsTransform.LimitType;

/**
 * 
 */
public class SingleRangeLimitTransformTest extends ParameterLimitsTransformTestCase {

  private static final double A = -2.5;
  private static final double B = 1.0;
  private static final ParameterLimitsTransform LOWER_LIMIT = new SingleRangeLimitTransform(B, LimitType.GREATER_THAN);
  private static final ParameterLimitsTransform UPPER_LIMIT = new SingleRangeLimitTransform(A, LimitType.LESS_THAN);

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
    LOWER_LIMIT.transformGradient(-3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOutOfRange4() {
    UPPER_LIMIT.transformGradient(1.01);
  }

  @Test
  public void testLower() {
    for (int i = 0; i < 10; i++) {
      final double x = B - 5 * Math.log(RANDOM.nextDouble());
      final double y = 5 * NORMAL.nextRandom();
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
      final double x = A + 5 * Math.log(RANDOM.nextDouble());
      final double y = 5 * NORMAL.nextRandom();
      testRoundTrip(UPPER_LIMIT, x);
      testReverseRoundTrip(UPPER_LIMIT, y);
      testGradient(UPPER_LIMIT, x);
      testInverseGradient(UPPER_LIMIT, y);
      testGradientRoundTrip(UPPER_LIMIT, x);
    }
  }
}
