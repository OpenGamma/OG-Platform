/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.calibratehazardratecurve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndSpreadsProvider;
import com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets;
import com.opengamma.analytics.financial.credit.creditdefaultswap.calibration.CalibrateHazardRateTermStructureISDAMethod;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.time.DateUtils;

/**
 * Tests for the refactored hazard rate curve calculator, which calibrates to quoted par spreads of vanilla credit default swaps.
 * See also {@link CalibrateHazardRateTermStructureISDAMethodTest} and {@link CalibrateHazardRateCurveTest}
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
public class ISDAHazardRateCurveCalculatorTest {

  private static final CalibrateHazardRateTermStructureISDAMethod DEPRECATED_CALCULATOR = new CalibrateHazardRateTermStructureISDAMethod();
  private static final ISDAHazardRateCurveCalculator CALCULATOR = new ISDAHazardRateCurveCalculator();
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2013, 3, 4);
  private static final ZonedDateTime[] MARKET_TENORS = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 3, 20), DateUtils.getUTCDate(2013, 6, 19), DateUtils.getUTCDate(2013, 9, 18),
      DateUtils.getUTCDate(2013, 12, 18), DateUtils.getUTCDate(2014, 3, 19), DateUtils.getUTCDate(2015, 3, 18), DateUtils.getUTCDate(2016, 3, 16), DateUtils.getUTCDate(2018, 3, 15),
      DateUtils.getUTCDate(2023, 3, 15) };
  private static final double[] MARKET_TIMES = new double[MARKET_TENORS.length + 1];
  private static final double[] MARKET_SPREADS = new double[] {300, 315, 350, 390, 400, 420, 410, 404, 402 };
  private static final double[] HAZARD_RATES = new double[] {0.07499999990686775, 0.07499999990686775, 0.07874999990221114, 0.08749999994567285,
    0.09749999993946404, 0.09999999993791184, 0.10499999993480742, 0.10249999993635962, 0.10099999993729095, 0.1004999999376014};
  private static final ZonedDateTime[] YIELD_TENORS = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 4, 1), DateUtils.getUTCDate(2013, 5, 1), DateUtils.getUTCDate(2013, 6, 1),
      DateUtils.getUTCDate(2013, 12, 1), DateUtils.getUTCDate(2014, 3, 1), DateUtils.getUTCDate(2015, 3, 1), DateUtils.getUTCDate(2016, 3, 1), DateUtils.getUTCDate(2018, 3, 1),
      DateUtils.getUTCDate(2023, 3, 1) };
  private static final double[] YIELD_TIMES = new double[YIELD_TENORS.length];
  private static final double[] YIELDS = new double[] {0.005, 0.006, 0.008, 0.009, 0.01, 0.012, 0.015, 0.02, 0.03 };
  private static final double OFFSET = 0;
  private static final ISDADateCurve YIELD_CURVE;
  private static final ISDAYieldCurveAndSpreadsProvider CURVES_DATA;
  private static final LegacyVanillaCreditDefaultSwapDefinition CDS =
      CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  static {
    MARKET_TIMES[0] = 0.0;
    for (int i = 0; i < MARKET_TENORS.length; i++) {
      MARKET_TIMES[i+1] = ACT_365.getDayCountFraction(VALUATION_DATE, MARKET_TENORS[i]);
    }

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
  /**
   * Regression test to highlight any changes made to the calibrator
   */
  public void testRegression() {
    final HazardRateCurve curve = CALCULATOR.calibrateHazardRateCurve(CDS, CURVES_DATA, VALUATION_DATE);
    final HazardRateCurve curveExpected = new HazardRateCurve(MARKET_TENORS, MARKET_TIMES, HAZARD_RATES, 0);
    assertTrue("Calibrated hazard rates have changed.", Arrays.equals(curve.getRates(), curveExpected.getRates()));
    assertTrue("Calibrated hazard times have changed.", Arrays.equals(curve.getTimes(), curveExpected.getTimes()));
    assertTrue("Calibrated ZeroDiscountFactor has changed.", curve.getZeroDiscountFactor() == curveExpected.getZeroDiscountFactor());
    assertTrue("Calibrated hazard rate curve has changed.", curve.equals(curveExpected));
  }

  /**
   * This regression test fails because CalibrateHazardRateTermStructureISDAMethod fails to calibrate to market spreads.
   * As of writing this, 16/06/2013, it fails on the first point. See {@link CalibrateHazardRateTermStructureISDAMethodTest}
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testDeprecated() {
    final HazardRateCurve curve1 = CALCULATOR.calibrateHazardRateCurve(CDS, CURVES_DATA, VALUATION_DATE);
    final HazardRateCurve curve2 = DEPRECATED_CALCULATOR.isdaCalibrateHazardRateCurve(VALUATION_DATE, CDS, MARKET_TENORS, MARKET_SPREADS, YIELD_CURVE);
    assertEquals(curve1, curve2);
  }
}
