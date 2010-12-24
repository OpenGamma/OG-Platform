/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class InterpolatorNDDataBundleTest {

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    new InterpolatorNDDataBundle(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyData() {
    List<Pair<double[], Double>> data = new ArrayList<Pair<double[], Double>>();
    new InterpolatorNDDataBundle(data);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyData2() {
    List<Pair<double[], Double>> data = new ArrayList<Pair<double[], Double>>();
    double[] temp = new double[] {};
    Pair<double[], Double> pair = new ObjectsPair<double[], Double>(temp, 0.0);
    data.add(pair);
    new InterpolatorNDDataBundle(data);
  }

}
