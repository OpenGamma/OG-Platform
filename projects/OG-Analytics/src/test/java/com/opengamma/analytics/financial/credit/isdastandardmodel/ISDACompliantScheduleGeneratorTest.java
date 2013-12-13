/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ISDACompliantScheduleGeneratorTest {
  private static final DayCount ACT = DayCounts.ACT_365;

  private static final LocalDate START_DATE = LocalDate.of(2013, 2, 13);
  private static final LocalDate END_DATE = LocalDate.of(2015, 6, 30);
  private static final LocalDate[] DISCOUNT_CURVE_DATES = new LocalDate[] {LocalDate.of(2013, 3, 13), LocalDate.of(2013, 4, 13), LocalDate.of(2013, 4, 12), LocalDate.of(2014, 7, 2),
    LocalDate.of(2015, 6, 30) };
  private static final LocalDate[] SPREAD_CURVE_DATES = new LocalDate[] {LocalDate.of(2013, 2, 23), LocalDate.of(2013, 4, 13), LocalDate.of(2014, 2, 17), LocalDate.of(2017, 4, 30),
    LocalDate.of(2014, 7, 2), LocalDate.of(2015, 4, 30) };

  /**
   *
   */
  @Test
  public void getIntegrationNodesAsDatesTest() {

    final LocalDate[] expected = new LocalDate[] {START_DATE, LocalDate.of(2013, 2, 23), LocalDate.of(2013, 3, 13), LocalDate.of(2013, 4, 12), LocalDate.of(2013, 4, 13), LocalDate.of(2014, 2, 17),
      LocalDate.of(2014, 7, 2), LocalDate.of(2015, 4, 30), END_DATE };
    final int n = expected.length;

    LocalDate[] res = ISDACompliantScheduleGenerator.getIntegrationNodesAsDates(START_DATE, END_DATE, DISCOUNT_CURVE_DATES, SPREAD_CURVE_DATES);
    assertEquals("", n, res.length);
    for (int i = 0; i < n; i++) {
      assertTrue(expected[i].equals(res[i]));
    }

    final LocalDate lateStartDate = LocalDate.of(2013, 3, 13);
    final LocalDate[] expectedLateStart = new LocalDate[] {lateStartDate, LocalDate.of(2013, 4, 12), LocalDate.of(2013, 4, 13), LocalDate.of(2014, 2, 17), LocalDate.of(2014, 7, 2),
      LocalDate.of(2015, 4, 30), END_DATE };
    final int nLateStart = expectedLateStart.length;
    res = ISDACompliantScheduleGenerator.getIntegrationNodesAsDates(lateStartDate, END_DATE, DISCOUNT_CURVE_DATES, SPREAD_CURVE_DATES);
    assertEquals(nLateStart, res.length);
    for (int i = 0; i < nLateStart; i++) {
      //      System.out.println(res[i] + "\t" + expectedLateStart[i]);
      assertTrue(expectedLateStart[i].equals(res[i]));
    }

    final LocalDate lateEndDate = LocalDate.of(2018, 8, 30);
    final LocalDate[] discCurveDatesRe = new LocalDate[] {LocalDate.of(2013, 3, 13), LocalDate.of(2013, 4, 14), LocalDate.of(2013, 4, 12), LocalDate.of(2014, 7, 5), LocalDate.of(2015, 6, 30) };
    final LocalDate[] expectedLateEnd = new LocalDate[] {START_DATE, LocalDate.of(2013, 2, 23), LocalDate.of(2013, 3, 13), LocalDate.of(2013, 4, 12), LocalDate.of(2013, 4, 13),
      LocalDate.of(2013, 4, 14), LocalDate.of(2014, 2, 17), LocalDate.of(2014, 7, 2), LocalDate.of(2014, 7, 5), LocalDate.of(2015, 4, 30), LocalDate.of(2015, 6, 30), LocalDate.of(2017, 4, 30),
      lateEndDate };
    final int nLateEnd = expectedLateEnd.length;
    res = ISDACompliantScheduleGenerator.getIntegrationNodesAsDates(START_DATE, lateEndDate, discCurveDatesRe, SPREAD_CURVE_DATES);
    assertEquals(nLateEnd, res.length);
    for (int i = 0; i < nLateEnd; i++) {
      //      System.out.println(res[i] + "\t" + expectedLateStart[i]);
      assertTrue(expectedLateEnd[i].equals(res[i]));
    }
  }

  /**
   *
   */
  @Test
  void nodesAsTimesTest() {
    final LocalDate today = LocalDate.of(2013, 1, 23);
    final LocalDate[] expectedDates = new LocalDate[] {START_DATE, LocalDate.of(2013, 2, 23), LocalDate.of(2013, 3, 13), LocalDate.of(2013, 4, 12), LocalDate.of(2013, 4, 13),
      LocalDate.of(2014, 2, 17), LocalDate.of(2014, 7, 2), LocalDate.of(2015, 4, 30), END_DATE };
    final int n = expectedDates.length;
    final double[] expected = new double[n];
    for (int i = 0; i < n; ++i) {
      expected[i] = ACT.getDayCountFraction(today, expectedDates[i]);
    }

    final double[] res = ISDACompliantScheduleGenerator.getIntegrationNodesAsTimes(today, START_DATE, END_DATE, DISCOUNT_CURVE_DATES, SPREAD_CURVE_DATES);
    assertEquals(n, res.length);
    for (int i = 0; i < n; ++i) {
      assertEquals(expected[i], res[i]);
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nodesAsTimesNullTodayTest() {
    final LocalDate today = null;
    ISDACompliantScheduleGenerator.getIntegrationNodesAsTimes(today, START_DATE, END_DATE, DISCOUNT_CURVE_DATES, SPREAD_CURVE_DATES);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nodesAsTimesNullStartTest() {
    final LocalDate today = LocalDate.of(2013, 1, 23);
    final LocalDate start = null;
    ISDACompliantScheduleGenerator.getIntegrationNodesAsTimes(today, start, END_DATE, DISCOUNT_CURVE_DATES, SPREAD_CURVE_DATES);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nodesAsTimeTodayAfterStartTest() {
    final LocalDate today = LocalDate.of(2013, 3, 23);
    ISDACompliantScheduleGenerator.getIntegrationNodesAsTimes(today, START_DATE, END_DATE, DISCOUNT_CURVE_DATES, SPREAD_CURVE_DATES);
  }

  /**
   *
   */
  @Test
  public void truncateListTest() {
    final LocalDate[] dateList = new LocalDate[] {LocalDate.of(1912, 2, 29), LocalDate.of(1013, 12, 13), LocalDate.of(2000, 2, 2), LocalDate.of(2014, 7, 2), LocalDate.of(2015, 12, 30) };
    final LocalDate startDate = LocalDate.of(1992, 2, 13);
    final LocalDate endDate = LocalDate.of(2015, 6, 30);

    LocalDate[] expected = new LocalDate[] {startDate, LocalDate.of(2000, 2, 2), LocalDate.of(2014, 7, 2), endDate };
    int n = expected.length;
    LocalDate[] res = ISDACompliantScheduleGenerator.truncateList(startDate, endDate, dateList);
    assertEquals(n, res.length);
    for (int i = 0; i < n; ++i) {
      assertTrue(expected[i].isEqual(res[i]));
    }

    final LocalDate[] emptyDateList = new LocalDate[] {};
    expected = new LocalDate[] {startDate, endDate };
    n = expected.length;
    res = ISDACompliantScheduleGenerator.truncateList(startDate, endDate, emptyDateList);
    assertEquals(n, res.length);
    for (int i = 0; i < n; ++i) {
      assertTrue(expected[i].isEqual(res[i]));
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void truncateListNullStartTest() {
    final LocalDate[] dateList = new LocalDate[] {LocalDate.of(1912, 2, 29), LocalDate.of(1013, 12, 13), LocalDate.of(2000, 2, 2), LocalDate.of(2014, 7, 2), LocalDate.of(2015, 12, 30) };
    final LocalDate startDate = null;
    final LocalDate endDate = LocalDate.of(2015, 6, 30);
    ISDACompliantScheduleGenerator.truncateList(startDate, endDate, dateList);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void truncateListNullEndTest() {
    final LocalDate[] dateList = new LocalDate[] {LocalDate.of(1912, 2, 29), LocalDate.of(1013, 12, 13), LocalDate.of(2000, 2, 2), LocalDate.of(2014, 7, 2), LocalDate.of(2015, 12, 30) };
    final LocalDate startDate = LocalDate.of(1992, 2, 13);
    final LocalDate endDate = null;
    ISDACompliantScheduleGenerator.truncateList(startDate, endDate, dateList);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void truncateListNullListTest() {
    final LocalDate[] dateList = null;
    final LocalDate startDate = LocalDate.of(1992, 2, 13);
    final LocalDate endDate = LocalDate.of(2015, 6, 30);
    ISDACompliantScheduleGenerator.truncateList(startDate, endDate, dateList);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void truncateListStartAfterEndTest() {
    final LocalDate[] dateList = new LocalDate[] {LocalDate.of(1912, 2, 29), LocalDate.of(1013, 12, 13), LocalDate.of(2000, 2, 2), LocalDate.of(2014, 7, 2), LocalDate.of(2015, 12, 30) };
    final LocalDate startDate = LocalDate.of(2022, 2, 13);
    final LocalDate endDate = LocalDate.of(2015, 6, 30);
    ISDACompliantScheduleGenerator.truncateList(startDate, endDate, dateList);
  }

}
