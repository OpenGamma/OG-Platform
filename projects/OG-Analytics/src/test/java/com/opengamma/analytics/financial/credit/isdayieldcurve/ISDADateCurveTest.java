/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdayieldcurve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

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
public class ISDADateCurveTest {
  private static final String NAME = "ISDA";
  private static final ZonedDateTime BASE_DATE = DateUtils.getUTCDate(2013, 3, 1);
  private static final ZonedDateTime[] DATES = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 3, 1), DateUtils.getUTCDate(2013, 6, 1), DateUtils.getUTCDate(2013, 9, 1),
      DateUtils.getUTCDate(2013, 12, 1), DateUtils.getUTCDate(2014, 3, 1), DateUtils.getUTCDate(2015, 3, 1), DateUtils.getUTCDate(2016, 3, 1), DateUtils.getUTCDate(2018, 3, 1),
      DateUtils.getUTCDate(2023, 3, 1) };
  private static final double[] TIMES;
  private static final double[] RATES = new double[] {0.005, 0.006, 0.008, 0.009, 0.01, 0.012, 0.015, 0.02, 0.03 };
  private static final double OFFSET = 1. / 365;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  static {
    final int n = DATES.length;
    TIMES = new double[n];
    for (int i = 0; i < n; i++) {
      TIMES[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, DATES[i]);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName1() {
    new ISDADateCurve(null, BASE_DATE, DATES, RATES, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName2() {
    new ISDADateCurve(null, DATES, TIMES, RATES, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName3() {
    new ISDADateCurve(null, BASE_DATE, DATES, RATES, OFFSET, DAY_COUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBaseDate1() {
    new ISDADateCurve(NAME, null, DATES, RATES, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBaseDate2() {
    new ISDADateCurve(NAME, null, DATES, RATES, OFFSET, DAY_COUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDates1() {
    new ISDADateCurve(NAME, BASE_DATE, null, RATES, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDates2() {
    new ISDADateCurve(NAME, null, TIMES, RATES, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDates3() {
    new ISDADateCurve(NAME, BASE_DATE, null, RATES, OFFSET, DAY_COUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTimes() {
    new ISDADateCurve(NAME, DATES, null, RATES, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRates1() {
    new ISDADateCurve(NAME, BASE_DATE, DATES, null, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRates2() {
    new ISDADateCurve(NAME, DATES, TIMES, null, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRates3() {
    new ISDADateCurve(NAME, BASE_DATE, DATES, null, OFFSET, DAY_COUNT);
  }

  //@Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthRates1() {
    //new ISDADateCurve(NAME, BASE_DATE, DATES, new double[] {1, 2 }, OFFSET);
  }

  //@Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthRates2() {
    //new ISDADateCurve(NAME, DATES, TIMES, new double[] {1, 2 }, OFFSET);
  }

  //@Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthRates3() {
    //new ISDADateCurve(NAME, BASE_DATE, DATES, new double[] {1, 2 }, OFFSET, DAY_COUNT);
  }

  //@Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthTimes() {
    //new ISDADateCurve(NAME, DATES, new double[] {1, 2 }, RATES, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    new ISDADateCurve(NAME, BASE_DATE, DATES, RATES, OFFSET, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyData1() {
    new ISDADateCurve(NAME, new ZonedDateTime[0], new double[0], new double[0], OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyData2() {
    new ISDADateCurve(NAME, BASE_DATE, new ZonedDateTime[0], new double[0], OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyData3() {
    new ISDADateCurve(NAME, BASE_DATE, new ZonedDateTime[0], new double[0], OFFSET, DAY_COUNT);
  }

  @Test
  public void testObject() {
    final ISDADateCurve curve = new ISDADateCurve(NAME, BASE_DATE, DATES, RATES, OFFSET);
    assertDateArrayEquals(DATES, curve.getCurveDates());
    assertEquals(NAME, curve.getName());
    assertEquals(DATES.length, curve.getNumberOfCurvePoints());
    assertEquals(OFFSET, curve.getOffset());
    final double[] shiftedTimes = new double[TIMES.length];
    for (int i = 0; i < TIMES.length; i++) {
      shiftedTimes[i] = TIMES[i] + OFFSET;
    }
    assertArrayEquals(shiftedTimes, curve.getTimePoints(), 0);
    final double[] equivalentRates = new double[TIMES.length];
    for (int i = 0; i < TIMES.length; i++) {
      equivalentRates[i] = curve.getInterestRate(TIMES[i] + OFFSET);
    }
    ISDADateCurve other = new ISDADateCurve(NAME, BASE_DATE, DATES, RATES, OFFSET);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new ISDADateCurve(NAME, DATES, TIMES, equivalentRates, OFFSET);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new ISDADateCurve(NAME, BASE_DATE, DATES, RATES, OFFSET, DAY_COUNT);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new ISDADateCurve(NAME + "1", BASE_DATE, DATES, RATES, OFFSET);
    assertFalse(other.equals(curve));
    other = new ISDADateCurve(NAME, BASE_DATE.plusSeconds(1), DATES, RATES, OFFSET);
    final ZonedDateTime[] dates = new ZonedDateTime[DATES.length];
    System.arraycopy(DATES, 0, dates, 0, DATES.length);
    dates[DATES.length - 1] = dates[DATES.length - 1].plusYears(1);
    other = new ISDADateCurve(NAME, dates, TIMES, RATES, OFFSET);
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
  public void testCurve() {
    ISDADateCurve isdaCurve = new ISDADateCurve(NAME, DATES, TIMES, RATES, OFFSET);
    assertTrue(isdaCurve.getCurve() instanceof InterpolatedDoublesCurve);
    isdaCurve = new ISDADateCurve(NAME, new ZonedDateTime[] {DATES[0] }, new double[] {TIMES[0] }, new double[] {RATES[0] }, OFFSET);
    assertTrue(isdaCurve.getCurve() instanceof ConstantDoublesCurve);
  }

  private void assertDateArrayEquals(final ZonedDateTime[] expected, final ZonedDateTime[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }
}
