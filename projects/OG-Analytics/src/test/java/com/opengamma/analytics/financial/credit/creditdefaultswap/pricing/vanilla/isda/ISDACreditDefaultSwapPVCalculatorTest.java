/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isda;

import static com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets.getLegacyFixedRecoveryDefinition;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets.getLegacyForwardStartingDefinition;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets.getLegacyMuniDefinition;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets.getLegacyQuantoDefinition;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets.getLegacyRecoveryLockDefinition;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets.getLegacySovereignDefinition;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets.getStandardFixedRecoveryDefinition;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets.getStandardForwardStartingDefinition;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets.getStandardMuniDefinition;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets.getStandardQuantoDefinition;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets.getStandardRecoveryLockDefinition;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets.getStandardSovereignpDefinition;
import static com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets.getStandardVanillaDefinition;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndHazardRateCurveProvider;
import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.legacy.PresentValueLegacyCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class ISDACreditDefaultSwapPVCalculatorTest {
  private static final PresentValueLegacyCreditDefaultSwap DEPRECATED_CALCULATOR = new PresentValueLegacyCreditDefaultSwap();
  private static final ISDACreditDefaultSwapPVCalculator CALCULATOR = new ISDACreditDefaultSwapPVCalculator();
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
  private static final ISDAYieldCurveAndHazardRateCurveProvider CURVE_PROVIDER;
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
    CURVE_PROVIDER = new ISDAYieldCurveAndHazardRateCurveProvider(YIELD_CURVE, HAZARD_RATE_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCDS() {
    CALCULATOR.getPresentValue(null, CURVE_PROVIDER, VALUATION_DATE, PriceType.CLEAN);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    final LegacyVanillaCreditDefaultSwapDefinition cds = getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    CALCULATOR.getPresentValue(cds, null, VALUATION_DATE, PriceType.CLEAN);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValuationDate() {
    final LegacyVanillaCreditDefaultSwapDefinition cds = getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    CALCULATOR.getPresentValue(cds, CURVE_PROVIDER, null, PriceType.CLEAN);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPriceType() {
    final LegacyVanillaCreditDefaultSwapDefinition cds = getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    CALCULATOR.getPresentValue(cds, CURVE_PROVIDER, VALUATION_DATE, null);
  }

  @Test
  public void testCDSTypes() {
    final CreditDefaultSwapDefinition cds = getLegacyVanillaDefinition();
    final PriceType priceType = PriceType.DIRTY;
    final double pv = CALCULATOR.getPresentValue(cds, CURVE_PROVIDER, VALUATION_DATE, priceType);
    assertEquals(pv, CALCULATOR.getPresentValue(getStandardVanillaDefinition(), CURVE_PROVIDER, VALUATION_DATE, priceType));
    assertEquals(pv, CALCULATOR.getPresentValue(getStandardFixedRecoveryDefinition(), CURVE_PROVIDER, VALUATION_DATE, priceType));
    assertEquals(pv, CALCULATOR.getPresentValue(getStandardForwardStartingDefinition(), CURVE_PROVIDER, VALUATION_DATE, priceType));
    assertEquals(pv, CALCULATOR.getPresentValue(getStandardMuniDefinition(), CURVE_PROVIDER, VALUATION_DATE, priceType));
    assertEquals(pv, CALCULATOR.getPresentValue(getStandardQuantoDefinition(), CURVE_PROVIDER, VALUATION_DATE, priceType));
    assertEquals(pv, CALCULATOR.getPresentValue(getStandardRecoveryLockDefinition(), CURVE_PROVIDER, VALUATION_DATE, priceType));
    assertEquals(pv, CALCULATOR.getPresentValue(getStandardSovereignpDefinition(), CURVE_PROVIDER, VALUATION_DATE, priceType));
    assertEquals(pv, CALCULATOR.getPresentValue(getLegacyFixedRecoveryDefinition(), CURVE_PROVIDER, VALUATION_DATE, priceType));
    assertEquals(pv, CALCULATOR.getPresentValue(getLegacyForwardStartingDefinition(), CURVE_PROVIDER, VALUATION_DATE, priceType));
    assertEquals(pv, CALCULATOR.getPresentValue(getLegacyMuniDefinition(), CURVE_PROVIDER, VALUATION_DATE, priceType));
    assertEquals(pv, CALCULATOR.getPresentValue(getLegacyQuantoDefinition(), CURVE_PROVIDER, VALUATION_DATE, priceType));
    assertEquals(pv, CALCULATOR.getPresentValue(getLegacyRecoveryLockDefinition(), CURVE_PROVIDER, VALUATION_DATE, priceType));
    assertEquals(pv, CALCULATOR.getPresentValue(getLegacySovereignDefinition(), CURVE_PROVIDER, VALUATION_DATE, priceType));
  }

  @Test
  public void testBuySell() {
    CreditDefaultSwapDefinition buy = getStandardVanillaDefinition(BuySellProtection.BUY);
    CreditDefaultSwapDefinition sell = getStandardVanillaDefinition(BuySellProtection.SELL);
    assertEquals(-CALCULATOR.getPresentValue(sell, CURVE_PROVIDER, VALUATION_DATE, PriceType.CLEAN),
        CALCULATOR.getPresentValue(buy, CURVE_PROVIDER, VALUATION_DATE, PriceType.CLEAN));
    buy = getLegacyVanillaDefinition(BuySellProtection.BUY);
    sell = getLegacyVanillaDefinition(BuySellProtection.SELL);
    assertEquals(-CALCULATOR.getPresentValue(sell, CURVE_PROVIDER, VALUATION_DATE, PriceType.CLEAN),
        CALCULATOR.getPresentValue(buy, CURVE_PROVIDER, VALUATION_DATE, PriceType.CLEAN));
  }

  @Test(enabled = true)
  public void regressionTestCleanPrice() {
    final LegacyVanillaCreditDefaultSwapDefinition cds = getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final double deprecatedResult = DEPRECATED_CALCULATOR.getPresentValueLegacyCreditDefaultSwap(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_RATE_CURVE, PriceType.CLEAN);
    final double result = CALCULATOR.getPresentValue(cds, CURVE_PROVIDER, VALUATION_DATE, PriceType.CLEAN);
    assertEquals(deprecatedResult, result, EPS);
  }
  
  @Test(enabled = true)
  public void regressionTestDirtyPrice() {
    final LegacyVanillaCreditDefaultSwapDefinition cds = getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final double deprecatedResult = DEPRECATED_CALCULATOR.getPresentValueLegacyCreditDefaultSwap(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_RATE_CURVE, PriceType.DIRTY);
    final double result = CALCULATOR.getPresentValue(cds, CURVE_PROVIDER, VALUATION_DATE, PriceType.DIRTY);
    assertEquals(deprecatedResult, result, EPS);
  }

  @Test(enabled = false)
  public void timeABDeprecated() {
    final LegacyVanillaCreditDefaultSwapDefinition cds = getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final double startTime = System.currentTimeMillis();
    int j = 0;
    for (int i = 0; i < 100000; i++) {
      DEPRECATED_CALCULATOR.getPresentValueLegacyCreditDefaultSwap(VALUATION_DATE, cds, YIELD_CURVE, HAZARD_RATE_CURVE, PriceType.CLEAN);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Deprecated:\t" + (endTime - startTime) / j * 100);
  }

  @Test(enabled = false)
  public void timeACRefactored() {
    final LegacyVanillaCreditDefaultSwapDefinition cds = getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final double startTime = System.currentTimeMillis();
    int j = 0;
    for (int i = 0; i < 100000; i++) {
      CALCULATOR.getPresentValue(cds, CURVE_PROVIDER, VALUATION_DATE, PriceType.CLEAN);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Refactored:\t" + (endTime - startTime) / j * 100);
  }

}
