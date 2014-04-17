/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.Barrier;
import com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.ObservationType;
import com.opengamma.analytics.financial.model.option.definition.EuropeanStandardBarrierOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EuropeanStandardBarrierOptionModelTest {
  private static final double SPOT = 100;
  private static final double REBATE = 3;
  private static final YieldAndDiscountCurve R = YieldCurve.from(ConstantDoublesCurve.from(0.08));
  private static final double B = 0.04;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final AnalyticOptionModel<EuropeanStandardBarrierOptionDefinition, StandardOptionDataBundle> MODEL = new EuropeanStandardBarrierOptionModel();
  private static final double EPS = 1e-4;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(new EuropeanStandardBarrierOptionDefinition(SPOT, EXPIRY, true, new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, SPOT))).evaluate(
        (StandardOptionDataBundle) null);
  }

  @Test
  public void testZeroVol() {
    final double delta = 10;
    final StandardOptionDataBundle data = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.)), 0, new VolatilitySurface(ConstantDoublesSurface.from(0.)), SPOT, DATE);
    Barrier barrier = new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, 95);
    EuropeanStandardBarrierOptionDefinition option = new EuropeanStandardBarrierOptionDefinition(SPOT - delta, EXPIRY, true, barrier, REBATE);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 10, 0);
    barrier = new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, 105);
    option = new EuropeanStandardBarrierOptionDefinition(SPOT - delta, EXPIRY, true, barrier, REBATE);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), REBATE, 0);
  }

  @Test
  public void test() {
    final StandardOptionDataBundle data = new StandardOptionDataBundle(R, B, new VolatilitySurface(ConstantDoublesSurface.from(0.25)), SPOT, DATE);
    Barrier barrier = new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, 95);
    EuropeanStandardBarrierOptionDefinition option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, true, barrier, REBATE);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 9.0246, EPS);
    barrier = new Barrier(KnockType.OUT, BarrierType.UP, ObservationType.CONTINUOUS, 105);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, true, barrier, REBATE);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 2.6789, EPS);
    barrier = new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, 95);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, false, barrier, REBATE);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 2.2798, EPS);
    barrier = new Barrier(KnockType.OUT, BarrierType.UP, ObservationType.CONTINUOUS, 105);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, false, barrier, REBATE);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 3.7760, EPS);
    barrier = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, 95);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, true, barrier, REBATE);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 7.7627, EPS);
    barrier = new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, 105);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, true, barrier, REBATE);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 14.1112, EPS);
    barrier = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, 95);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, false, barrier, REBATE);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 2.9586, EPS);
    barrier = new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, 105);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, false, barrier, REBATE);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 1.4653, EPS);
  }
}
