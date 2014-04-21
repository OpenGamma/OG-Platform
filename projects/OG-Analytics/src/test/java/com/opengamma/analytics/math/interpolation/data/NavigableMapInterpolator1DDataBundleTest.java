/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation.data;

import java.util.NavigableMap;
import java.util.TreeMap;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NavigableMapInterpolator1DDataBundleTest extends Interpolator1DDataBundleTestCase {

  @Override
  protected Interpolator1DDataBundle createDataBundle(final double[] keys, final double[] values) {
    final NavigableMap<Double, Double> map = new TreeMap<>();
    for (int i = 0; i < keys.length; i++) {
      map.put(keys[i], values[i]);
    }
    return new NavigableMapInterpolator1DDataBundle(map);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullInputs() {
    new NavigableMapInterpolator1DDataBundle(null);
  }

}
