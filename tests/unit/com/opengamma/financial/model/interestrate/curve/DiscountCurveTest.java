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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;

import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.time.DateUtil;

/**
 * 
 * @author emcleod
 */
public class DiscountCurveTest {
  private static final Date DATE = DateUtil.date(20090901);
  private static final Interpolator1D INTERPOLATOR = new LinearInterpolator1D();
  private static final Double EPS = 1e-15;

  @Test
  public void test() {
    final Map<Double, Double> data = new HashMap<Double, Double>();
    data.put(1., 0.05);
    data.put(2., 0.06);
    data.put(3., 0.07);
    final DiscountCurve curve = new DiscountCurve(DATE, data, INTERPOLATOR);
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
    final Double rate = curve.getInterestRate(t);
    assertEquals(INTERPOLATOR.interpolate(data, t).getResult(), rate, EPS);
    assertEquals(Math.exp(-INTERPOLATOR.interpolate(data, t).getResult() * t), curve.getDiscountFactor(t), EPS);
  }

  @Test
  public void testConstructors() {
    try {
      new DiscountCurve(null, null, null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new DiscountCurve(null, new HashMap<Double, Double>(), null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new DiscountCurve(DATE, Collections.<Double, Double> singletonMap(-4., 3.), null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final SortedMap<Double, Double> data = new TreeMap<Double, Double>();
    data.put(1., 0.03);
    data.put(2., 0.05);
    data.put(3., 0.045);
    data.put(4., 0.07);
    final DiscountCurve curve1 = new DiscountCurve(DATE, data, INTERPOLATOR);
    final SortedMap<Double, Double> sorted = curve1.getData();
    data.put(1., 0.05);
    assertFalse(data.equals(sorted));
  }
}
