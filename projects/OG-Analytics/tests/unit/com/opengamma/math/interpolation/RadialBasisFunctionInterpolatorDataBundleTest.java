/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.junit.Test;

/**
 * 
 */
public class RadialBasisFunctionInterpolatorDataBundleTest extends InterpolatorNDTestCase {

  @Test(expected = IllegalArgumentException.class)
  public void testNullFunction() {
    new RadialBasisFunctionInterpolatorDataBundle(FLAT_DATA, null, false);
  }

}
