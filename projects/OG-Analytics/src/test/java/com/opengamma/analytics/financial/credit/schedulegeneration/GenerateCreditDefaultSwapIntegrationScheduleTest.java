/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.schedulegeneration;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class GenerateCreditDefaultSwapIntegrationScheduleTest {

  private static final GenerateCreditDefaultSwapIntegrationSchedule CALCULATOR = new GenerateCreditDefaultSwapIntegrationSchedule();
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2013, 1, 6);
  private static final ZonedDateTime BASE_DATE = DateUtils.getUTCDate(2013, 3, 1);
  private static final ZonedDateTime[] HR_DATES = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 3, 1), DateUtils.getUTCDate(2013, 6, 1), DateUtils.getUTCDate(2013, 9, 1),
      DateUtils.getUTCDate(2013, 12, 1), DateUtils.getUTCDate(2014, 3, 1), DateUtils.getUTCDate(2015, 3, 1), DateUtils.getUTCDate(2016, 3, 1), DateUtils.getUTCDate(2018, 3, 1),
      DateUtils.getUTCDate(2023, 3, 1) };
  private static final double[] HR_TIMES;
  private static final double[] HR_RATES = new double[] {0.01, 0.02, 0.04, 0.03, 0.06, 0.03, 0.05, 0.03, 0.02 };
  private static final ZonedDateTime[] YC_DATES = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 3, 1), DateUtils.getUTCDate(2013, 6, 1), DateUtils.getUTCDate(2013, 9, 1),
      DateUtils.getUTCDate(2013, 12, 1), DateUtils.getUTCDate(2014, 3, 1), DateUtils.getUTCDate(2015, 3, 1), DateUtils.getUTCDate(2016, 3, 1), DateUtils.getUTCDate(2018, 3, 1),
      DateUtils.getUTCDate(2023, 3, 1) };
  private static final double[] YC_RATES = new double[] {0.005, 0.006, 0.008, 0.009, 0.01, 0.012, 0.018, 0.02, 0.03 };
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final double OFFSET = 1. / 365;
  private static final ISDADateCurve YIELD_CURVE;
  static {
    int n = HR_DATES.length;
    HR_TIMES = new double[n];
    for (int i = 0; i < n; i++) {
      HR_TIMES[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, HR_DATES[i]);
    }
    YIELD_CURVE = new ISDADateCurve("ISDA", BASE_DATE, YC_DATES, YC_RATES, OFFSET);
  }

  @Test
  public void accLegRegrTest() {
    final CreditDefaultSwapDefinition cds1 = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final CreditDefaultSwapDefinition cds2 = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithProtectionStart(false);
    final ZonedDateTime[] hrDates = new ZonedDateTime[HR_DATES.length];
    final double[] hrTimes = new double[HR_TIMES.length];
    final int len = HR_DATES.length;
    for (int j = 1; j < 15; j += 3) {
      for (int i = 0; i < len; i++) {
        hrDates[i] = HR_DATES[i].plusDays(j);
        hrTimes[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, hrDates[i]);
      }
      final HazardRateCurve hazardRateCurve = new HazardRateCurve(hrDates, hrTimes, HR_RATES, OFFSET);

      ZonedDateTime[] res1 = CALCULATOR.constructCreditDefaultSwapAccruedLegIntegrationSchedule(VALUATION_DATE, cds1, YIELD_CURVE, hazardRateCurve, true);
      final ZonedDateTime[] expected = new ZonedDateTime[hrDates.length - 1 + YC_DATES.length - 1 + 2];
      for (int i = 0; i < YC_DATES.length - 1; i++) {
        expected[i * 2 + 1] = YC_DATES[i];
      }
      for (int i = 0; i < hrDates.length - 1; i++) {
        expected[i * 2 + 2] = hrDates[i];
      }
      expected[0] = cds1.getStartDate();
      expected[expected.length - 1] = cds1.getMaturityDate().plusDays(1);
      assertDateArrayEquals(expected, res1);
      ZonedDateTime[] res2 = CALCULATOR.constructCreditDefaultSwapAccruedLegIntegrationSchedule(VALUATION_DATE, cds2, YIELD_CURVE, hazardRateCurve, false);
      expected[expected.length - 1] = cds2.getMaturityDate().plusDays(0);
      assertDateArrayEquals(expected, res2);
      //    DEPRECATED_CALCULATOR.constructCreditDefaultSwapAccruedLegIntegrationSchedule(VALUATION_DATE, cds2, YIELD_CURVE, hazardRateCurve, true);

      //    final int nRes = res1.length;
      //    for (int i = 0; i < nRes; ++i) {
      //      System.out.println(res1[i]);
      //    }
      //    System.out.println("\n");
      //    final int nEx = expected.length;
      //    for (int i = 0; i < nEx; ++i) {
      //      System.out.println(expected[i]);
      //    }
      //    System.out.println("\n");
      //    final int nRes2 = res2.length;
      //    for (int i = 0; i < nRes2; ++i) {
      //      System.out.println(res2[i]);
      //    }
    }
  }

  @Test
  public void testAccLegWithFixedCurve() {
    final HazardRateCurve hrCurve = new HazardRateCurve(HR_DATES, HR_TIMES, HR_RATES, OFFSET);
    for (int j = 1; j < 15; j += 3) {
      for (int k = 1; k < 25; k += 4) {
        CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithProtectionStart(true)
            .withMaturityDate(YC_DATES[YC_DATES.length - 1].minusMonths(j))
            .withEffectiveDate(VALUATION_DATE.minusMonths(j).plusDays(k))
            .withStartDate(VALUATION_DATE.minusMonths(j));
        ZonedDateTime[] expected = new ZonedDateTime[YC_DATES.length + 1];
        for (int i = 0; i < YC_DATES.length; i++) {
          expected[i + 1] = YC_DATES[i];
        }
        expected[0] = cds.getStartDate();
        expected[YC_DATES.length] = cds.getMaturityDate().plusDays(1);
        assertDateArrayEquals(expected, CALCULATOR.constructCreditDefaultSwapAccruedLegIntegrationSchedule(VALUATION_DATE, cds, YIELD_CURVE, hrCurve, false));
        cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithProtectionStart(false)
            .withMaturityDate(YC_DATES[YC_DATES.length - 1].minusMonths(j))
            .withEffectiveDate(VALUATION_DATE.minusMonths(j).plusDays(k))
            .withStartDate(VALUATION_DATE.minusMonths(j));
        expected = new ZonedDateTime[YC_DATES.length + 1];
        for (int i = 0; i < YC_DATES.length; i++) {
          expected[i + 1] = YC_DATES[i];
        }
        expected[0] = cds.getStartDate();
        expected[YC_DATES.length] = cds.getMaturityDate();
        assertDateArrayEquals(expected, CALCULATOR.constructCreditDefaultSwapAccruedLegIntegrationSchedule(VALUATION_DATE, cds, YIELD_CURVE, hrCurve, true));
      }
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void accLegNullCdsTest() {
    final CreditDefaultSwapDefinition cds1 = null;
    final ZonedDateTime[] hrDates = new ZonedDateTime[HR_DATES.length];
    final double[] hrTimes = new double[HR_TIMES.length];
    final int len = HR_DATES.length;
    for (int i = 0; i < len; i++) {
      hrDates[i] = HR_DATES[i].plusDays(12);
      hrTimes[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, hrDates[i]);
    }
    final HazardRateCurve hazardRateCurve = new HazardRateCurve(hrDates, hrTimes, HR_RATES, OFFSET);

    CALCULATOR.constructCreditDefaultSwapAccruedLegIntegrationSchedule(VALUATION_DATE, cds1, YIELD_CURVE, hazardRateCurve, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void accLegNullYieldTest() {
    final CreditDefaultSwapDefinition cds1 = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final ZonedDateTime[] hrDates = new ZonedDateTime[HR_DATES.length];
    final double[] hrTimes = new double[HR_TIMES.length];
    final int len = HR_DATES.length;
    for (int i = 0; i < len; i++) {
      hrDates[i] = HR_DATES[i].plusDays(12);
      hrTimes[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, hrDates[i]);
    }
    final HazardRateCurve hazardRateCurve = new HazardRateCurve(hrDates, hrTimes, HR_RATES, OFFSET);
    final ISDADateCurve yc = null;

    CALCULATOR.constructCreditDefaultSwapAccruedLegIntegrationSchedule(VALUATION_DATE, cds1, yc, hazardRateCurve, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void accLegNullHazardTest() {
    final CreditDefaultSwapDefinition cds1 = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final HazardRateCurve hazardRateCurve = null;

    CALCULATOR.constructCreditDefaultSwapAccruedLegIntegrationSchedule(VALUATION_DATE, cds1, YIELD_CURVE, hazardRateCurve, true);
  }

  //  @Test  (expectedExceptions = IllegalArgumentException.class)
  //  public void accEndBeforeValuateTest() {
  //    final CreditDefaultSwapDefinition cds1 = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
  //    final ZonedDateTime[] hrDates = new ZonedDateTime[HR_DATES.length];
  //    final double[] hrTimes = new double[HR_TIMES.length];
  //    final int len = HR_DATES.length;
  //    for (int i = 0; i < len; i++) {
  //      hrDates[i] = HR_DATES[i].plusDays(12);
  //      hrTimes[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, hrDates[i]);
  //    }
  //    final HazardRateCurve hazardRateCurve = new HazardRateCurve(hrDates, hrTimes, HR_RATES, OFFSET);
  //    final ZonedDateTime vDate = DateUtils.getUTCDate(2033, 1, 6);
  //
  //    ZonedDateTime[] res1 = DEPRECATED_CALCULATOR.constructCreditDefaultSwapAccruedLegIntegrationSchedule(vDate, cds1, YIELD_CURVE, hazardRateCurve, true);
  //    final int nRes = res1.length;
  //    for (int i = 0; i < nRes; ++i) {
  //      System.out.println(res1[i]);
  //    }
  //  }

  @Test
  public void cngLegRegrTest() {
    CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final ZonedDateTime[] hrDates = new ZonedDateTime[HR_DATES.length];
    final double[] hrTimes = new double[HR_TIMES.length];
    final int len = HR_DATES.length;
    for (int i = 0; i < len; i++) {
      hrDates[i] = HR_DATES[i].plusDays(12);
      hrTimes[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, hrDates[i]);
    }
    final HazardRateCurve hazardRateCurve = new HazardRateCurve(hrDates, hrTimes, HR_RATES, OFFSET);
    ZonedDateTime startDate = getStartDate(cds);
    final ZonedDateTime endDate = cds.getMaturityDate();

    final int lenExp = hrDates.length - 1 + YC_DATES.length - 1 + 2;
    final DayCount act365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
    final double[] ans = new double[lenExp];
    for (int i = 0; i < YC_DATES.length - 1; i++) {
      ans[i * 2 + 1] = YIELD_CURVE.getTimePoints()[i];
    }
    for (int i = 0; i < hrDates.length - 1; i++) {
      ans[i * 2 + 2] = hazardRateCurve.getShiftedTimePoints()[i];
    }
    ans[0] = TimeCalculator.getTimeBetween(VALUATION_DATE, startDate, act365);
    ans[lenExp - 1] = TimeCalculator.getTimeBetween(VALUATION_DATE, endDate, act365);
    double[] res = CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cds, YIELD_CURVE, hazardRateCurve);
    assertEquals(lenExp, res.length);
    for (int i = 0; i < lenExp; ++i) {
      assertEquals(ans[i], res[i]);
    }
    //    System.out.println(new DoubleMatrix1D(ans));
    //    System.out.println(new DoubleMatrix1D(res));
    cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    startDate = VALUATION_DATE;
    ans[0] = TimeCalculator.getTimeBetween(VALUATION_DATE, startDate, act365);
    res = CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cds, YIELD_CURVE, hazardRateCurve);
    assertEquals(lenExp, res.length);
    for (int i = 0; i < lenExp; ++i) {
      assertEquals(ans[i], res[i]);
    }
  }

  @Test
  public void testCngLegWithFixedCurve() {
    final HazardRateCurve hrCurve = new HazardRateCurve(HR_DATES, HR_TIMES, HR_RATES, OFFSET);
    for (int j = 1; j < 25; j += 3) {
      CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithProtectionStart(true)
          .withMaturityDate(YC_DATES[YC_DATES.length - 1].minusMonths(1))
          .withEffectiveDate(VALUATION_DATE.minusMonths(1).plusDays(j))
          .withStartDate(VALUATION_DATE.minusMonths(1));
      final ZonedDateTime[] hrDates = new ZonedDateTime[HR_DATES.length];
      final double[] hrTimes = new double[HR_TIMES.length];
      final int len = HR_DATES.length;
      for (int i = 0; i < len; i++) {
        hrDates[i] = HR_DATES[i].plusDays(12);
        hrTimes[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, hrDates[i]);
      }
      ZonedDateTime startDate = getStartDate(cds);
      final ZonedDateTime endDate = cds.getMaturityDate();
      final int lenExp = YC_DATES.length + 2;

      final DayCount act365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
      final double[] ans = new double[lenExp];
      for (int i = 0; i < lenExp - 2; i++) {
        ans[i + 1] = YIELD_CURVE.getTimePoints()[i];
      }
      ans[0] = TimeCalculator.getTimeBetween(VALUATION_DATE, startDate, act365);
      ans[lenExp - 1] = TimeCalculator.getTimeBetween(VALUATION_DATE, endDate, act365);
      double[] res = CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cds, YIELD_CURVE, hrCurve);
      assertEquals(lenExp, res.length);
      for (int i = 0; i < lenExp; ++i) {
        assertEquals(ans[i], res[i]);
      }
      cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithProtectionStart(false)
          .withMaturityDate(YC_DATES[YC_DATES.length - 1].minusMonths(1))
          .withEffectiveDate(VALUATION_DATE.minusMonths(1).plusDays(j))
          .withStartDate(VALUATION_DATE.minusMonths(1));

      res = CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cds, YIELD_CURVE, hrCurve);
      assertEquals(lenExp, res.length);
      for (int i = 0; i < lenExp; ++i) {
        assertEquals(ans[i], res[i]);
      }
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void cngLegNullCdsTest() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final ZonedDateTime[] hrDates = new ZonedDateTime[HR_DATES.length];
    final double[] hrTimes = new double[HR_TIMES.length];
    final int len = HR_DATES.length;
    for (int i = 0; i < len; i++) {
      hrDates[i] = HR_DATES[i].plusDays(12);
      hrTimes[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, hrDates[i]);
    }
    final HazardRateCurve hazardRateCurve = new HazardRateCurve(hrDates, hrTimes, HR_RATES, OFFSET);
    final ZonedDateTime startDate = getStartDate(cds);
    final ZonedDateTime endDate = cds.getMaturityDate();
    final CreditDefaultSwapDefinition cdsNull = null;

    CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cdsNull, YIELD_CURVE, hazardRateCurve);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void cngLegNullYieldTest() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final ZonedDateTime[] hrDates = new ZonedDateTime[HR_DATES.length];
    final double[] hrTimes = new double[HR_TIMES.length];
    final int len = HR_DATES.length;
    for (int i = 0; i < len; i++) {
      hrDates[i] = HR_DATES[i].plusDays(12);
      hrTimes[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, hrDates[i]);
    }
    final HazardRateCurve hazardRateCurve = new HazardRateCurve(hrDates, hrTimes, HR_RATES, OFFSET);
    final ZonedDateTime startDate = getStartDate(cds);
    final ZonedDateTime endDate = cds.getMaturityDate();
    final ISDADateCurve yc = null;

    CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cds, yc, hazardRateCurve);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void cngLegNullHazardTest() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final HazardRateCurve hazardRateCurve = null;
    final ZonedDateTime startDate = getStartDate(cds);
    final ZonedDateTime endDate = cds.getMaturityDate();

    CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cds, YIELD_CURVE, hazardRateCurve);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void cngEndBeforeStartTest() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final ZonedDateTime[] hrDates = new ZonedDateTime[HR_DATES.length];
    final double[] hrTimes = new double[HR_TIMES.length];
    final int len = HR_DATES.length;
    for (int i = 0; i < len; i++) {
      hrDates[i] = HR_DATES[i].plusDays(12);
      hrTimes[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, hrDates[i]);
    }
    final HazardRateCurve hazardRateCurve = new HazardRateCurve(hrDates, hrTimes, HR_RATES, OFFSET);
    final ZonedDateTime startDate = getStartDate(cds);
    final ZonedDateTime endDate = cds.getMaturityDate();

    CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, endDate, startDate, cds, YIELD_CURVE, hazardRateCurve);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void endBeforeValuateTest() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final ZonedDateTime[] hrDates = new ZonedDateTime[HR_DATES.length];
    final double[] hrTimes = new double[HR_TIMES.length];
    final int len = HR_DATES.length;
    for (int i = 0; i < len; i++) {
      hrDates[i] = HR_DATES[i].plusDays(12);
      hrTimes[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, hrDates[i]);
    }
    final HazardRateCurve hazardRateCurve = new HazardRateCurve(hrDates, hrTimes, HR_RATES, OFFSET);
    final ZonedDateTime startDate = getStartDate(cds);
    final ZonedDateTime endDate = cds.getMaturityDate();
    final ZonedDateTime ev = DateUtils.getUTCDate(2043, 1, 6);

    CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(ev, endDate, startDate, cds, YIELD_CURVE, hazardRateCurve);
    //    System.out.println(new DoubleMatrix1D(res));
  }

  private ZonedDateTime getStartDate(final CreditDefaultSwapDefinition cds) {
    ZonedDateTime startDate;
    int offset = 0;
    if (cds.getProtectionStart()) {
      offset = 1;
    }
    ZonedDateTime clStartDate = VALUATION_DATE;
    if (cds.getProtectionStart()) {
      clStartDate = VALUATION_DATE.minusDays(1);
    }
    final ZonedDateTime stepinDate = cds.getEffectiveDate();
    if (clStartDate.isAfter(stepinDate.minusDays(offset))) {
      startDate = clStartDate;
    } else {
      startDate = stepinDate.minusDays(offset);
    }
    if (startDate.isAfter(VALUATION_DATE.minusDays(1))) {
      //startDate = startDate;
    } else {
      startDate = VALUATION_DATE.minusDays(1);
    }
    return startDate;
  }

  private void assertDateArrayEquals(final ZonedDateTime[] expected, final ZonedDateTime[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }
}
