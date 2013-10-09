/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isda;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.ISDACompliantScheduleGenerator;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapIntegrationSchedule;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 *
 */
public class ISDACompliantScheduleGeneratorTest {
  private static final DayCount ACT = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  private static final LocalDate START_DATE = LocalDate.of(2013, 2, 13);
  private static final LocalDate END_DATE = LocalDate.of(2015, 6, 30);
  private static final LocalDate[] DISCOUNT_CURVE_DATES = new LocalDate[] {LocalDate.of(2013, 3, 13), LocalDate.of(2013, 4, 13), LocalDate.of(2013, 4, 12), LocalDate.of(2014, 7, 2),
      LocalDate.of(2015, 6, 30) };
  private static final LocalDate[] SPREAD_CURVE_DATES = new LocalDate[] {LocalDate.of(2013, 2, 23), LocalDate.of(2013, 4, 13), LocalDate.of(2014, 2, 17), LocalDate.of(2017, 4, 30),
      LocalDate.of(2014, 7, 2),
      LocalDate.of(2015, 4, 30) };

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
  public void toZonedDateTimeMatchTest() {
    final LocalDate[] lDates = new LocalDate[] {LocalDate.of(1912, 2, 29), LocalDate.of(1013, 12, 13), LocalDate.of(2000, 2, 2), LocalDate.of(2014, 7, 2), LocalDate.of(2015, 12, 30) };
    final ZonedDateTime[] expected = new ZonedDateTime[] {ZonedDateTime.of(1912, 2, 29, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(1013, 12, 13, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2000, 2, 2, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2014, 7, 2, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2015, 12, 30, 12, 0, 0, 0, ZoneId.of("Z")) };
    final int nDates = lDates.length;

    final ZonedDateTime[] zDates = ISDACompliantScheduleGenerator.toZoneDateTime(lDates);
    assertEquals(nDates, zDates.length);
    for (int i = 0; i < nDates; ++i) {
      //      System.out.println(zDates[i]);
      assertTrue(expected[i].equals(zDates[i]));
    }

    final ZonedDateTime zDate = ISDACompliantScheduleGenerator.toZoneDateTime(lDates[2]);
    assertTrue(expected[2].equals(zDate));
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void toZoneDateTimeNullTest() {
    final LocalDate date = null;
    ISDACompliantScheduleGenerator.toZoneDateTime(date);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void toZoneDateTimeNullArrayTest() {
    final LocalDate[] date = null;
    ISDACompliantScheduleGenerator.toZoneDateTime(date);
  }

  /**
   *
   */
  @Test
  public void toLocalDateMatchTest() {
    final ZonedDateTime[] zDates = new ZonedDateTime[] {ZonedDateTime.of(1912, 2, 29, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(1013, 12, 13, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2000, 2, 2, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2014, 7, 2, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2015, 12, 30, 12, 0, 0, 0, ZoneId.of("Z")) };
    final LocalDate[] expected = new LocalDate[] {LocalDate.of(1912, 2, 29), LocalDate.of(1013, 12, 13), LocalDate.of(2000, 2, 2), LocalDate.of(2014, 7, 2), LocalDate.of(2015, 12, 30) };
    final int nDates = zDates.length;

    final LocalDate[] lDates = ISDACompliantScheduleGenerator.toLocalDate(zDates);
    assertEquals(nDates, lDates.length);
    for (int i = 0; i < nDates; ++i) {
      //      System.out.println(zDates[i]);
      assertTrue(expected[i].equals(lDates[i]));
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void toLocalDateNullArrayTest() {
    final ZonedDateTime[] date = null;
    ISDACompliantScheduleGenerator.toLocalDate(date);
  }

  /**
   *
   */
  @Test
  void nodesAsTimesTest() {
    final LocalDate today = LocalDate.of(2013, 1, 23);
    final LocalDate[] expectedDates = new LocalDate[] {START_DATE, LocalDate.of(2013, 2, 23), LocalDate.of(2013, 3, 13), LocalDate.of(2013, 4, 12), LocalDate.of(2013, 4, 13),
        LocalDate.of(2014, 2, 17),
        LocalDate.of(2014, 7, 2), LocalDate.of(2015, 4, 30), END_DATE };
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
  void nodesAsTimesDeprecatedTest() {
    final ZonedDateTime today = ZonedDateTime.of(2013, 1, 23, 12, 0, 0, 0, ZoneId.of("Z"));
    final ZonedDateTime startDate = ZonedDateTime.of(2013, 2, 13, 12, 0, 0, 0, ZoneId.of("Z"));
    final ZonedDateTime endDate = ZonedDateTime.of(2015, 6, 30, 12, 0, 0, 0, ZoneId.of("Z"));
    final ZonedDateTime[] dCurveDates = new ZonedDateTime[] {ZonedDateTime.of(2013, 3, 13, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2013, 4, 13, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2013, 4, 12, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2014, 7, 2, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2015, 6, 30, 12, 0, 0, 0, ZoneId.of("Z")) };
    final ZonedDateTime[] sCurveDates = new ZonedDateTime[] {ZonedDateTime.of(2013, 2, 23, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2013, 4, 13, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2014, 2, 17, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2017, 4, 30, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2014, 7, 2, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2015, 4, 30, 12, 0, 0, 0, ZoneId.of("Z")) };

    final ZonedDateTime[] expectedDates = new ZonedDateTime[] {startDate, ZonedDateTime.of(2013, 2, 23, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2013, 3, 13, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2013, 4, 12, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2013, 4, 13, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2014, 2, 17, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2014, 7, 2, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2015, 4, 30, 12, 0, 0, 0, ZoneId.of("Z")), endDate };
    final int n = expectedDates.length;
    final double[] expected = new double[n];
    for (int i = 0; i < n; ++i) {
      expected[i] = ACT.getDayCountFraction(today, expectedDates[i]);
    }

    final double[] res = ISDACompliantScheduleGenerator.getIntegrationNodesAsTimes(today, startDate, endDate, dCurveDates, sCurveDates);
    assertEquals(n, res.length);
    for (int i = 0; i < n; ++i) {
      assertEquals(expected[i], res[i]);
    }
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nodesAsTimesNullDiscountCurveDeprecatedTest() {
    final ZonedDateTime today = ZonedDateTime.of(2013, 1, 23, 12, 0, 0, 0, ZoneId.of("Z"));
    final ZonedDateTime startDate = ZonedDateTime.of(2013, 2, 13, 12, 0, 0, 0, ZoneId.of("Z"));
    final ZonedDateTime endDate = ZonedDateTime.of(2015, 6, 30, 12, 0, 0, 0, ZoneId.of("Z"));
    final ZonedDateTime[] dCurveDates = null;
    final ZonedDateTime[] sCurveDates = new ZonedDateTime[] {ZonedDateTime.of(2013, 2, 23, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2013, 4, 13, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2014, 2, 17, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2017, 4, 30, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2014, 7, 2, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2015, 4, 30, 12, 0, 0, 0, ZoneId.of("Z")) };
    ISDACompliantScheduleGenerator.getIntegrationNodesAsTimes(today, startDate, endDate, dCurveDates, sCurveDates);
  }

  /**
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nodesAsTimesNullSpreadCurveDeprecatedTest() {
    final ZonedDateTime today = ZonedDateTime.of(2013, 1, 23, 12, 0, 0, 0, ZoneId.of("Z"));
    final ZonedDateTime startDate = ZonedDateTime.of(2013, 2, 13, 12, 0, 0, 0, ZoneId.of("Z"));
    final ZonedDateTime endDate = ZonedDateTime.of(2015, 6, 30, 12, 0, 0, 0, ZoneId.of("Z"));
    final ZonedDateTime[] dCurveDates = new ZonedDateTime[] {ZonedDateTime.of(2013, 3, 13, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2013, 4, 13, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2013, 4, 12, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2014, 7, 2, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2015, 6, 30, 12, 0, 0, 0, ZoneId.of("Z")) };
    final ZonedDateTime[] sCurveDates = null;
    ISDACompliantScheduleGenerator.getIntegrationNodesAsTimes(today, startDate, endDate, dCurveDates, sCurveDates);
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

  /**
   * Deprecated methods in {@link GenerateCreditDefaultSwapIntegrationSchedule}
   */
  @Test
  public void regressionTest() {
    final GenerateCreditDefaultSwapIntegrationSchedule calculator = new GenerateCreditDefaultSwapIntegrationSchedule();
    final ZonedDateTime valDate = ZonedDateTime.of(2003, 1, 6, 12, 0, 0, 0, ZoneId.of("Z"));
    final ZonedDateTime baseDate = ZonedDateTime.of(2003, 3, 1, 12, 0, 0, 0, ZoneId.of("Z"));
    final ZonedDateTime[] hazDates = new ZonedDateTime[] {ZonedDateTime.of(2013, 3, 1, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2013, 6, 1, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2013, 9, 1, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2013, 12, 1, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2014, 3, 1, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2015, 3, 1, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2016, 3, 1, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2018, 3, 1, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2023, 3, 1, 12, 0, 0, 0, ZoneId.of("Z")) };
    final double[] hazTimes;
    final double[] hazRates = new double[] {0.01, 0.02, 0.04, 0.03, 0.06, 0.03, 0.05, 0.03, 0.02 };
    final ZonedDateTime[] ycDates = new ZonedDateTime[] {ZonedDateTime.of(2013, 3, 1, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2013, 6, 1, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2013, 9, 1, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2013, 12, 1, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2014, 3, 1, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2015, 3, 1, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2016, 3, 1, 12, 0, 0, 0, ZoneId.of("Z")), ZonedDateTime.of(2018, 3, 1, 12, 0, 0, 0, ZoneId.of("Z")),
        ZonedDateTime.of(2023, 3, 1, 12, 0, 0, 0, ZoneId.of("Z")) };
    final double[] ycRates = new double[] {0.005, 0.006, 0.008, 0.009, 0.01, 0.012, 0.018, 0.02, 0.03 };
    final double offset = 1. / 365;
    final ISDADateCurve yieldCurve;
    final int n = hazDates.length;
    hazTimes = new double[n];
    for (int i = 0; i < n; i++) {
      hazTimes[i] = ACT.getDayCountFraction(baseDate, hazDates[i]);
    }
    yieldCurve = new ISDADateCurve("ISDA", baseDate, ycDates, ycRates, offset);
    final HazardRateCurve hazardRateCurve = new HazardRateCurve(hazDates, hazTimes, hazRates, offset);

    for (int j = 5; j < 21; j += 3) {
      final CreditDefaultSwapDefinition cds1 = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(valDate.plusYears(j));

      final LocalDate[] res1Deprecated = ISDACompliantScheduleGenerator.toLocalDate(calculator.constructCreditDefaultSwapAccruedLegIntegrationSchedule(valDate, cds1, yieldCurve, hazardRateCurve, true));
      final int nRes1Deprecated = res1Deprecated.length;

      final LocalDate startDateLocal = ISDACompliantScheduleGenerator.toLocalDate(new ZonedDateTime[] {cds1.getStartDate() })[0];
      /*
       * Note cds1.getProtectionStart() == ture by default, thus .plusDays(1) is needed
       */
      final LocalDate endDateLocal = ISDACompliantScheduleGenerator.toLocalDate(new ZonedDateTime[] {cds1.getMaturityDate().plusDays(1) })[0];
      final LocalDate baseDateLocal = ISDACompliantScheduleGenerator.toLocalDate(new ZonedDateTime[] {baseDate })[0];
      final LocalDate[] hazDatesLocal = ISDACompliantScheduleGenerator.toLocalDate(hazDates);
      final LocalDate[] ycDatesLocal = ISDACompliantScheduleGenerator.toLocalDate(ycDates);

      final LocalDate[] res1 = ISDACompliantScheduleGenerator.getIntegrationNodesAsDates(startDateLocal, endDateLocal, ycDatesLocal, hazDatesLocal);
      final int nRes1 = res1.length;
      //      for (int i = 0; i < nRes1Deprecated; ++i) {
      //        System.out.println(res1Deprecated[i]);
      //      }
      //      System.out.println("\n");
      //      for (int i = 0; i < nRes1; ++i) {
      //        System.out.println(res1[i]);
      //      }
      //      System.out.println("\n");

      assertEquals(nRes1Deprecated, nRes1);
      for (int i = 0; i < nRes1; ++i) {
        assertTrue(res1[i].equals(res1Deprecated[i]));
      }

      /*
       * Note that valuation date is used only for start date and end date
       * Other date points are computed with the base date contained in yield curve and hazard rate curve
       */
      final double[] res2Deprecated = calculator.constructCreditDefaultSwapContingentLegIntegrationSchedule(baseDate, cds1.getStartDate(), cds1.getMaturityDate().plusDays(1),
          cds1, yieldCurve, hazardRateCurve);
      final int nRes2Deprecated = res2Deprecated.length;

      final int m = ycDatesLocal.length;
      /*
       * Note that offset is turned on in HazardRateCurve and yieldCurve
       */
      final LocalDate[] ycDatesLocalOffset = new LocalDate[m];
      final LocalDate[] hazDatesLocalOffset = new LocalDate[n];
      for (int i = 0; i < m; ++i) {
        ycDatesLocalOffset[i] = ycDatesLocal[i].plusDays(1);
      }
      for (int i = 0; i < n; ++i) {
        hazDatesLocalOffset[i] = hazDatesLocal[i].plusDays(1);
      }
      final double[] res2 = ISDACompliantScheduleGenerator.getIntegrationNodesAsTimes(baseDateLocal, startDateLocal, endDateLocal, ycDatesLocalOffset, hazDatesLocalOffset);
      final int nRes2 = res2.length;

      //    for (int i = 0; i < nRes2Deprecated; ++i) {
      //      System.out.println(res2Deprecated[i]);
      //    }
      //    System.out.println("\n");
      //    for (int i = 0; i < nRes2; ++i) {
      //      System.out.println(res2[i]);
      //    }

      assertEquals(nRes2Deprecated, nRes2);
      for (int i = 0; i < nRes2; ++i) {
        assertEquals(res2[i], res2Deprecated[i], 1.e-14);
      }
    }
  }

}
