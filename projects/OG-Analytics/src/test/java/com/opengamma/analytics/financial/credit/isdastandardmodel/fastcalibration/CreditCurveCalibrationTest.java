/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration;

import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.FastCreditCurveBuilder;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurveBuilder.ArbitrageHandling;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.MultiCDSAnalytic;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CreditCurveCalibrationTest extends com.opengamma.analytics.financial.credit.isdastandardmodel.CreditCurveCalibrationTest {
  private static final CDSAnalyticFactory CDS_FACTORY = new CDSAnalyticFactory();
  private static final Period[] PILLARS = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(7),
    Period.ofYears(10) };

  private static final SuperFastCreditCurveBuilder BUILDER_ISDA = new SuperFastCreditCurveBuilder(ORIGINAL_ISDA);
  private static final SuperFastCreditCurveBuilder BUILDER_MARKIT = new SuperFastCreditCurveBuilder(MARKIT_FIX);

  @Test
  public void test() {
    testCalibrationAgainstISDA(BUILDER_ISDA, 1e-14);
    //TODO adjust the logic to match the incorrect Markit `fix'
    // testCalibrationAgainstISDA(BUILDER_MARKIT, 1e-14);
  }

  @SuppressWarnings("unused")
  @Test
  public void speedTest() {
    final LocalDate tradeDate = LocalDate.of(2013, Month.SEPTEMBER, 5);
    final LocalDate spotDate = addWorkDays(tradeDate.minusDays(1), 1, DEFAULT_CALENDAR);
    final String[] yieldCurvePoints = new String[] {"1M", "2M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
    final String[] yieldCurveInstruments = new String[] {"M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
    //final String[] yieldCurveInstruments = new String[] {"M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M" };
    final double[] rates = new double[] {0.004919, 0.005006, 0.00515, 0.005906, 0.008813, 0.0088, 0.01195, 0.01534, 0.01836, 0.02096, 0.02322, 0.02514, 0.02673, 0.02802, 0.02997, 0.0318, 0.03331,
      0.03383, 0.034 };
    final ISDACompliantYieldCurve yieldCurve = makeYieldCurve(tradeDate, spotDate, yieldCurvePoints, yieldCurveInstruments, rates, ACT_ACT_ISDA, ACT_ACT_ISDA, Period.ofMonths(6));

    final CDSAnalytic[] cds = CDS_FACTORY.makeIMMCDS(tradeDate, PILLARS);
    final int n = PILLARS.length;
    final double[] spreads = new double[] {0.01, 0.012, 0.015, 0.02, 0.023, 0.021, 0.02, 0.019 };
    ArgumentChecker.isTrue(n == spreads.length, "spreads wrong length");

    // final AnalyticCDSPricer pricer = new AnalyticCDSPricer(MARKIT_FIX);
    final CreditCurveCalibrator calibrator1 = new CreditCurveCalibrator(cds, yieldCurve);
    final ISDACompliantCreditCurve cc1 = calibrator1.calibrate(spreads);
    final FastCreditCurveBuilder calibrator2 = new FastCreditCurveBuilder();
    final ISDACompliantCreditCurve cc2 = calibrator2.calibrateCreditCurve(cds, spreads, yieldCurve);
    for (int i = 0; i < n; i++) {
      //System.out.println(cc1.getZeroRateAtIndex(i) + "\t" + cc2.getZeroRateAtIndex(i));
      assertEquals(cc2.getZeroRateAtIndex(i), cc1.getZeroRateAtIndex(i), 1e-15);
    }

    final MultiCDSAnalytic multiCDS = CDS_FACTORY.makeMultiIMMCDS(tradeDate, PILLARS);
    final CreditCurveCalibrator calibrator3 = new CreditCurveCalibrator(multiCDS, yieldCurve);
    final ISDACompliantCreditCurve cc3 = calibrator3.calibrate(spreads);
    for (int i = 0; i < n; i++) {
      assertEquals(cc1.getZeroRateAtIndex(i), cc3.getZeroRateAtIndex(i), 1e-15);
    }

    final int warmups = 200;
    final int hotspots = 1000;

    for (int i = 0; i < warmups; i++) {
      final ISDACompliantCreditCurve cc1a = calibrator1.calibrate(spreads);
      final ISDACompliantCreditCurve cc2a = calibrator2.calibrateCreditCurve(cds, spreads, yieldCurve);
    }

    if (hotspots > 0) {
      long t0 = System.nanoTime();
      for (int i = 0; i < hotspots; i++) {
        //  final CreditCurveCalibrator calibrator1a = new CreditCurveCalibrator(cds, yieldCurve);
        final ISDACompliantCreditCurve cc1a = calibrator1.calibrate(spreads);
      }
      long t1 = System.nanoTime();
      System.out.println("time of new calibration: " + (t1 - t0) / hotspots / 1e6 + "ms");

      t0 = System.nanoTime();
      for (int i = 0; i < hotspots; i++) {
        final ISDACompliantCreditCurve cc2a = calibrator2.calibrateCreditCurve(cds, spreads, yieldCurve);
      }
      t1 = System.nanoTime();
      System.out.println("time of old calibration: " + (t1 - t0) / hotspots / 1e6 + "ms");
    }
  }

  @SuppressWarnings("unused")
  @Test
  public void test2() {
    final LocalDate tradeDate = LocalDate.of(2013, Month.SEPTEMBER, 5);
    final LocalDate spotDate = addWorkDays(tradeDate.minusDays(1), 1, DEFAULT_CALENDAR);
    final String[] yieldCurvePoints = new String[] {"1M", "2M", "3M", "6M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
    final String[] yieldCurveInstruments = new String[] {"M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
    //final String[] yieldCurveInstruments = new String[] {"M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M", "M" };
    final double[] rates = new double[] {0.004919, 0.005006, 0.00515, 0.005906, 0.008813, 0.0088, 0.01195, 0.01534, 0.01836, 0.02096, 0.02322, 0.02514, 0.02673, 0.02802, 0.02997, 0.0318, 0.03331,
      0.03383, 0.034 };
    final ISDACompliantYieldCurve yieldCurve = makeYieldCurve(tradeDate, spotDate, yieldCurvePoints, yieldCurveInstruments, rates, ACT_ACT_ISDA, ACT_ACT_ISDA, Period.ofMonths(6));

    final LocalDate[] maturities = new LocalDate[] {LocalDate.of(2013, Month.OCTOBER, 30), LocalDate.of(2014, Month.SEPTEMBER, 20), LocalDate.of(2015, Month.JUNE, 10),
      LocalDate.of(2016, Month.SEPTEMBER, 5), LocalDate.of(2017, Month.OCTOBER, 1), LocalDate.of(2018, Month.DECEMBER, 30), LocalDate.of(2020, Month.JANUARY, 12),
      LocalDate.of(2023, Month.OCTOBER, 30) };

    final LocalDate effective = LocalDate.of(2013, Month.AUGUST, 1);
    final CDSAnalytic[] cds = CDS_FACTORY.makeCDS(tradeDate, effective, maturities);

    final int n = cds.length;
    final double[] spreads = new double[] {0.01, 0.012, 0.015, 0.02, 0.023, 0.021, 0.02, 0.019 };
    ArgumentChecker.isTrue(n == spreads.length, "spreads wrong length");

    //  final AnalyticCDSPricer pricer = new AnalyticCDSPricer(true);
    final CreditCurveCalibrator calibrator1 = new CreditCurveCalibrator(cds, yieldCurve, MARKIT_FIX, ArbitrageHandling.Ignore);
    final ISDACompliantCreditCurve cc1 = calibrator1.calibrate(spreads);
    final FastCreditCurveBuilder calibrator2 = new FastCreditCurveBuilder(MARKIT_FIX);
    final ISDACompliantCreditCurve cc2 = calibrator2.calibrateCreditCurve(cds, spreads, yieldCurve);
    for (int i = 0; i < n; i++) {
      //   System.out.println(cc1.getZeroRateAtIndex(i) + "\t" + cc2.getZeroRateAtIndex(i));
      assertEquals(cc2.getZeroRateAtIndex(i), cc1.getZeroRateAtIndex(i), 1e-13);
    }

    final int warmups = 200;
    final int hotspots = 1000;

    for (int i = 0; i < warmups; i++) {
      final ISDACompliantCreditCurve cc1a = calibrator1.calibrate(spreads);
      final ISDACompliantCreditCurve cc2a = calibrator2.calibrateCreditCurve(cds, spreads, yieldCurve);
    }

    if (hotspots > 0) {
      long t0 = System.nanoTime();
      for (int i = 0; i < hotspots; i++) {

        final ISDACompliantCreditCurve cc1a = calibrator1.calibrate(spreads);
      }
      long t1 = System.nanoTime();
      System.out.println("time of new calibration: " + (t1 - t0) / hotspots / 1e6 + "ms");

      t0 = System.nanoTime();
      for (int i = 0; i < hotspots; i++) {
        //     final FastCreditCurveBuilder calibrator2a = new FastCreditCurveBuilder(true);
        final ISDACompliantCreditCurve cc2a = calibrator2.calibrateCreditCurve(cds, spreads, yieldCurve);
      }
      t1 = System.nanoTime();
      System.out.println("time of old calibration: " + (t1 - t0) / hotspots / 1e6 + "ms");
    }
  }

}
