/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.analytics.financial.credit.isdastandardmodel.IMMDateLogic.getPrevIMMDate;
import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.Period;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration.CreditCurveCalibrator;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class HedgeRatioCalculatorTest extends ISDABaseTest {
  protected static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  protected static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1, RANDOM);

  private static final CDSAnalyticFactory CDS_FACTORY = new CDSAnalyticFactory(0.4);

  private static final LocalDate TRADE_DATE = LocalDate.of(2013, Month.NOVEMBER, 13);
  private static final LocalDate MATURITY = LocalDate.of(2016, Month.MARCH, 20);
  private static final Period[] TENORS = new Period[] {Period.ofMonths(6), Period.ofYears(1), Period.ofYears(3), Period.ofYears(5), Period.ofYears(7), Period.ofYears(10) };
  private static final HedgeRatioCalculator HEDGE_CAL = new HedgeRatioCalculator();
  private static final CDSAnalytic[] HEDGE_CDS;
  private static final ISDACompliantYieldCurve YIELD_CURVE;
  private static final ISDACompliantCreditCurve CREDIT_CURVE;

  static {
    final LocalDate spotDate = addWorkDays(TRADE_DATE.minusDays(1), 3, DEFAULT_CALENDAR);
    final String[] yieldCurvePoints = new String[] {"1M", "2M", "3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
    final String[] yieldCurveInstruments = new String[] {"M", "M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
    final double[] rates = new double[] {0.00445, 0.009488, 0.012337, 0.017762, 0.01935, 0.020838, 0.01652, 0.02018, 0.023033, 0.02525, 0.02696, 0.02825, 0.02931, 0.03017, 0.03092, 0.0316, 0.03231,
      0.03367, 0.03419, 0.03411, 0.03412 };
    YIELD_CURVE = makeYieldCurve(TRADE_DATE, spotDate, yieldCurvePoints, yieldCurveInstruments, rates, ACT360, D30360, Period.ofYears(1));

    HEDGE_CDS = CDS_FACTORY.makeIMMCDS(TRADE_DATE, TENORS);
    final double[] spreads = new double[] {0.00886315689995649, 0.00886315689995649, 0.0133044689825873, 0.0171490070952563, 0.0183903639181293, 0.0194721890639724 };
    final CreditCurveCalibrator calibrator = new CreditCurveCalibrator(HEDGE_CDS, YIELD_CURVE);
    CREDIT_CURVE = calibrator.calibrate(spreads);
  }

  @Test
  public void test() {
    final LocalDate accStart = FOLLOWING.adjustDate(DEFAULT_CALENDAR, getPrevIMMDate(TRADE_DATE));
    final CDSAnalytic cds = CDS_FACTORY.makeCDS(TRADE_DATE, accStart, MATURITY);
    final double cdsCoupon = 0.01;

    final int n = HEDGE_CDS.length;
    final double[] hedgeCoupons = new double[n];
    Arrays.fill(hedgeCoupons, cdsCoupon);
    final DoubleMatrix1D w = HEDGE_CAL.getHedgeRatios(cds, cdsCoupon, HEDGE_CDS, hedgeCoupons, CREDIT_CURVE, YIELD_CURVE);
    final double[] expected = new double[] {-1.1842173839714448E-6, 0.36244465818986815, 0.6376106050590048, 0.0, 0.0, 0.0 };
    //System.out.println(w);
    //regression test
    for (int i = 0; i < n; i++) {
      assertEquals("", expected[i], w.getEntry(i), 1e-15);
    }

    //value portfolio
    double pv = PRICER.pv(cds, YIELD_CURVE, CREDIT_CURVE, cdsCoupon);
    for (int i = 0; i < n; i++) {
      pv -= w.getEntry(i) * PRICER.pv(HEDGE_CDS[i], YIELD_CURVE, CREDIT_CURVE, hedgeCoupons[i]);
    }
    //  System.out.println("pv: " + pv);

    //perturb the credit curve 
    final double[] t = CREDIT_CURVE.getKnotTimes();
    final double[] h = CREDIT_CURVE.getKnotZeroRates();
    final double sigma = 0.01; //would expect to see up to a 4% shift in hazard rates 
    for (int k = 0; k < 200; k++) {
      final double[] bumpedH = new double[n];
      System.arraycopy(h, 0, bumpedH, 0, n);
      for (int i = 0; i < n; i++) {
        //this may induce an arbitrage - it doesn't matter here 
        bumpedH[i] *= Math.exp(sigma * NORMAL.nextRandom() - sigma * sigma / 2);
      }
      final ISDACompliantCreditCurve cc = new ISDACompliantCreditCurve(t, bumpedH);
      double pvBumped = PRICER.pv(cds, YIELD_CURVE, cc, cdsCoupon);
      for (int i = 0; i < n; i++) {
        pvBumped -= w.getEntry(i) * PRICER.pv(HEDGE_CDS[i], YIELD_CURVE, cc, hedgeCoupons[i]);
      }
      final double change = pvBumped - pv;
      //  System.out.println(change);
      assertTrue(change > 0 && change < 3e-7); //position has positive gamma, so change should always be positive 
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void constHazardRateTest() {
    final ISDACompliantCreditCurve flatCC = new ISDACompliantCreditCurve(5.0, 0.02);
    final LocalDate accStart = FOLLOWING.adjustDate(DEFAULT_CALENDAR, getPrevIMMDate(TRADE_DATE));
    final CDSAnalytic cds = CDS_FACTORY.makeCDS(TRADE_DATE, accStart, MATURITY);
    final double cdsCoupon = 0.01;

    final int n = HEDGE_CDS.length;
    final double[] hedgeCoupons = new double[n];
    Arrays.fill(hedgeCoupons, cdsCoupon);
    final DoubleMatrix1D w = HEDGE_CAL.getHedgeRatios(cds, cdsCoupon, HEDGE_CDS, hedgeCoupons, flatCC, YIELD_CURVE);
    System.out.println(w);
  }

  @Test
  public void lessCDStest() {
    final LocalDate accStart = FOLLOWING.adjustDate(DEFAULT_CALENDAR, getPrevIMMDate(TRADE_DATE));
    final CDSAnalytic cds = CDS_FACTORY.makeCDS(TRADE_DATE, accStart, MATURITY);
    final double cdsCoupon = 0.01;

    final CDSAnalytic[] hedgeCDS = CDS_FACTORY.makeIMMCDS(TRADE_DATE, new Period[] {Period.ofYears(1), Period.ofYears(5) });
    final int n = hedgeCDS.length;
    final double[] hedgeCoupons = new double[n];
    Arrays.fill(hedgeCoupons, cdsCoupon);
    final DoubleMatrix1D w = HEDGE_CAL.getHedgeRatios(cds, cdsCoupon, hedgeCDS, hedgeCoupons, CREDIT_CURVE, YIELD_CURVE);
    //   System.out.println(w);
    final double[] expected = new double[] {0.3877847710928422, 0.026594401620818442 };
    for (int i = 0; i < n; i++) {
      assertEquals("", expected[i], w.getEntry(i), 1e-15);
    }
  }

}
