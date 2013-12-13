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
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FixedStrikeLookbackOptionDefinitionTest {
  private static final double STRIKE = 100;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2011, 5, 1);
  private static final Expiry EXPIRY = new Expiry(DATE);
  private static final FixedStrikeLookbackOptionDefinition CALL = new FixedStrikeLookbackOptionDefinition(STRIKE, EXPIRY, true);
  private static final FixedStrikeLookbackOptionDefinition PUT = new FixedStrikeLookbackOptionDefinition(STRIKE, EXPIRY, false);
  private static final double SPOT = 100;
  private static final double DIFF = 10;
  private static final DoubleTimeSeries<?> HIGH_TS = ImmutableLocalDateDoubleTimeSeries.of(new int[] {20100501, 20101101, 20110501}, new double[] {SPOT, SPOT + DIFF,
      SPOT});
  private static final DoubleTimeSeries<?> LOW_TS = ImmutableLocalDateDoubleTimeSeries.of(new int[] {20100501, 20101101, 20110501}, new double[] {SPOT, SPOT - DIFF,
      SPOT});
  private static final StandardOptionWithSpotTimeSeriesDataBundle HIGH_DATA = new StandardOptionWithSpotTimeSeriesDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.1)), 0.05,
      new VolatilitySurface(ConstantDoublesSurface.from(0.2)), SPOT, DateUtils.getUTCDate(2010, 6, 1), HIGH_TS);
  private static final StandardOptionWithSpotTimeSeriesDataBundle LOW_DATA = new StandardOptionWithSpotTimeSeriesDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.1)), 0.05,
      new VolatilitySurface(ConstantDoublesSurface.from(0.2)), SPOT, DateUtils.getUTCDate(2010, 6, 1), LOW_TS);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataBundle() {
    CALL.getPayoffFunction().getPayoff(null, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS() {
    CALL.getPayoffFunction().getPayoff(HIGH_DATA.withSpotTimeSeries(null), null);
  }

  @Test
  public void testExercise() {
    assertFalse(CALL.getExerciseFunction().shouldExercise(HIGH_DATA, null));
    assertFalse(CALL.getExerciseFunction().shouldExercise(LOW_DATA, null));
    assertFalse(PUT.getExerciseFunction().shouldExercise(HIGH_DATA, null));
    assertFalse(PUT.getExerciseFunction().shouldExercise(LOW_DATA, null));
  }

  @Test
  public void testPayoff() {
    final double eps = 1e-15;
    OptionPayoffFunction<StandardOptionWithSpotTimeSeriesDataBundle> payoff = CALL.getPayoffFunction();
    assertEquals(payoff.getPayoff(LOW_DATA, 0.), 0, eps);
    assertEquals(payoff.getPayoff(HIGH_DATA, 0.), SPOT + DIFF - STRIKE, eps);
    payoff = PUT.getPayoffFunction();
    assertEquals(payoff.getPayoff(LOW_DATA, 0.), STRIKE + DIFF - SPOT, eps);
    assertEquals(payoff.getPayoff(HIGH_DATA, 0.), 0, eps);
  }

  @Test
  public void testEqualsAndHashCode() {
    final OptionDefinition call1 = new FixedStrikeLookbackOptionDefinition(STRIKE, EXPIRY, true);
    final OptionDefinition put1 = new FixedStrikeLookbackOptionDefinition(STRIKE, EXPIRY, false);
    final OptionDefinition call2 = new FixedStrikeLookbackOptionDefinition(STRIKE, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 3)), true);
    final OptionDefinition put2 = new FixedStrikeLookbackOptionDefinition(STRIKE, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 3)), false);
    assertFalse(CALL.equals(PUT));
    assertEquals(call1, CALL);
    assertEquals(put1, PUT);
    assertEquals(call1.hashCode(), CALL.hashCode());
    assertEquals(put1.hashCode(), PUT.hashCode());
    assertFalse(call2.equals(CALL));
    assertFalse(put2.equals(PUT));
  }
}
