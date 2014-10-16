/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveSimple;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroSpreadCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.test.TestGroup;

/**
 * Test YieldCurve/DiscountCurve.
 */
@Test(groups = TestGroup.UNIT)
public class ModelInterestRateCurveTest extends AnalyticsTestBase {

  @Test
  public void testConstantYieldCurve() {
    final YieldCurve dc1 = new YieldCurve("Curve name-Yld", ConstantDoublesCurve.from(0.05));
    final YieldCurve dc2 = cycleObject(YieldCurve.class, dc1);
    assertEquals(dc1, dc2);
  }

  @Test
  public void testInterpolatedDiscountCurve() {
    final Map<Double, Double> map = new HashMap<>();
    map.put(1., 0.03);
    map.put(2., 0.04);
    map.put(3., 0.05);
    final DiscountCurve dc1 = new DiscountCurve("Curve name-dsc",InterpolatedDoublesCurve.from(map, Interpolator1DFactory.getInterpolator("Linear")));
    final DiscountCurve dc2 = cycleObject(DiscountCurve.class, dc1);
    assertEquals(dc1, dc2);
  }

  @Test
  public void testInterpolatedYieldCurve() {
    final Map<Double, Double> map = new HashMap<>();
    map.put(1., 0.03);
    map.put(2., 0.04);
    map.put(3., 0.05);
    final YieldCurve dc1 = new YieldCurve("Curve name-Yld", InterpolatedDoublesCurve.from(map, Interpolator1DFactory.getInterpolator("Linear")));
    final YieldCurve dc2 = cycleObject(YieldCurve.class, dc1);
    assertEquals(dc1, dc2);
  }

  @Test
  public void testPriceIndexCurve() {
    final Map<Double, Double> map = new HashMap<>();
    map.put(1., 0.03);
    map.put(2., 0.04);
    map.put(3., 0.05);
    final PriceIndexCurve curve = new PriceIndexCurveSimple(InterpolatedDoublesCurve.from(map, Interpolator1DFactory.getInterpolator("Linear")));
    assertEquals(curve, cycleObject(PriceIndexCurve.class, curve));
  }

  @Test
  public void testYieldAndDiscountAddZeroSpreadCurve() {
    YieldAndDiscountAddZeroSpreadCurve curve =
        new YieldAndDiscountAddZeroSpreadCurve("name", false, YieldCurve.from(ConstantDoublesCurve.from(0.1)));
    assertEquals(curve, cycleObject(YieldAndDiscountAddZeroSpreadCurve.class, curve));
  }
}
