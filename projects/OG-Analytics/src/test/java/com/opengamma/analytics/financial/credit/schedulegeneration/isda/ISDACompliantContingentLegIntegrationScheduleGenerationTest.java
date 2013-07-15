/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.schedulegeneration.isda;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndHazardRateCurveProvider;
import com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapIntegrationSchedule;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class ISDACompliantContingentLegIntegrationScheduleGenerationTest {
  private static final GenerateCreditDefaultSwapIntegrationSchedule DEPRECATED_CALCULATOR = new GenerateCreditDefaultSwapIntegrationSchedule();
  private static final ISDAContingentLegIntegrationScheduleGenerator CALCULATOR = new ISDAContingentLegIntegrationScheduleGenerator();
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
  private static final HazardRateCurve HAZARD_RATE_CURVE;
  private static final double[] YC_TIMES;
  private static final double[] YC_RATES = new double[] {0.005, 0.006, 0.008, 0.009, 0.01, 0.012, 0.015, 0.02, 0.03 };
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final double OFFSET = 1. / 365;
  private static final ISDADateCurve YIELD_CURVE;
  private static final ISDAYieldCurveAndHazardRateCurveProvider CURVES;
  private static final double EPS = 1e-15;

  static {
    int n = HR_DATES.length;
    HR_TIMES = new double[n];
    for (int i = 0; i < n; i++) {
      HR_TIMES[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, HR_DATES[i]);
    }
    HAZARD_RATE_CURVE = new HazardRateCurve(HR_DATES, HR_TIMES, HR_RATES, OFFSET);
    n = YC_DATES.length;
    YC_TIMES = new double[n];
    for (int i = 0; i < n; i++) {
      YC_TIMES[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, YC_DATES[i]);
    }
    YIELD_CURVE = new ISDADateCurve("ISDA", BASE_DATE, YC_DATES, YC_RATES, OFFSET);
    CURVES = new ISDAYieldCurveAndHazardRateCurveProvider(YIELD_CURVE, HAZARD_RATE_CURVE);
  }

  @Test
  public void testNonOverlappingHazardRateAndYieldCurveDates() {
    final ZonedDateTime[] hrDates = new ZonedDateTime[HR_DATES.length];
    final double[] hrTimes = new double[HR_TIMES.length];
    for (int i = 0; i < HR_DATES.length; i++) {
      hrDates[i] = HR_DATES[i].plusDays(12);
      hrTimes[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, hrDates[i]);
    }
    final HazardRateCurve hazardRateCurve = new HazardRateCurve(hrDates, hrTimes, HR_RATES, OFFSET);
    CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithProtectionStart(true).withMaturityDate(VALUATION_DATE.plusYears(50));
    final ISDAYieldCurveAndHazardRateCurveProvider curves = new ISDAYieldCurveAndHazardRateCurveProvider(YIELD_CURVE, hazardRateCurve);
    ZonedDateTime startDate = getStartDate(cds);
    ZonedDateTime endDate = cds.getMaturityDate();
    double[] actual = CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cds, curves);
    double[] dep = DEPRECATED_CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cds, YIELD_CURVE, hazardRateCurve);
    int nActual = actual.length;
    int nDep = dep.length;
    assertEquals(nDep, nActual);
    for (int i = 0; i < nDep; ++i) {
      assertEquals(actual[i], dep[i]);
    }
    cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithProtectionStart(false).withMaturityDate(VALUATION_DATE.plusYears(50));
    startDate = getStartDate(cds);
    endDate = cds.getMaturityDate();
    actual = CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cds, curves);
    dep = DEPRECATED_CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cds, YIELD_CURVE, hazardRateCurve);
    nActual = actual.length;
    nDep = dep.length;
    assertEquals(nDep, nActual);
    for (int i = 0; i < nDep; ++i) {
      assertEquals(actual[i], dep[i]);
    }
  }

  @Test
  public void testCDSWithinCurves() {
    CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithProtectionStart(true)
        .withMaturityDate(YC_DATES[YC_DATES.length - 1].minusMonths(1))
        .withEffectiveDate(VALUATION_DATE.minusMonths(1).plusDays(1))
        .withStartDate(VALUATION_DATE.minusMonths(1));
    final ISDAYieldCurveAndHazardRateCurveProvider curves = new ISDAYieldCurveAndHazardRateCurveProvider(YIELD_CURVE, HAZARD_RATE_CURVE);
    ZonedDateTime startDate = getStartDate(cds);
    ZonedDateTime endDate = cds.getMaturityDate();
    double[] actual = CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cds, CURVES);
    double[] dep = DEPRECATED_CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cds, YIELD_CURVE, HAZARD_RATE_CURVE);
    int nActual = actual.length;
    int nDep = dep.length;
    assertEquals(nDep, nActual);
    for (int i = 0; i < nDep; ++i) {
      assertEquals(actual[i], dep[i]);
    }
    cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithProtectionStart(false)
        .withMaturityDate(YC_DATES[YC_DATES.length - 1].minusMonths(1))
        .withEffectiveDate(VALUATION_DATE.minusMonths(1).plusDays(1))
        .withStartDate(VALUATION_DATE.minusMonths(1));
    startDate = getStartDate(cds);
    endDate = cds.getMaturityDate();
    actual = CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cds, CURVES);
    dep = DEPRECATED_CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cds, YIELD_CURVE, HAZARD_RATE_CURVE);
    nActual = actual.length;
    nDep = dep.length;
    assertEquals(nDep, nActual);
    for (int i = 0; i < nDep; ++i) {
      assertEquals(actual[i], dep[i]);
    }
  }

  @Test
  public void regressionTest() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final ZonedDateTime startDate = getStartDate(cds);
    final ZonedDateTime endDate = cds.getMaturityDate();
    final double[] deprecatedResult = DEPRECATED_CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cds, YIELD_CURVE, HAZARD_RATE_CURVE);
    final double[] result = CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cds, CURVES);
    assertArrayEquals(deprecatedResult, result, EPS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCdSTest() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final ZonedDateTime startDate = getStartDate(cds);
    final ZonedDateTime endDate = cds.getMaturityDate();
    final CreditDefaultSwapDefinition nCdS = null;
    CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, nCdS, CURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurveTest() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final ZonedDateTime startDate = getStartDate(cds);
    final ZonedDateTime endDate = cds.getMaturityDate();
    final ISDAYieldCurveAndHazardRateCurveProvider cv = null;
    CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cds, cv);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void endBeforeStartTest() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final ZonedDateTime startDate = getStartDate(cds);
    final ZonedDateTime endDate = cds.getMaturityDate();
    CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, endDate, startDate, cds, CURVES);
  }

  //  @Test(expectedExceptions = IllegalArgumentException.class)
  //  public void endBeforeValuateTest() {
  //    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
  //    final ZonedDateTime startDate = getStartDate(cds);
  //    final ZonedDateTime endDate = cds.getMaturityDate();
  //    CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(endDate, startDate, VALUATION_DATE, cds, CURVES);
  //  }

  @Test(enabled = false)
  public void timeBDeprecated() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final ZonedDateTime startDate = getStartDate(cds);
    final ZonedDateTime endDate = cds.getMaturityDate();
    final double startTime = System.currentTimeMillis();
    int j = 0;
    for (int i = 0; i < 1000000; i++) {
      DEPRECATED_CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cds, YIELD_CURVE, HAZARD_RATE_CURVE);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Deprecated:\t" + (endTime - startTime) / j * 100);
  }

  @Test(enabled = false)
  public void timeARefactored() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final ZonedDateTime startDate = getStartDate(cds);
    final ZonedDateTime endDate = cds.getMaturityDate();
    final double startTime = System.currentTimeMillis();
    int j = 0;
    for (int i = 0; i < 5000000; i++) {
      CALCULATOR.constructCreditDefaultSwapContingentLegIntegrationSchedule(VALUATION_DATE, startDate, endDate, cds, CURVES);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Refactored:\t" + (endTime - startTime) / j * 100);
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
