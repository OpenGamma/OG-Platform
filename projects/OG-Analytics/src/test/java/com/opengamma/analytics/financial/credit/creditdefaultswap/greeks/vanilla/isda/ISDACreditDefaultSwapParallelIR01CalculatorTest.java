/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla.isda;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla.IR01CreditDefaultSwap;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.InterestRateBumpType;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class ISDACreditDefaultSwapParallelIR01CalculatorTest {
  private static final IR01CreditDefaultSwap DEPRECATED_CALCULATOR = new IR01CreditDefaultSwap();
  private static final ISDACreditDefaultSwapParallelIR01Calculator CALCULATOR = new ISDACreditDefaultSwapParallelIR01Calculator();
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
  private static final double[] YC_TIMES;
  private static final double[] YC_RATES = new double[] {0.005, 0.006, 0.008, 0.009, 0.01, 0.012, 0.015, 0.02, 0.03 };
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final double OFFSET = 1. / 365;
  private static final double BP = 0.0001;
  private static final ISDADateCurve YIELD_CURVE;
  private static final double EPS = 1e-15;

  static {
    int n = HR_DATES.length;
    HR_TIMES = new double[n];
    for (int i = 0; i < n; i++) {
      HR_TIMES[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, HR_DATES[i]);
    }
    n = YC_DATES.length;
    YC_TIMES = new double[n];
    for (int i = 0; i < n; i++) {
      YC_TIMES[i] = DAY_COUNT.getDayCountFraction(BASE_DATE, YC_DATES[i]);
    }
    YIELD_CURVE = new ISDADateCurve("ISDA", BASE_DATE, YC_DATES, YC_RATES, OFFSET);
  }

  @Test(enabled = false)
  public void regressionTest() {
    final LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final double deprecatedResult = DEPRECATED_CALCULATOR.getIR01ParallelShiftCreditDefaultSwap(VALUATION_DATE, cds, YIELD_CURVE, HR_DATES, HR_RATES, BP, InterestRateBumpType.ADDITIVE_PARALLEL,
        PriceType.CLEAN);
    final double result = CALCULATOR.getIR01ParallelShiftCreditDefaultSwap(VALUATION_DATE, cds, YIELD_CURVE, HR_DATES, HR_RATES, BP, InterestRateBumpType.ADDITIVE_PARALLEL, PriceType.CLEAN);
    assertEquals(deprecatedResult, result, EPS);
  }

  @Test(enabled = false)
  public void timeBDeprecated() {
    final LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final double startTime = System.currentTimeMillis();
    double total = 0;
    for (int i = 0; i < 1000; i++) {
      total += DEPRECATED_CALCULATOR.getIR01ParallelShiftCreditDefaultSwap(VALUATION_DATE, cds, YIELD_CURVE, HR_DATES, HR_RATES, BP, InterestRateBumpType.ADDITIVE_PARALLEL, PriceType.CLEAN);
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Deprecated:\t" + (endTime - startTime) / 100);
    System.out.println(total);
  }

  @Test(enabled = false)
  public void timeARefactored() {
    final LegacyVanillaCreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final double startTime = System.currentTimeMillis();
    double total = 0;
    for (int i = 0; i < 1000; i++) {
      total += CALCULATOR.getIR01ParallelShiftCreditDefaultSwap(VALUATION_DATE, cds, YIELD_CURVE, HR_DATES, HR_RATES, BP, InterestRateBumpType.ADDITIVE_PARALLEL, PriceType.CLEAN);
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Refactored:\t" + (endTime - startTime) / 100);
    System.out.println(total);
  }
}
