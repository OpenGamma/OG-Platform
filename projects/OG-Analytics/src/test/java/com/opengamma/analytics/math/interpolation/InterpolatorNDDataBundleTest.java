/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.data.InterpolatorNDDataBundle;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class InterpolatorNDDataBundleTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    new InterpolatorNDDataBundle(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyData() {
    final List<Pair<double[], Double>> data = new ArrayList<>();
    new InterpolatorNDDataBundle(data);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyData2() {
    final List<Pair<double[], Double>> data = new ArrayList<>();
    final double[] temp = new double[] {};
    final Pair<double[], Double> pair = Pairs.of(temp, 0.0);
    data.add(pair);
    new InterpolatorNDDataBundle(data);
  }

}
