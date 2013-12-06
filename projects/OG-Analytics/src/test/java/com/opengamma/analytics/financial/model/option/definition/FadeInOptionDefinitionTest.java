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

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
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
public class FadeInOptionDefinitionTest {
  private static final double SPOT = 50;
  private static final double DIFF = 5;
  private static final double LOWER = 50 - DIFF;
  private static final double UPPER = 50 + DIFF;
  private static final DoubleTimeSeries<?> ALL_WITHIN_RANGE = ImmutableLocalDateDoubleTimeSeries.of(new int[] {20100501, 20100502, 20100503, 20100504, 20100505},
      new double[] {SPOT, SPOT, SPOT, SPOT, SPOT});
  private static final DoubleTimeSeries<?> ONE_WITHIN_RANGE = ImmutableLocalDateDoubleTimeSeries.of(new int[] {20100501, 20100502, 20100503, 20100504, 20100505},
      new double[] {SPOT + 2 * DIFF, SPOT + 3 * DIFF, SPOT, SPOT - 1.5 * DIFF, SPOT - 4 * DIFF});
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.06));
  private static final double B = 0.04;
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.4));
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 5, 6);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE);
  private static final StandardOptionWithSpotTimeSeriesDataBundle ALL_DATA = new StandardOptionWithSpotTimeSeriesDataBundle(DATA, ALL_WITHIN_RANGE);
  private static final StandardOptionWithSpotTimeSeriesDataBundle ONE_DATA = new StandardOptionWithSpotTimeSeriesDataBundle(DATA, ONE_WITHIN_RANGE);
  private static final Expiry EXPIRY = new Expiry(DATE);
  private static final FadeInOptionDefinition CALL = new FadeInOptionDefinition(SPOT, EXPIRY, true, LOWER, UPPER);
  private static final FadeInOptionDefinition PUT = new FadeInOptionDefinition(SPOT, EXPIRY, false, LOWER, UPPER);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeLowerBound() {
    new FadeInOptionDefinition(SPOT, EXPIRY, true, -LOWER, UPPER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeUpperBound() {
    new FadeInOptionDefinition(SPOT, EXPIRY, true, LOWER, -UPPER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUpperSmallerThanHigher() {
    new FadeInOptionDefinition(SPOT, EXPIRY, true, UPPER, LOWER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPayoffWithNullDataBundle() {
    CALL.getPayoffFunction().getPayoff(null, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPayoffWithNullTS() {
    CALL.getPayoffFunction().getPayoff(new StandardOptionWithSpotTimeSeriesDataBundle(DATA, null), null);
  }

  @Test
  public void testExerciseFunction() {
    OptionExerciseFunction<StandardOptionWithSpotTimeSeriesDataBundle> exercise = CALL.getExerciseFunction();
    assertFalse(exercise.shouldExercise(ALL_DATA, null));
    assertFalse(exercise.shouldExercise(ONE_DATA, null));
    exercise = PUT.getExerciseFunction();
    assertFalse(exercise.shouldExercise(ALL_DATA, null));
    assertFalse(exercise.shouldExercise(ONE_DATA, null));
  }

  @Test
  public void testPayoff() {
    final double eps = 1e-15;
    OptionPayoffFunction<StandardOptionWithSpotTimeSeriesDataBundle> payoff = CALL.getPayoffFunction();
    assertEquals(payoff.getPayoff(ALL_DATA.withSpot(SPOT - 1), null), 0, eps);
    assertEquals(payoff.getPayoff(ALL_DATA.withSpot(SPOT + 1), null), 1, eps);
    assertEquals(payoff.getPayoff(ONE_DATA.withSpot(SPOT - 1), null), 0, eps);
    assertEquals(payoff.getPayoff(ONE_DATA.withSpot(SPOT + 1), null), 0.2, eps);
    payoff = PUT.getPayoffFunction();
    assertEquals(payoff.getPayoff(ALL_DATA.withSpot(SPOT - 1), null), 1, eps);
    assertEquals(payoff.getPayoff(ALL_DATA.withSpot(SPOT + 1), null), 0, eps);
    assertEquals(payoff.getPayoff(ONE_DATA.withSpot(SPOT - 1), null), 0.2, eps);
    assertEquals(payoff.getPayoff(ONE_DATA.withSpot(SPOT + 1), null), 0, eps);
  }

  @Test
  public void testEqualsAndHashCode() {
    OptionDefinition call = new FadeInOptionDefinition(SPOT, EXPIRY, true, LOWER, UPPER);
    final OptionDefinition put = new FadeInOptionDefinition(SPOT, EXPIRY, false, LOWER, UPPER);
    assertEquals(call, CALL);
    assertEquals(put, PUT);
    assertEquals(call.hashCode(), CALL.hashCode());
    assertEquals(put.hashCode(), PUT.hashCode());
    assertFalse(call.equals(put));
    call = new FadeInOptionDefinition(SPOT + 1, EXPIRY, true, LOWER, UPPER);
    assertFalse(call.equals(CALL));
    call = new FadeInOptionDefinition(SPOT, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 1)), true, LOWER, UPPER);
    assertFalse(call.equals(CALL));
    call = new FadeInOptionDefinition(SPOT, EXPIRY, true, LOWER + 1, UPPER);
    assertFalse(call.equals(CALL));
    call = new FadeInOptionDefinition(SPOT, EXPIRY, true, LOWER, UPPER + 1);
    assertFalse(call.equals(CALL));
  }
}
