/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.data;

import org.testng.annotations.Test;

import com.opengamma.math.interpolation.InterpolatorNDTestCase;

/**
 * 
 */
public class RadialBasisFunctionInterpolatorDataBundleTest extends InterpolatorNDTestCase {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction() {
    new RadialBasisFunctionInterpolatorDataBundle(FLAT_DATA, null, false);
  }

}
