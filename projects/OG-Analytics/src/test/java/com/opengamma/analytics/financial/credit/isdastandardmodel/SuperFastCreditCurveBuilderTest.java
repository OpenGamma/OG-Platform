/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurveBuilder.ArbitrageHandling;
import com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration.CreditCurveCalibrator;
import com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration.SuperFastCreditCurveBuilder;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SuperFastCreditCurveBuilderTest extends CreditCurveCalibrationTest {

  private static final SuperFastCreditCurveBuilder BUILDER_ISDA = new SuperFastCreditCurveBuilder();

  //  private static final SuperFastCreditCurveBuilder BUILDER_MARKIT = new SuperFastCreditCurveBuilder(MARKIT_FIX);

  @Test
  public void test() {
    testCalibrationAgainstISDA(BUILDER_ISDA, 1e-14);

    //NOTE: we do not match the Markit 'fix' for forward starting swaps 
    // testCalibrationAgainstISDA(BUILDER_MARKIT, 1e-14);
  }

  @Test
  public void arbitrageHandlingTest() {
    final FastCreditCurveBuilder FastOgZero = new FastCreditCurveBuilder(OG_FIX, ArbitrageHandling.ZeroHazardRate);

    final SuperFastCreditCurveBuilder fastOgZero = new SuperFastCreditCurveBuilder(OG_FIX, ArbitrageHandling.ZeroHazardRate);
    final SuperFastCreditCurveBuilder fastOgFail = new SuperFastCreditCurveBuilder(OG_FIX, ArbitrageHandling.Fail);

    final LocalDate tradeDate = LocalDate.of(2013, Month.APRIL, 25);

    final CDSAnalyticFactory baseFactory = new CDSAnalyticFactory();
    final CDSAnalyticFactory noAccFactory = baseFactory.withPayAccOnDefault(false);
    final Period[] tenors = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(3), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
    final CDSAnalytic[] pillar = noAccFactory.makeIMMCDS(tradeDate, tenors);
    final double[] spreads = new double[] {0.027, 0.017, 0.012, 0.009, 0.008, 0.005 };

    final LocalDate spotDate = addWorkDays(tradeDate.minusDays(1), 3, DEFAULT_CALENDAR);
    final String[] yieldCurvePoints = new String[] {"1M", "2M", "3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
    final String[] yieldCurveInstruments = new String[] {"M", "M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
    final double[] rates = new double[] {0.00445, 0.009488, 0.012337, 0.017762, 0.01935, 0.020838, 0.01652, 0.02018, 0.023033, 0.02525, 0.02696, 0.02825, 0.02931, 0.03017, 0.03092, 0.0316, 0.03231,
        0.03367, 0.03419, 0.03411, 0.03412 };
    final ISDACompliantYieldCurve yc = makeYieldCurve(tradeDate, spotDate, yieldCurvePoints, yieldCurveInstruments, rates, ACT360, D30360, Period.ofYears(1));

    final ISDACompliantCreditCurve curveSuperFast = fastOgZero.calibrateCreditCurve(pillar, spreads, yc);
    final ISDACompliantCreditCurve curveFast = FastOgZero.calibrateCreditCurve(pillar, spreads, yc);
    final double[] sampleTime = new double[] {30 / 365., 90 / 365., 180. / 365., 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
    final int num = sampleTime.length;
    for (int i = 0; i < num; ++i) {
      assertEquals(curveFast.getHazardRate(sampleTime[i]), curveSuperFast.getHazardRate(sampleTime[i]), 1.e-12);
    }

    try {
      fastOgFail.calibrateCreditCurve(pillar, spreads, yc);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }

    final double coupon = 0.025;
    final MarketQuoteConverter conv = new MarketQuoteConverter();
    final int nSpreads = spreads.length;
    final PointsUpFront[] pufsFail = new PointsUpFront[nSpreads];
    final double[] pufValues = conv.parSpreadsToPUF(pillar, coupon, yc, spreads);
    for (int i = 0; i < nSpreads; ++i) {
      pufsFail[i] = new PointsUpFront(coupon, pufValues[i]);
    }
    try {
      fastOgFail.calibrateCreditCurve(pillar, pufsFail, yc);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
  }

  /**
   * Tests below for CreditCurveCalibrator
   */
  public void almostFlatRateTest() {
    final LocalDate tradeDate = LocalDate.of(2013, Month.APRIL, 25);

    final CDSAnalyticFactory baseFactory = new CDSAnalyticFactory();
    final Period[] tenors = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(3), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
    final CDSAnalytic[] pillar = baseFactory.makeIMMCDS(tradeDate, tenors);
    final LocalDate spotDate = addWorkDays(tradeDate.minusDays(1), 3, DEFAULT_CALENDAR);
    final String[] yieldCurvePoints = new String[] {"1M", "2M", "3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
    final String[] yieldCurveInstruments = new String[] {"M", "M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
    final double[] rates = new double[] {0.00462, 0.00231, 0.00154, 0.017762, 0.01935, 0.020838, 0.01652, 0.02018, 0.023033, 0.02525, 0.02696, 0.02825, 0.02931, 0.03017, 0.03092, 0.0316, 0.03231,
        0.03367, 0.03419, 0.03411, 0.03412 };
    final ISDACompliantYieldCurve yc = makeYieldCurve(tradeDate, spotDate, yieldCurvePoints, yieldCurveInstruments, rates, ACT360, D30360, Period.ofYears(1));
    final double[] spreads = new double[] {2.7e-5, 2.7e-4, 2.7e-3, 2.7e-3, 2.7e-2, 2.7e-2 };

    final CreditCurveCalibrator calib = new CreditCurveCalibrator(pillar, yc);
    final ISDACompliantCreditCurve curve = calib.calibrate(spreads);

    final FastCreditCurveBuilder fastBuild = new FastCreditCurveBuilder();
    final ISDACompliantCreditCurve curveFast = fastBuild.calibrateCreditCurve(pillar, spreads, yc);

    final double[] sampleTime = new double[] {30 / 365., 90 / 365., 180. / 365., 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
    final int num = sampleTime.length;
    for (int i = 0; i < num; ++i) {
      assertEquals(curveFast.getHazardRate(sampleTime[i]), curve.getHazardRate(sampleTime[i]), 1.e-6);
    }
  }

  /**
   * 
   */
  public void hashCodeEqualsTest() {
    final LocalDate tradeDate = LocalDate.of(2013, Month.APRIL, 25);

    final CDSAnalyticFactory baseFactory = new CDSAnalyticFactory();
    final CDSAnalyticFactory recFactory = baseFactory.withRecoveryRate(0.25);
    final Period[] tenors = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(3), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
    final CDSAnalytic[] pillar = baseFactory.makeIMMCDS(tradeDate, tenors);
    final CDSAnalytic[] pillarShort = Arrays.copyOf(pillar, pillar.length - 1);
    final CDSAnalytic[] pillarRec = recFactory.makeIMMCDS(tradeDate, tenors);

    final LocalDate spotDate = addWorkDays(tradeDate.minusDays(1), 3, DEFAULT_CALENDAR);
    final String[] yieldCurvePoints = new String[] {"1M", "2M", "3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
    final String[] yieldCurveInstruments = new String[] {"M", "M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
    final double[] rates = new double[] {0.00445, 0.009488, 0.012337, 0.017762, 0.01935, 0.020838, 0.01652, 0.02018, 0.023033, 0.02525, 0.02696, 0.02825, 0.02931, 0.03017, 0.03092, 0.0316, 0.03231,
        0.03367, 0.03419, 0.03411, 0.03412 };
    final double[] ratesDiff = new double[] {0.00445, 0.009489, 0.012337, 0.017762, 0.01935, 0.020838, 0.01652, 0.02018, 0.023033, 0.02525, 0.02696, 0.02825, 0.02931, 0.03017, 0.03092, 0.0316,
        0.03231, 0.03367, 0.03419, 0.03411, 0.03412 };
    final ISDACompliantYieldCurve yc = makeYieldCurve(tradeDate, spotDate, yieldCurvePoints, yieldCurveInstruments, rates, ACT360, D30360, Period.ofYears(1));
    final ISDACompliantYieldCurve ycDiff = makeYieldCurve(tradeDate, spotDate, yieldCurvePoints, yieldCurveInstruments, ratesDiff, ACT360, D30360, Period.ofYears(1));

    final CreditCurveCalibrator calib = new CreditCurveCalibrator(pillar, yc);
    final CreditCurveCalibrator calib1 = calib;
    final CreditCurveCalibrator calib2 = new CreditCurveCalibrator(pillar, yc);
    final CreditCurveCalibrator calib3 = new CreditCurveCalibrator(pillar, yc, AccrualOnDefaultFormulae.Correct, ArbitrageHandling.Ignore);
    final CreditCurveCalibrator calib4 = new CreditCurveCalibrator(pillar, yc, AccrualOnDefaultFormulae.OrignalISDA, ArbitrageHandling.ZeroHazardRate);
    final CreditCurveCalibrator calib5 = new CreditCurveCalibrator(pillarShort, yc);
    final CreditCurveCalibrator calib6 = new CreditCurveCalibrator(pillar, ycDiff);
    final CreditCurveCalibrator calib7 = new CreditCurveCalibrator(pillarRec, yc);

    final Object[] arrCal = new Object[] {calib1, calib2, calib3, calib4, calib5, calib6, calib7, new FastCreditCurveBuilder() };

    assertTrue(calib.equals(calib));
    assertFalse(calib.equals(null));
    for (int i = 0; i < arrCal.length; ++i) {
      if (calib.equals(arrCal[i])) {
        assertTrue(arrCal[i].equals(calib));
        assertTrue(calib.hashCode() == arrCal[i].hashCode());
      }
      if (calib.hashCode() != arrCal[i].hashCode()) {
        assertFalse(calib.equals(arrCal[i]));
      }
    }

  }
}
