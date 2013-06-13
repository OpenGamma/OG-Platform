/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.hazardratecurve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class HazardRateCurveTest {
  private static final ZonedDateTime BASE_DATE = DateUtils.getUTCDate(2013, 3, 1);
  private static final ZonedDateTime[] DATES = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 3, 1), DateUtils.getUTCDate(2013, 6, 1), DateUtils.getUTCDate(2013, 9, 1),
      DateUtils.getUTCDate(2013, 12, 1), DateUtils.getUTCDate(2014, 3, 1), DateUtils.getUTCDate(2015, 3, 1), DateUtils.getUTCDate(2016, 3, 1), DateUtils.getUTCDate(2018, 3, 1),
      DateUtils.getUTCDate(2023, 3, 1) };
  private static final double[] TIMES;
  private static final double[] RATES = new double[] {0.01, 0.02, 0.04, 0.03, 0.06, 0.03, 0.05, 0.03, 0.02 };
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final double OFFSET = 1. / 365;
  private static final double EPS = 1e-15;

  static {
    final int n = DATES.length;
    TIMES = new double[n];
    for (int i = 0; i < n; i++) {
      TIMES[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, DATES[i]);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDates1() {
    new HazardRateCurve(null, TIMES, RATES, OFFSET);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDates2() {
    new HazardRateCurve(DATES, TIMES, RATES, OFFSET).bootstrapHelperHazardRateCurve(null, TIMES, RATES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTimes1() {
    new HazardRateCurve(DATES, null, RATES, OFFSET);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTimes2() {
    new HazardRateCurve(DATES, TIMES, RATES, OFFSET).bootstrapHelperHazardRateCurve(DATES, null, RATES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRates1() {
    new HazardRateCurve(DATES, TIMES, null, OFFSET);
  }

  @SuppressWarnings("deprecation")
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRates2() {
    new HazardRateCurve(DATES, TIMES, RATES, OFFSET).bootstrapHelperHazardRateCurve(DATES, TIMES, null);
  }

  //@Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthTimes() {
    //new HazardRateCurve(DATES, new double[] {1, 2 }, RATES, OFFSET);
  }

  //@Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthRates() {
    //new HazardRateCurve(DATES, TIMES, new double[] {0.01, 0.02, }, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyData() {
    new HazardRateCurve(new ZonedDateTime[0], new double[0], new double[0], OFFSET);
  }

  @Test
  public void testObject() {
    final HazardRateCurve curve = new HazardRateCurve(DATES, TIMES, RATES, OFFSET);
    assertDateArrayEquals(DATES, curve.getCurveTenors());
    final double[] shiftedTimePoints = new double[DATES.length];
    for (int i = 0; i < DATES.length; i++) {
      shiftedTimePoints[i] = TIMES[i] + OFFSET;
    }
    assertArrayEquals(shiftedTimePoints, curve.getShiftedTimePoints(), 0);
    HazardRateCurve other = new HazardRateCurve(DATES, TIMES, RATES, OFFSET);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    final ZonedDateTime[] dates = new ZonedDateTime[DATES.length];
    System.arraycopy(DATES, 0, dates, 0, DATES.length);
    dates[DATES.length - 1] = dates[DATES.length - 1].plusYears(1);
    other = new HazardRateCurve(dates, TIMES, RATES, OFFSET);
    assertFalse(other.equals(curve));
    final double[] times = new double[TIMES.length];
    System.arraycopy(TIMES, 0, times, 0, TIMES.length);
    times[TIMES.length - 1] = times[TIMES.length - 1] + 1;
    assertFalse(other.equals(curve));
    final double[] rates = new double[RATES.length];
    System.arraycopy(RATES, 0, rates, 0, RATES.length);
    rates[RATES.length - 1] = rates[RATES.length - 1] - 0.0001;
    assertFalse(other.equals(curve));
  }

  @Test
  public void testCurveTypes() {
    HazardRateCurve curve = new HazardRateCurve(DATES, TIMES, RATES, OFFSET);
    assertTrue(curve.getCurve() instanceof InterpolatedDoublesCurve);
    curve = new HazardRateCurve(new ZonedDateTime[] {DATES[0] }, new double[] {TIMES[0] }, new double[] {RATES[0] }, OFFSET);
    assertTrue(curve.getCurve() instanceof ConstantDoublesCurve);
  }

  @Test
  public void testHazardRatesWithZeroOffset() {
    final HazardRateCurve curve = new HazardRateCurve(DATES, TIMES, RATES, 0);
    for (int i = 0; i < TIMES.length; i++) {
      assertEquals(RATES[i], curve.getHazardRate(TIMES[i]), EPS);
      assertEquals(Math.exp(-TIMES[i] * RATES[i]), curve.getSurvivalProbability(TIMES[i]), EPS);
    }
  }

  @Test
  public void testZeroHazardRates() {
    final double[] rates = new double[TIMES.length];
    final HazardRateCurve curve = new HazardRateCurve(DATES, TIMES, rates, 0);
    assertEquals(1, curve.getZeroDiscountFactor(), EPS);
  }

  @Test
  public void testNonZeroOffset() {
    final HazardRateCurve curve = new HazardRateCurve(DATES, TIMES, RATES, OFFSET);
    assertEquals(Math.exp(OFFSET * RATES[0]), curve.getZeroDiscountFactor(), EPS);
  }

  @Test
  public void testFlatHazardRateCurve() {
    final double[] rates = new double[TIMES.length];
    final double rate = 0.02;
    Arrays.fill(rates, rate);
    HazardRateCurve curve = new HazardRateCurve(DATES, TIMES, rates, 0);
    for (int i = 0; i < TIMES.length; i++) {
      assertEquals(rate, curve.getHazardRate(TIMES[i]), EPS);
      assertEquals(Math.exp(-TIMES[i] * rate), curve.getSurvivalProbability(TIMES[i]), EPS);
    }
    curve = new HazardRateCurve(DATES, TIMES, rates, OFFSET);
    final double zeroDiscountFactor = Math.exp(OFFSET * rate);
    for (int i = 0; i < TIMES.length; i++) {
      assertEquals(rate, curve.getHazardRate(TIMES[i]), EPS);
      assertEquals(Math.exp(-(TIMES[i] - OFFSET) * rate) / zeroDiscountFactor, curve.getSurvivalProbability(TIMES[i]), EPS);
    }
  }

  private void assertDateArrayEquals(final ZonedDateTime[] expected, final ZonedDateTime[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }

}
