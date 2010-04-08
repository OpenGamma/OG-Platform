/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.InterpolatedDiscountCurve;
import com.opengamma.math.interpolation.Interpolator1DFactory;

public class ModelInterestRateCurveTest extends AnalyticsTestBase {
  
  @Test
  public void testInterpolatedDiscountCurve () {
    final Map<Double,Double> map = new HashMap<Double,Double> ();
    map.put(1., 0.03);
    map.put(2., 0.04);
    map.put(3., 0.05);
    InterpolatedDiscountCurve dc1 = new InterpolatedDiscountCurve (map, Interpolator1DFactory.getInterpolator ("Linear"));
    InterpolatedDiscountCurve dc2 = cycleObject (InterpolatedDiscountCurve.class, dc1);
    assertEquals (dc1, dc2);
  }
  
}