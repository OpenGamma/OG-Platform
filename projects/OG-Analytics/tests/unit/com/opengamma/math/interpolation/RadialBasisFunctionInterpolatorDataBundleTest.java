/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
