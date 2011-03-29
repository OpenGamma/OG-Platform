/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class EuropeanStandardBarrierOptionDefinitionTest {
  private static final double STRIKE = 100;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 1));
  private static final Barrier BARRIER = new Barrier(KnockType.IN, BarrierType.DOWN, 90);
  private static final double REBATE = 2;
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(new YieldCurve(ConstantDoublesCurve.from(0.05)), 0.03, new VolatilitySurface(ConstantDoublesSurface.from(0.2)),
      100, DATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBarrier() {
    new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeRebate() {
    new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, BARRIER, -3);
  }

  @Test
  public void testEqualsAndHashCode() {
    final EuropeanStandardBarrierOptionDefinition option = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, BARRIER, REBATE);
    EuropeanStandardBarrierOptionDefinition other = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, BARRIER, REBATE);
    assertEquals(option, other);
    assertEquals(option.hashCode(), other.hashCode());
    other = new EuropeanStandardBarrierOptionDefinition(STRIKE + 1, EXPIRY, true, BARRIER, REBATE);
    assertFalse(other.equals(option));
    other = new EuropeanStandardBarrierOptionDefinition(STRIKE, new Expiry(DateUtil.getDateOffsetWithYearFraction(EXPIRY.getExpiry(), 2)), true, BARRIER, REBATE);
    assertFalse(other.equals(option));
    other = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, BARRIER, REBATE);
    assertFalse(other.equals(option));
    other = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.IN, BarrierType.UP, 40), REBATE);
    assertFalse(other.equals(option));
    other = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, BARRIER, REBATE + 1);
    assertFalse(other.equals(option));
  }

  @Test
  public void testGetters() {
    final EuropeanStandardBarrierOptionDefinition option = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, BARRIER, REBATE);
    assertEquals(option.getBarrier(), BARRIER);
    assertEquals(option.getRebate(), REBATE, 0);
  }

  @Test
  public void testConstructors() {
    assertEquals(new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, BARRIER), new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, BARRIER, 0));
  }

  @Test
  public void testExercise() {
    EuropeanStandardBarrierOptionDefinition option = new EuropeanStandardBarrierOptionDefinition(STRIKE - 60, EXPIRY, true, BARRIER, REBATE);
    OptionExerciseFunction<StandardOptionDataBundle> f = option.getExerciseFunction();
    assertFalse(f.shouldExercise(DATA, null));
    option = new EuropeanStandardBarrierOptionDefinition(STRIKE - 60, EXPIRY, false, BARRIER, REBATE);
    f = option.getExerciseFunction();
    assertFalse(f.shouldExercise(DATA, null));
    option = new EuropeanStandardBarrierOptionDefinition(STRIKE + 60, EXPIRY, true, BARRIER, REBATE);
    f = option.getExerciseFunction();
    assertFalse(f.shouldExercise(DATA, null));
    option = new EuropeanStandardBarrierOptionDefinition(STRIKE + 60, EXPIRY, false, BARRIER, REBATE);
    f = option.getExerciseFunction();
    assertFalse(f.shouldExercise(DATA, null));
  }

  @Test
  public void testPayoffNoRebate() {
    final double barrierDelta = 10;
    final double delta = 20;
    EuropeanStandardBarrierOptionDefinition call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.IN, BarrierType.DOWN, STRIKE - barrierDelta));
    EuropeanStandardBarrierOptionDefinition put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.IN, BarrierType.DOWN, STRIKE - barrierDelta));
    StandardOptionDataBundle data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    data = DATA.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), delta, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.IN, BarrierType.DOWN, STRIKE + barrierDelta));
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.IN, BarrierType.DOWN, STRIKE + barrierDelta));
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), delta, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), delta, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.IN, BarrierType.UP, STRIKE - barrierDelta));
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.IN, BarrierType.UP, STRIKE - barrierDelta));
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), delta, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.IN, BarrierType.UP, STRIKE + barrierDelta));
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.IN, BarrierType.UP, STRIKE + barrierDelta));
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), delta, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.OUT, BarrierType.DOWN, STRIKE - barrierDelta));
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.OUT, BarrierType.DOWN, STRIKE - barrierDelta));
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), delta, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    data = DATA.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.OUT, BarrierType.DOWN, STRIKE + barrierDelta));
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.OUT, BarrierType.DOWN, STRIKE + barrierDelta));
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.OUT, BarrierType.UP, STRIKE - barrierDelta));
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.OUT, BarrierType.UP, STRIKE - barrierDelta));
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), delta, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.OUT, BarrierType.UP, STRIKE + barrierDelta));
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.OUT, BarrierType.UP, STRIKE + barrierDelta));
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), delta, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
  }

  @Test
  public void testPayoffWithRebate() {
    final double barrierDelta = 10;
    final double delta = 20;
    EuropeanStandardBarrierOptionDefinition call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.IN, BarrierType.DOWN, STRIKE - barrierDelta), REBATE);
    EuropeanStandardBarrierOptionDefinition put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.IN, BarrierType.DOWN, STRIKE - barrierDelta), REBATE);
    StandardOptionDataBundle data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    data = DATA.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), delta, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.IN, BarrierType.DOWN, STRIKE + barrierDelta), REBATE);
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.IN, BarrierType.DOWN, STRIKE + barrierDelta), REBATE);
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), delta, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), delta, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.IN, BarrierType.UP, STRIKE - barrierDelta), REBATE);
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.IN, BarrierType.UP, STRIKE - barrierDelta), REBATE);
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), delta, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.IN, BarrierType.UP, STRIKE + barrierDelta), REBATE);
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.IN, BarrierType.UP, STRIKE + barrierDelta), REBATE);
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), delta, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.OUT, BarrierType.DOWN, STRIKE - barrierDelta), REBATE);
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.OUT, BarrierType.DOWN, STRIKE - barrierDelta), REBATE);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), delta, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    data = DATA.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.OUT, BarrierType.DOWN, STRIKE + barrierDelta), REBATE);
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.OUT, BarrierType.DOWN, STRIKE + barrierDelta), REBATE);
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.OUT, BarrierType.UP, STRIKE - barrierDelta), REBATE);
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.OUT, BarrierType.UP, STRIKE - barrierDelta), REBATE);
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), delta, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.OUT, BarrierType.UP, STRIKE + barrierDelta), REBATE);
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.OUT, BarrierType.UP, STRIKE + barrierDelta), REBATE);
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), delta, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), REBATE, 0);
  }
}
