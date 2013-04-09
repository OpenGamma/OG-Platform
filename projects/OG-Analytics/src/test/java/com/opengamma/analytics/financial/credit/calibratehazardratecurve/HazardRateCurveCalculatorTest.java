/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.calibratehazardratecurve;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndSpreadsProvider;
import com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class HazardRateCurveCalculatorTest {
  private static final PresentValueCreditDefaultSwap DEPRECATED_CALCULATOR = new PresentValueCreditDefaultSwap();
  private static final HazardRateCurveCalculator CALCULATOR = new HazardRateCurveCalculator();
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2013, 3, 4);
  private static final ZonedDateTime[] MARKET_TENORS = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 3, 20), DateUtils.getUTCDate(2013, 6, 19), DateUtils.getUTCDate(2013, 9, 18),
    DateUtils.getUTCDate(2013, 12, 18), DateUtils.getUTCDate(2014, 3, 19), DateUtils.getUTCDate(2015, 3, 18), DateUtils.getUTCDate(2016, 3, 16), DateUtils.getUTCDate(2018, 3, 15),
    DateUtils.getUTCDate(2023, 3, 15) };
  private static final double[] MARKET_SPREADS = new double[] {300, 315, 350, 390, 400, 420, 410, 404, 402 };
  private static final ZonedDateTime[] YIELD_TENORS = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 4, 1), DateUtils.getUTCDate(2013, 5, 1), DateUtils.getUTCDate(2013, 6, 1),
    DateUtils.getUTCDate(2013, 12, 1), DateUtils.getUTCDate(2014, 3, 1), DateUtils.getUTCDate(2015, 3, 1), DateUtils.getUTCDate(2016, 3, 1), DateUtils.getUTCDate(2018, 3, 1),
    DateUtils.getUTCDate(2023, 3, 1) };
  private static final double[] YIELD_TIMES = new double[YIELD_TENORS.length];
  private static final double[] YIELDS = new double[] {0.005, 0.006, 0.008, 0.009, 0.01, 0.012, 0.015, 0.02, 0.03 };
  private static final double OFFSET = 0;
  private static final ISDADateCurve YIELD_CURVE;
  private static final ISDAYieldCurveAndSpreadsProvider CURVES_DATA;
  private static final LegacyVanillaCreditDefaultSwapDefinition CDS =
      CreditDefaultSwapDefinitionDataSets.getLegacyVanillaCreditDefaultSwapDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
  private static final double BP = 10000;
  private static final double EPS = 1e-15;

  static {
    for (int i = 0; i < YIELD_TENORS.length; i++) {
      YIELD_TIMES[i] = TimeCalculator.getTimeBetween(VALUATION_DATE, YIELD_TENORS[i]);
    }
    YIELD_CURVE = new ISDADateCurve("ISDA", YIELD_TENORS, YIELD_TIMES, YIELDS, OFFSET);
    CURVES_DATA = new ISDAYieldCurveAndSpreadsProvider(MARKET_TENORS, MARKET_SPREADS, YIELD_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCDS() {
    CALCULATOR.calibrateHazardRateCurve(null, CURVES_DATA, VALUATION_DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    CALCULATOR.calibrateHazardRateCurve(CDS, null, VALUATION_DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tesetNullValuationDate() {
    CALCULATOR.calibrateHazardRateCurve(CDS, CURVES_DATA, null);
  }

  @Test
  public void testCurve() {
    final HazardRateCurve curve = CALCULATOR.calibrateHazardRateCurve(CDS, CURVES_DATA, VALUATION_DATE);

  }

  @Test
  public void testDeprecated() {
    final HazardRateCurve curve1 = CALCULATOR.calibrateHazardRateCurve(CDS, CURVES_DATA, VALUATION_DATE);
    final HazardRateCurve curve2 = DEPRECATED_CALCULATOR.calibrateHazardRateCurve(VALUATION_DATE, CDS, MARKET_TENORS, MARKET_SPREADS, YIELD_CURVE);
    assertEquals(curve1, curve2);
  }

  @Test(enabled = false)
  public void timeBDeprecated() {
    final double startTime = System.currentTimeMillis();
    int j = 0;
    for (int i = 0; i < 2000; i++) {
      DEPRECATED_CALCULATOR.calibrateHazardRateCurve(VALUATION_DATE, CDS, MARKET_TENORS, MARKET_SPREADS, YIELD_CURVE);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Deprecated:\t" + (endTime - startTime) / j * 100);
  }

  @Test(enabled = false)
  public void timeARefactored() {
    final double startTime = System.currentTimeMillis();
    int j = 0;
    for (int i = 0; i < 2000; i++) {
      CALCULATOR.calibrateHazardRateCurve(CDS, CURVES_DATA, VALUATION_DATE);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Refactored:\t" + (endTime - startTime) / j * 100);
  }
}
