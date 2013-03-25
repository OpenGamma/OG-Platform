/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdayieldcurve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

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
    DateUtils.getUTCDate(2023, 3, 1)};
  private static final double[] TIMES;
  private static final double[] YIELDS = new double[] {0.005, 0.006, 0.008, 0.009, 0.01, 0.012, 0.015, 0.02, 0.03};
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
    new ISDADateCurve(null, BASE_DATE, DATES, YIELDS, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName2() {
    new ISDADateCurve(null, DATES, TIMES, YIELDS, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName3() {
    new ISDADateCurve(null, BASE_DATE, DATES, YIELDS, OFFSET, DAY_COUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBaseDate1() {
    new ISDADateCurve(NAME, null, DATES, YIELDS, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBaseDate2() {
    new ISDADateCurve(NAME, null, DATES, YIELDS, OFFSET, DAY_COUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDates1() {
    new ISDADateCurve(NAME, BASE_DATE, null, YIELDS, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDates2() {
    new ISDADateCurve(NAME, null, TIMES, YIELDS, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDates3() {
    new ISDADateCurve(NAME, BASE_DATE, null, YIELDS, OFFSET, DAY_COUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTimes() {
    new ISDADateCurve(NAME, DATES, null, YIELDS, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYields1() {
    new ISDADateCurve(NAME, BASE_DATE, DATES, null, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYields2() {
    new ISDADateCurve(NAME, DATES, TIMES, null, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYields3() {
    new ISDADateCurve(NAME, BASE_DATE, DATES, null, OFFSET, DAY_COUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthRates1() {
    new ISDADateCurve(NAME, BASE_DATE, DATES, new double[] {1, 2}, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthRates2() {
    new ISDADateCurve(NAME, DATES, TIMES, new double[] {1, 2}, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthRates3() {
    new ISDADateCurve(NAME, BASE_DATE, DATES, new double[] {1, 2}, OFFSET, DAY_COUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthTimes() {
    new ISDADateCurve(NAME, DATES, new double[] {1, 2}, YIELDS, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount() {
    new ISDADateCurve(NAME, BASE_DATE, DATES, YIELDS, OFFSET, null);
  }

  @Test
  public void testObject() {
    final ISDADateCurve curve = new ISDADateCurve(NAME, BASE_DATE, DATES, YIELDS, OFFSET);
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
    ISDADateCurve other = new ISDADateCurve(NAME, BASE_DATE, DATES, YIELDS, OFFSET);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new ISDADateCurve(NAME, DATES, TIMES, equivalentRates, OFFSET);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
    other = new ISDADateCurve(NAME, BASE_DATE, DATES, YIELDS, OFFSET, DAY_COUNT);
    assertEquals(curve, other);
    assertEquals(curve.hashCode(), other.hashCode());
  }

  private void assertDateArrayEquals(final ZonedDateTime[] expected, final ZonedDateTime[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }
}
