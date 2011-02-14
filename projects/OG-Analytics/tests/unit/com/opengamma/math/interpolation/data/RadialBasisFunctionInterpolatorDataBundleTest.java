/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.data;

import org.junit.Test;

import com.opengamma.math.interpolation.InterpolatorNDTestCase;
import com.opengamma.math.interpolation.data.RadialBasisFunctionInterpolatorDataBundle;

/**
 * 
 */
public class RadialBasisFunctionInterpolatorDataBundleTest extends InterpolatorNDTestCase {

  @Test(expected = IllegalArgumentException.class)
  public void testNullFunction() {
    new RadialBasisFunctionInterpolatorDataBundle(FLAT_DATA, null, false);
  }

}
