/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class NullTransformTest extends ParameterLimitsTransformTestCase {
  private static final ParameterLimitsTransform NULL_TRANSFORM = new NullTransform();

  @Test
  public void test() {
    for (int i = 0; i < 10; i++) {
      final double y = 5 * NORMAL.nextRandom();
      testRoundTrip(NULL_TRANSFORM, y);
      testReverseRoundTrip(NULL_TRANSFORM, y);
      testGradient(NULL_TRANSFORM, y);
      testInverseGradient(NULL_TRANSFORM, y);
      testGradientRoundTrip(NULL_TRANSFORM, y);
    }
  }

  @Test
  public void testHashCodeAndEquals() {
    ParameterLimitsTransform other = new NullTransform();
    assertEquals(other, NULL_TRANSFORM);
    assertEquals(other.hashCode(), NULL_TRANSFORM.hashCode());
    other = new ParameterLimitsTransform() {

      @Override
      public double transformGradient(final double x) {
        return 0;
      }

      @Override
      public double transform(final double x) {
        return 0;
      }

      @Override
      public double inverseTransformGradient(final double y) {
        return 0;
      }

      @Override
      public double inverseTransform(final double y) {
        return 0;
      }
    };
    assertFalse(other.equals(NULL_TRANSFORM));
  }
}
