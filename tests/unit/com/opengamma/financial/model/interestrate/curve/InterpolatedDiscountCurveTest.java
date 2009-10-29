/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;

import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.LinearInterpolator1D;

/**
 * 
 * @author emcleod
 */
public class InterpolatedDiscountCurveTest {
  private static final Interpolator1D INTERPOLATOR = new LinearInterpolator1D();
  private static final Double EPS = 1e-15;

  @Test
  public void test() {
    final Map<Double, Double> data = new HashMap<Double, Double>();
    data.put(1., 0.05);
    data.put(2., 0.06);
    data.put(3., 0.07);
    final Map<Double, Double> dfData = new HashMap<Double, Double>();
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      dfData.put(entry.getKey(), Math.exp(-entry.getKey() * entry.getValue()));
    }
    final InterpolatedDiscountCurve curve = new InterpolatedDiscountCurve(data, INTERPOLATOR);
    try {
      curve.getInterestRate(-1.);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      curve.getDiscountFactor(-1.);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final double t = 1.5;
    final Double df = curve.getDiscountFactor(t);
    assertEquals(INTERPOLATOR.interpolate(dfData, t).getResult(), df, EPS);
    assertEquals(-Math.log(INTERPOLATOR.interpolate(dfData, t).getResult()) / t, curve.getInterestRate(t), EPS);
  }

  @Test
  public void testConstructors() {
    // TODO kirk 2009-09-24 -- Should be using @Test(expected=) form for the
    // throwing ones.
    try {
      new InterpolatedDiscountCurve(null, null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new InterpolatedDiscountCurve(new HashMap<Double, Double>(), null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new InterpolatedDiscountCurve(Collections.<Double, Double> singletonMap(-4., 3.), null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final SortedMap<Double, Double> data = new TreeMap<Double, Double>();
    data.put(1., 0.03);
    data.put(2., 0.05);
    data.put(3., 0.045);
    data.put(4., 0.07);
    final InterpolatedDiscountCurve curve1 = new InterpolatedDiscountCurve(data, INTERPOLATOR);
    final SortedMap<Double, Double> sorted = curve1.getData();
    data.put(1., 0.05);
    assertFalse(data.equals(sorted));
  }
}
