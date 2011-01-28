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
public class NullTransformTest extends ParameterLimitsTransformTestCase {
  
  private static final ParameterLimitsTransform NULL_TRANSFORM = new NullTransform();

  
  @Test
  public void test() {
    for (int i = 0; i < 10; i++) {
      
      double y = 5 * NORMAL.nextRandom();
      testRoundTrip(NULL_TRANSFORM, y);
      testReverseRoundTrip(NULL_TRANSFORM, y);

      testGradient(NULL_TRANSFORM, y);
      testInverseGradient(NULL_TRANSFORM, y);

      testGradientRoundTrip(NULL_TRANSFORM, y);
    }
  }

}
