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

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.math.interpolation.Interpolator1DFactory;

/**
 * Test ConstantYieldCurve/InterpolatedYieldCurve.
 */
public class ModelInterestRateCurveTest extends AnalyticsTestBase {

  @Test
  public void testConstantInterestRateDiscountCurve() {
    ConstantYieldCurve dc1 = new ConstantYieldCurve(0.05);
    ConstantYieldCurve dc2 = cycleObject(ConstantYieldCurve.class, dc1);
    assertEquals(dc1, dc2);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testInterpolatedDiscountCurve() {
    final Map<Double, Double> map = new HashMap<Double, Double>();
    map.put(1., 0.03);
    map.put(2., 0.04);
    map.put(3., 0.05);
    InterpolatedYieldCurve dc1 = new InterpolatedYieldCurve(map, Interpolator1DFactory.getInterpolator("Linear"));
    InterpolatedYieldCurve dc2 = cycleObject(InterpolatedYieldCurve.class, dc1);
    assertEquals(dc1, dc2);
  }

}
