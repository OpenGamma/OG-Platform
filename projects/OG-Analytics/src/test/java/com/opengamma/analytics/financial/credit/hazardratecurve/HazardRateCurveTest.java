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
 * Tests related to the HazardRateCurve class.
 */
@Test(groups = TestGroup.UNIT)
public class HazardRateCurveTest {

  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  private static final ZonedDateTime BASE_DATE = DateUtils.getUTCDate(2013, 3, 1);
  private static final ZonedDateTime[] DATES_1 = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 3, 1), DateUtils.getUTCDate(2013, 6, 1), DateUtils.getUTCDate(2013, 9, 1),
      DateUtils.getUTCDate(2013, 12, 1), DateUtils.getUTCDate(2014, 3, 1), DateUtils.getUTCDate(2015, 3, 1), DateUtils.getUTCDate(2016, 3, 1), DateUtils.getUTCDate(2018, 3, 1),
      DateUtils.getUTCDate(2023, 3, 1) };
  private static final double[] TIMES_1;
  private static final double[] RATES_1 = new double[] {0.01, 0.02, 0.04, 0.03, 0.06, 0.03, 0.05, 0.03, 0.02 };
  static {
    final int n = DATES_1.length;
    TIMES_1 = new double[n];
    for (int i = 0; i < n; i++) {
      TIMES_1[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, DATES_1[i]);
    }
  }

  private static final ZonedDateTime[] DATES_2 = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 6, 1), DateUtils.getUTCDate(2013, 9, 1),
      DateUtils.getUTCDate(2013, 12, 1), DateUtils.getUTCDate(2014, 3, 1), DateUtils.getUTCDate(2015, 3, 1), DateUtils.getUTCDate(2016, 3, 1), DateUtils.getUTCDate(2018, 3, 1),
      DateUtils.getUTCDate(2023, 3, 1) };
  private static final double[] TIMES_2; // The first time is 0.
  private static final double[] RATES_2 = new double[] {0.01, 0.02, 0.04, 0.03, 0.06, 0.03, 0.05, 0.03, 0.02 };
  static {
    final int n = DATES_2.length;
    TIMES_2 = new double[n + 1];
    for (int i = 0; i < n; i++) {
      TIMES_2[i + 1] = DAY_COUNT.getDayCountFraction(BASE_DATE, DATES_2[i]);
    }
  }
  private static final double OFFSET = 1. / 365;
  private static final double EPS = 1e-15;
  private static final double TOLERANCE_PROBA = 1e-12;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDates1() {
    new HazardRateCurve(null, TIMES_1, RATES_1, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDates2() {
    new HazardRateCurve(DATES_1, TIMES_1, RATES_1, OFFSET).bootstrapHelperHazardRateCurve(null, TIMES_1, RATES_1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTimes1() {
    new HazardRateCurve(DATES_1, null, RATES_1, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTimes2() {
    new HazardRateCurve(DATES_1, TIMES_1, RATES_1, OFFSET).bootstrapHelperHazardRateCurve(DATES_1, null, RATES_1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRates1() {
    new HazardRateCurve(DATES_1, TIMES_1, null, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRates2() {
    new HazardRateCurve(DATES_1, TIMES_1, RATES_1, OFFSET).bootstrapHelperHazardRateCurve(DATES_1, TIMES_1, null);
  }

  //  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthTimes() {
    //new HazardRateCurve(DATES, new double[] {1, 2 }, RATES, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthRates() {
    new HazardRateCurve(DATES_1, TIMES_1, new double[] {0.01, 0.02, }, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyData() {
    new HazardRateCurve(new ZonedDateTime[0], new double[0], new double[0], OFFSET);
  }

  @Test
  public void testObject() {
    final HazardRateCurve curve = new HazardRateCurve(DATES_1, TIMES_1, RATES_1, OFFSET);
    assertDateArrayEquals(DATES_1, curve.getCurveTenors());
    final double[] shiftedTimePoints = new double[DATES_1.length];
    for (int i = 0; i < DATES_1.length; i++) {
      shiftedTimePoints[i] = TIMES_1[i] + OFFSET;
    }
    assertArrayEquals(shiftedTimePoints, curve.getShiftedTimePoints(), 0);
    HazardRateCurve other = new HazardRateCurve(DATES_1, TIMES_1, RATES_1, OFFSET);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    final ZonedDateTime[] dates = new ZonedDateTime[DATES_1.length];
    System.arraycopy(DATES_1, 0, dates, 0, DATES_1.length);
    dates[DATES_1.length - 1] = dates[DATES_1.length - 1].plusYears(1);
    other = new HazardRateCurve(dates, TIMES_1, RATES_1, OFFSET);
    assertFalse(other.equals(curve));
    final double[] times = new double[TIMES_1.length];
    System.arraycopy(TIMES_1, 0, times, 0, TIMES_1.length);
    times[TIMES_1.length - 1] = times[TIMES_1.length - 1] + 1;
    assertFalse(other.equals(curve));
    final double[] rates = new double[RATES_1.length];
    System.arraycopy(RATES_1, 0, rates, 0, RATES_1.length);
    rates[RATES_1.length - 1] = rates[RATES_1.length - 1] - 0.0001;
    assertFalse(other.equals(curve));
  }

  @Test
  public void testObject2() {
    final HazardRateCurve curve = new HazardRateCurve(DATES_2, TIMES_2, RATES_2, OFFSET);
    assertDateArrayEquals(DATES_2, curve.getCurveTenors());
    final double[] shiftedTimePoints = new double[DATES_2.length];
    for (int i = 0; i < DATES_2.length; i++) {
      shiftedTimePoints[i] = TIMES_2[i] + OFFSET;
    }
    assertArrayEquals(shiftedTimePoints, curve.getShiftedTimePoints(), 0);
  }

  @Test
  public void testCurveTypes() {
    HazardRateCurve curve = new HazardRateCurve(DATES_1, TIMES_1, RATES_1, OFFSET);
    assertTrue(curve.getCurve() instanceof InterpolatedDoublesCurve);
    curve = new HazardRateCurve(new ZonedDateTime[] {DATES_1[0] }, new double[] {TIMES_1[0] }, new double[] {RATES_1[0] }, OFFSET);
    assertTrue(curve.getCurve() instanceof ConstantDoublesCurve);
  }

  @Test
  public void testHazardRatesWithZeroOffset() {
    final HazardRateCurve curve = new HazardRateCurve(DATES_1, TIMES_1, RATES_1, 0);
    for (int i = 0; i < TIMES_1.length; i++) {
      assertEquals(RATES_1[i], curve.getHazardRate(TIMES_1[i]), EPS);
      assertEquals(Math.exp(-TIMES_1[i] * RATES_1[i]), curve.getSurvivalProbability(TIMES_1[i]), EPS);
    }
  }

  @Test
  public void testZeroHazardRates() {
    final double[] rates = new double[TIMES_1.length];
    final HazardRateCurve curve = new HazardRateCurve(DATES_1, TIMES_1, rates, 0);
    assertEquals(1, curve.getZeroDiscountFactor(), EPS);
  }

  @Test
  public void testNonZeroOffset() {
    final HazardRateCurve curve = new HazardRateCurve(DATES_1, TIMES_1, RATES_1, OFFSET);
    assertEquals(Math.exp(OFFSET * RATES_1[0]), curve.getZeroDiscountFactor(), EPS);
  }

  @Test
  public void testFlatHazardRateCurve() {
    final double[] rates = new double[TIMES_1.length];
    final double rate = 0.02;
    Arrays.fill(rates, rate);
    HazardRateCurve curve = new HazardRateCurve(DATES_1, TIMES_1, rates, 0);
    for (int i = 0; i < TIMES_1.length; i++) {
      assertEquals(rate, curve.getHazardRate(TIMES_1[i]), EPS);
      assertEquals(Math.exp(-TIMES_1[i] * rate), curve.getSurvivalProbability(TIMES_1[i]), EPS);
    }
    curve = new HazardRateCurve(DATES_1, TIMES_1, rates, OFFSET);
    final double zeroDiscountFactor = Math.exp(OFFSET * rate);
    for (int i = 0; i < TIMES_1.length; i++) {
      assertEquals(rate, curve.getHazardRate(TIMES_1[i]), EPS);
      assertEquals(Math.exp(-(TIMES_1[i] - OFFSET) * rate) / zeroDiscountFactor, curve.getSurvivalProbability(TIMES_1[i]), EPS);
    }
  }

  @Test
  public void survivalProbabilityUnchanged() {
    final HazardRateCurve curveNoOffset = new HazardRateCurve(DATES_1, TIMES_1, RATES_1, 0);
    final double[] timesTest = {0.1, 2.0, 5.15, 12.345, 20.0, 100.0 };
    final int nbTests = timesTest.length;
    final double[] probaExpected = {0.9980019986673331, 0.9417645335842487, 0.8593707921722691, 0.7997110576273061, 0.7407776290146131, 0.33285284468634746 };
    final double[] probaComputed = new double[nbTests];
    for (int looptest = 0; looptest < nbTests; looptest++) {
      probaComputed[looptest] = curveNoOffset.getSurvivalProbability(timesTest[looptest]);
      assertEquals("HazardRateCurve - getSurvivalProbability - harcoded values", probaExpected[looptest], probaComputed[looptest], TOLERANCE_PROBA);
    }
  }

  @Test
  public void survivalProbabilityOffsetUnchanged() {
    final HazardRateCurve curveNoOffset = new HazardRateCurve(DATES_1, TIMES_1, RATES_1, OFFSET);
    final double[] timesTest = {0.1, 2.0, 5.15, 12.345, 20.0, 100.0 };
    final int nbTests = timesTest.length;
    final double[] probaExpected = {0.9980293415624031, 0.9417387321696511, 0.8593707921722691, 0.7997110576273063, 0.7407776290146131, 0.33285284468634746 };
    final double[] probaComputed = new double[nbTests];
    for (int looptest = 0; looptest < nbTests; looptest++) {
      probaComputed[looptest] = curveNoOffset.getSurvivalProbability(timesTest[looptest]);
      assertEquals("HazardRateCurve - getSurvivalProbability - harcoded values", probaExpected[looptest], probaComputed[looptest], TOLERANCE_PROBA);
    }
  }

  private void assertDateArrayEquals(final ZonedDateTime[] expected, final ZonedDateTime[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }

}
