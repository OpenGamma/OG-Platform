/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.Barrier;
import com.opengamma.financial.model.option.definition.EuropeanStandardBarrierOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class EuropeanStandardBarrierOptionModelTest {
  private static final double SPOT = 100;
  private static final double REBATE = 3;
  private static final YieldAndDiscountCurve R = new ConstantYieldCurve(0.08);
  private static final double B = 0.04;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final AnalyticOptionModel<EuropeanStandardBarrierOptionDefinition, StandardOptionDataBundle> MODEL = new EuropeanStandardBarrierOptionModel();

  @Test
  public void test() {
    final StandardOptionDataBundle data = new StandardOptionDataBundle(R, B, new ConstantVolatilitySurface(0.25), SPOT, DATE);
    Barrier barrier = new Barrier(KnockType.OUT, BarrierType.DOWN, 95);
    EuropeanStandardBarrierOptionDefinition option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, true, barrier, REBATE);
    System.out.println(MODEL.getPricingFunction(option).evaluate(data));
    barrier = new Barrier(KnockType.OUT, BarrierType.UP, 105);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, true, barrier, REBATE);
    System.out.println(MODEL.getPricingFunction(option).evaluate(data));
    barrier = new Barrier(KnockType.OUT, BarrierType.DOWN, 95);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, false, barrier, REBATE);
    System.out.println(MODEL.getPricingFunction(option).evaluate(data));
    barrier = new Barrier(KnockType.OUT, BarrierType.UP, 105);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, false, barrier, REBATE);
    System.out.println(MODEL.getPricingFunction(option).evaluate(data));
    barrier = new Barrier(KnockType.IN, BarrierType.DOWN, 95);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, true, barrier, REBATE);
    System.out.println(MODEL.getPricingFunction(option).evaluate(data));
    barrier = new Barrier(KnockType.IN, BarrierType.UP, 105);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, true, barrier, REBATE);
    System.out.println(MODEL.getPricingFunction(option).evaluate(data));
    barrier = new Barrier(KnockType.IN, BarrierType.DOWN, 95);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, false, barrier, REBATE);
    System.out.println(MODEL.getPricingFunction(option).evaluate(data));
    barrier = new Barrier(KnockType.IN, BarrierType.UP, 105);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, false, barrier, REBATE);
    System.out.println(MODEL.getPricingFunction(option).evaluate(data));
  }

  @Test
  public void test1() {
    System.out.println("-----------------");
    final StandardOptionDataBundle data = new StandardOptionDataBundle(R, B, new ConstantVolatilitySurface(0.3), SPOT, DATE);
    Barrier barrier = new Barrier(KnockType.OUT, BarrierType.DOWN, 95);
    EuropeanStandardBarrierOptionDefinition option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, true, barrier, REBATE);
    System.out.println(MODEL.getPricingFunction(option).evaluate(data));
    barrier = new Barrier(KnockType.OUT, BarrierType.UP, 105);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, true, barrier, REBATE);
    System.out.println(MODEL.getPricingFunction(option).evaluate(data));
    barrier = new Barrier(KnockType.OUT, BarrierType.DOWN, 95);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, false, barrier, REBATE);
    System.out.println(MODEL.getPricingFunction(option).evaluate(data));
    barrier = new Barrier(KnockType.OUT, BarrierType.UP, 105);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, false, barrier, REBATE);
    System.out.println(MODEL.getPricingFunction(option).evaluate(data));
    barrier = new Barrier(KnockType.IN, BarrierType.DOWN, 95);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, true, barrier, REBATE);
    System.out.println(MODEL.getPricingFunction(option).evaluate(data));
    barrier = new Barrier(KnockType.IN, BarrierType.UP, 105);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, true, barrier, REBATE);
    System.out.println(MODEL.getPricingFunction(option).evaluate(data));
    barrier = new Barrier(KnockType.IN, BarrierType.DOWN, 95);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, false, barrier, REBATE);
    System.out.println(MODEL.getPricingFunction(option).evaluate(data));
    barrier = new Barrier(KnockType.IN, BarrierType.UP, 105);
    option = new EuropeanStandardBarrierOptionDefinition(90, EXPIRY, false, barrier, REBATE);
    System.out.println(MODEL.getPricingFunction(option).evaluate(data));
  }
}
