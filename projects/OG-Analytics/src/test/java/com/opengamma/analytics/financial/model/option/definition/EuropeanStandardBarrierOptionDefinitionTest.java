/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.KnockType;
import com.opengamma.analytics.financial.model.option.definition.Barrier.ObservationType;
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
public class EuropeanStandardBarrierOptionDefinitionTest {
  private static final double STRIKE = 100;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 1));
  private static final Barrier BARRIER = new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, 90);
  private static final double REBATE = 2;
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.05)), 0.03, new VolatilitySurface(ConstantDoublesSurface.from(0.2)),
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
    other = new EuropeanStandardBarrierOptionDefinition(STRIKE, new Expiry(DateUtils.getDateOffsetWithYearFraction(EXPIRY.getExpiry(), 2)), true, BARRIER, REBATE);
    assertFalse(other.equals(option));
    other = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, BARRIER, REBATE);
    assertFalse(other.equals(option));
    other = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, 40), REBATE);
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
    EuropeanStandardBarrierOptionDefinition call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, STRIKE
        - barrierDelta));
    EuropeanStandardBarrierOptionDefinition put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, STRIKE
        - barrierDelta));
    StandardOptionDataBundle data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    data = DATA.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), delta, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, STRIKE + barrierDelta));
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, STRIKE + barrierDelta));
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), delta, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), delta, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, STRIKE - barrierDelta));
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, STRIKE - barrierDelta));
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), delta, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, STRIKE + barrierDelta));
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, STRIKE + barrierDelta));
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), delta, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, STRIKE - barrierDelta));
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, STRIKE - barrierDelta));
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), delta, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    data = DATA.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, STRIKE + barrierDelta));
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, STRIKE + barrierDelta));
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.OUT, BarrierType.UP, ObservationType.CONTINUOUS, STRIKE - barrierDelta));
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.OUT, BarrierType.UP, ObservationType.CONTINUOUS, STRIKE - barrierDelta));
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), delta, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.OUT, BarrierType.UP, ObservationType.CONTINUOUS, STRIKE + barrierDelta));
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.OUT, BarrierType.UP, ObservationType.CONTINUOUS, STRIKE + barrierDelta));
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
    EuropeanStandardBarrierOptionDefinition call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, STRIKE
        - barrierDelta), REBATE);
    EuropeanStandardBarrierOptionDefinition put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, STRIKE
        - barrierDelta), REBATE);
    StandardOptionDataBundle data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    data = DATA.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), delta, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, STRIKE + barrierDelta), REBATE);
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.IN, BarrierType.DOWN, ObservationType.CONTINUOUS, STRIKE + barrierDelta), REBATE);
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), delta, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), delta, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, STRIKE - barrierDelta), REBATE);
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, STRIKE - barrierDelta), REBATE);
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), delta, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, STRIKE + barrierDelta), REBATE);
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.IN, BarrierType.UP, ObservationType.CONTINUOUS, STRIKE + barrierDelta), REBATE);
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), delta, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, STRIKE - barrierDelta), REBATE);
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, STRIKE - barrierDelta), REBATE);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), delta, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, 0);
    data = DATA.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, STRIKE + barrierDelta), REBATE);
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.OUT, BarrierType.DOWN, ObservationType.CONTINUOUS, STRIKE + barrierDelta), REBATE);
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.OUT, BarrierType.UP, ObservationType.CONTINUOUS, STRIKE - barrierDelta), REBATE);
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.OUT, BarrierType.UP, ObservationType.CONTINUOUS, STRIKE - barrierDelta), REBATE);
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), delta, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    call = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, true, new Barrier(KnockType.OUT, BarrierType.UP, ObservationType.CONTINUOUS, STRIKE + barrierDelta), REBATE);
    put = new EuropeanStandardBarrierOptionDefinition(STRIKE, EXPIRY, false, new Barrier(KnockType.OUT, BarrierType.UP, ObservationType.CONTINUOUS, STRIKE + barrierDelta), REBATE);
    data = data.withSpot(STRIKE - delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), delta, 0);
    data = DATA.withSpot(STRIKE + delta);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), REBATE, 0);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), REBATE, 0);
  }
}
