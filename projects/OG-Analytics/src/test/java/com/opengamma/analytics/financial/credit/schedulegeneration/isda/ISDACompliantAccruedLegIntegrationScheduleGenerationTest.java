/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.schedulegeneration.isda;

import static org.testng.AssertJUnit.assertEquals;

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
public class ISDACompliantAccruedLegIntegrationScheduleGenerationTest {
  private static final GenerateCreditDefaultSwapIntegrationSchedule DEPRECATED_CALCULATOR = new GenerateCreditDefaultSwapIntegrationSchedule();
  private static final ISDAAccruedLegIntegrationScheduleGenerator CALCULATOR = new ISDAAccruedLegIntegrationScheduleGenerator();
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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCdSTest() {
    final CreditDefaultSwapDefinition cds = null;
    CALCULATOR.constructCreditDefaultSwapAccruedLegIntegrationSchedule(cds, CURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurveTest() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final ISDAYieldCurveAndHazardRateCurveProvider cv = null;
    CALCULATOR.constructCreditDefaultSwapAccruedLegIntegrationSchedule(cds, cv);
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
    final ZonedDateTime[] expected = new ZonedDateTime[hrDates.length + YC_DATES.length + 2];
    for (int i = 0; i < YC_DATES.length; i++) {
      expected[i * 2 + 1] = YC_DATES[i];
    }
    for (int i = 0; i < hrDates.length; i++) {
      expected[i * 2 + 2] = hrDates[i];
    }
    CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithProtectionStart(true).withMaturityDate(VALUATION_DATE.plusYears(50));
    final ISDAYieldCurveAndHazardRateCurveProvider curves = new ISDAYieldCurveAndHazardRateCurveProvider(YIELD_CURVE, hazardRateCurve);
    ZonedDateTime[] actual = CALCULATOR.constructCreditDefaultSwapAccruedLegIntegrationSchedule(cds, curves);
    expected[0] = cds.getStartDate();
    expected[expected.length - 1] = cds.getMaturityDate().plusDays(1);
    assertDateArrayEquals(expected, actual);
    assertDateArrayEquals(expected, DEPRECATED_CALCULATOR.constructCreditDefaultSwapAccruedLegIntegrationSchedule(VALUATION_DATE, cds, YIELD_CURVE, hazardRateCurve, false));
    cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithProtectionStart(false).withMaturityDate(VALUATION_DATE.plusYears(50));
    actual = CALCULATOR.constructCreditDefaultSwapAccruedLegIntegrationSchedule(cds, curves);
    expected[0] = cds.getStartDate();
    expected[expected.length - 1] = cds.getMaturityDate();
    assertDateArrayEquals(expected, actual);
    assertDateArrayEquals(expected, DEPRECATED_CALCULATOR.constructCreditDefaultSwapAccruedLegIntegrationSchedule(VALUATION_DATE, cds, YIELD_CURVE, hazardRateCurve, false));
  }

  @Test
  public void testCDSWithinCurves() {
    CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithProtectionStart(true)
        .withMaturityDate(YC_DATES[YC_DATES.length - 1].minusMonths(1))
        .withEffectiveDate(VALUATION_DATE.minusMonths(1).plusDays(1))
        .withStartDate(VALUATION_DATE.minusMonths(1));
    final ISDAYieldCurveAndHazardRateCurveProvider curves = new ISDAYieldCurveAndHazardRateCurveProvider(YIELD_CURVE, HAZARD_RATE_CURVE);
    ZonedDateTime[] actual = CALCULATOR.constructCreditDefaultSwapAccruedLegIntegrationSchedule(cds, CURVES);
    ZonedDateTime[] expected = new ZonedDateTime[YC_DATES.length + 1];
    for (int i = 0; i < YC_DATES.length; i++) {
      expected[i + 1] = YC_DATES[i];
    }
    expected[0] = cds.getStartDate();
    expected[YC_DATES.length] = cds.getMaturityDate().plusDays(1);
    assertDateArrayEquals(expected, actual);
    assertDateArrayEquals(expected, DEPRECATED_CALCULATOR.constructCreditDefaultSwapAccruedLegIntegrationSchedule(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_RATE_CURVE, false));
    cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinitionWithProtectionStart(false)
        .withMaturityDate(YC_DATES[YC_DATES.length - 1].minusMonths(1))
        .withEffectiveDate(VALUATION_DATE.minusMonths(1).plusDays(1))
        .withStartDate(VALUATION_DATE.minusMonths(1));
    actual = CALCULATOR.constructCreditDefaultSwapAccruedLegIntegrationSchedule(cds, CURVES);
    expected = new ZonedDateTime[YC_DATES.length + 1];
    for (int i = 0; i < YC_DATES.length; i++) {
      expected[i + 1] = YC_DATES[i];
    }
    expected[0] = cds.getStartDate();
    expected[YC_DATES.length] = cds.getMaturityDate();
    assertDateArrayEquals(expected, actual);
    assertDateArrayEquals(expected, DEPRECATED_CALCULATOR.constructCreditDefaultSwapAccruedLegIntegrationSchedule(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_RATE_CURVE, false));
  }

  private void assertDateArrayEquals(final ZonedDateTime[] expected, final ZonedDateTime[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }
}
