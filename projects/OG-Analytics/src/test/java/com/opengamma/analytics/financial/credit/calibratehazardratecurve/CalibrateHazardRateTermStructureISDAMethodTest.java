package com.opengamma.analytics.financial.credit.calibratehazardratecurve;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
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
 * Tests of Deprecated Hazard Rate Curve Calibrator
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
public class CalibrateHazardRateTermStructureISDAMethodTest {

  private static final CalibrateHazardRateTermStructureISDAMethod DEPRECATED_CALCULATOR = new CalibrateHazardRateTermStructureISDAMethod();
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2013, 3, 4);
  private static final ZonedDateTime[] MARKET_TENORS = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 6, 19), DateUtils.getUTCDate(2013, 9, 18),
      DateUtils.getUTCDate(2013, 12, 18), DateUtils.getUTCDate(2014, 3, 19), DateUtils.getUTCDate(2015, 3, 18), DateUtils.getUTCDate(2016, 3, 16), DateUtils.getUTCDate(2018, 3, 15),
      DateUtils.getUTCDate(2023, 3, 15) };
  private static final double[] MARKET_TIMES = new double[MARKET_TENORS.length + 1];
  private static final double[] MARKET_SPREADS = new double[] {315, 350, 390, 400, 420, 410, 404, 402 };

  private static final double[] ZERO_SPREADS = new double[MARKET_TENORS.length];
  private static final double[] ZERO_HAZARD_RATES = new double[MARKET_TENORS.length + 1];

  private static final ZonedDateTime[] YIELD_TENORS = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 4, 1), DateUtils.getUTCDate(2013, 5, 1), DateUtils.getUTCDate(2013, 6, 1),
      DateUtils.getUTCDate(2013, 12, 1), DateUtils.getUTCDate(2014, 3, 1), DateUtils.getUTCDate(2015, 3, 1), DateUtils.getUTCDate(2016, 3, 1), DateUtils.getUTCDate(2018, 3, 1),
      DateUtils.getUTCDate(2023, 3, 1) };
  private static final double[] YIELD_TIMES = new double[YIELD_TENORS.length];
  private static final double[] YIELDS = new double[] {0.005, 0.006, 0.008, 0.009, 0.01, 0.012, 0.015, 0.02, 0.03 };
  private static final double OFFSET = 0;
  private static final ISDADateCurve YIELD_CURVE;
  private static final LegacyVanillaCreditDefaultSwapDefinition CDS =
      CreditDefaultSwapDefinitionDataSets.getLegacyVanillaDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  static {
    Arrays.fill(ZERO_SPREADS, 0.0);
    Arrays.fill(ZERO_HAZARD_RATES, 0.0);

    MARKET_TIMES[0] = 0.0;
    for (int i = 0; i < MARKET_TENORS.length; i++) {
      MARKET_TIMES[i+1] = ACT_365.getDayCountFraction(VALUATION_DATE, MARKET_TENORS[i]);
    }

    for (int i = 0; i < YIELD_TENORS.length; i++) {
      YIELD_TIMES[i] = TimeCalculator.getTimeBetween(VALUATION_DATE, YIELD_TENORS[i]);
    }
    YIELD_CURVE = new ISDADateCurve("ISDA", YIELD_TENORS, YIELD_TIMES, YIELDS, OFFSET);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullValuationDate() {
    DEPRECATED_CALCULATOR.isdaCalibrateHazardRateCurve(null, CDS, MARKET_TENORS, MARKET_SPREADS, YIELD_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCDS() {
    DEPRECATED_CALCULATOR.isdaCalibrateHazardRateCurve(VALUATION_DATE, null, MARKET_TENORS, MARKET_SPREADS, YIELD_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSpreadDates() {
    DEPRECATED_CALCULATOR.isdaCalibrateHazardRateCurve(VALUATION_DATE, CDS, null, MARKET_SPREADS, YIELD_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSpreads() {
    DEPRECATED_CALCULATOR.isdaCalibrateHazardRateCurve(VALUATION_DATE, CDS, MARKET_TENORS, null, YIELD_CURVE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYieldCurve() {
    final HazardRateCurve result = DEPRECATED_CALCULATOR.isdaCalibrateHazardRateCurve(VALUATION_DATE, CDS, MARKET_TENORS, MARKET_SPREADS, null);
    assertNotNull(result);
  }

  /**
   * Tests trivial example in which there is no credit risk, i.e. par spreads for all CDS are zero
   */
  @Test
  public void testCalibrationToZeroSpreadsSucceeds() {

    final HazardRateCurve result = DEPRECATED_CALCULATOR.isdaCalibrateHazardRateCurve(VALUATION_DATE, CDS, MARKET_TENORS, ZERO_SPREADS, YIELD_CURVE);
    assertNotNull(result);
  }

  /**
   * Regression test to highlight any changes made to the calibrator. <p>
   * Tests trivial example in which there is no credit risk, i.e. par spreads for all CDS are zero,
   * and thus zero hazard rates for all tenors.
   */
  @Test
  public void testRegressionOnZeroSpreads() {
    final HazardRateCurve curve = DEPRECATED_CALCULATOR.isdaCalibrateHazardRateCurve(VALUATION_DATE, CDS, MARKET_TENORS, ZERO_SPREADS, YIELD_CURVE);
    final HazardRateCurve curveExpected = new HazardRateCurve(MARKET_TENORS, MARKET_TIMES, ZERO_HAZARD_RATES, 0);
    assertTrue("Calibrated hazard rates have changed.", Arrays.equals(curve.getRates(), curveExpected.getRates()));
    assertTrue("Calibrated hazard times have changed.", Arrays.equals(curve.getTimes(), curveExpected.getTimes()));
    assertTrue("Calibrated ZeroDiscountFactor has changed.", curve.getZeroDiscountFactor() == curveExpected.getZeroDiscountFactor());
    assertTrue("Calibrated hazard rate curve has changed.", curve.equals(curveExpected));
  }

  /**
   * Test that calibration succeeds with realistic inputs
   * This regression test fails because CalibrateHazardRateTermStructureISDAMethod fails to calibrate to market spreads.
   * As of writing this, 16/06/2013, it fails on the first point. See {@link ISDAHazardRateCurveCalculatorTest}
   */
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testCalibrationToRealisticSpreadsSucceeds() {
    final HazardRateCurve result = DEPRECATED_CALCULATOR.isdaCalibrateHazardRateCurve(VALUATION_DATE, CDS, MARKET_TENORS, MARKET_SPREADS, YIELD_CURVE);
    assertNotNull(result);
  }



}
