/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isda;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndHazardRateCurveProvider;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class ISDACompliantPremiumLegCalculatorTest {
  private static final PresentValueCreditDefaultSwap DEPRECATED_CALCULATOR = new PresentValueCreditDefaultSwap();
  private static final ISDACompliantPremiumLegCalculator CALCULATOR = new ISDACompliantPremiumLegCalculator();
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

  @Test(enabled = false)
  public void regressionTest() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final double deprecatedResult = DEPRECATED_CALCULATOR.calculatePremiumLeg(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_RATE_CURVE, PriceType.CLEAN);
    final double result = CALCULATOR.calculateLeg(VALUATION_DATE, cds, CURVES, PriceType.CLEAN);
    assertEquals(deprecatedResult, result, EPS);
  }

  @Test(enabled = false)
  public void timeBDeprecated() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final double startTime = System.currentTimeMillis();
    int j = 0;
    for (int i = 0; i < 100000; i++) {
      DEPRECATED_CALCULATOR.calculatePremiumLeg(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_RATE_CURVE, PriceType.CLEAN);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Deprecated:\t" + (endTime - startTime) / j * 100);
  }

  @Test(enabled = false)
  public void timeARefactored() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final double startTime = System.currentTimeMillis();
    int j = 0;
    for (int i = 0; i < 100000; i++) {
      CALCULATOR.calculateLeg(VALUATION_DATE, cds, CURVES, PriceType.CLEAN);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Refactored:\t" + (endTime - startTime) / j * 100);
  }

}
