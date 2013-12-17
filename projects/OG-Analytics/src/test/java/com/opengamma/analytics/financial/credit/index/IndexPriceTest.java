/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.index;

import static com.opengamma.financial.convention.businessday.BusinessDayDateUtils.addWorkDays;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDABaseTest;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.analytics.financial.credit.isdastandardmodel.fastcalibration.CreditCurveCalibrator;
import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.GaussHermiteQuadratureIntegrator1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.BrentSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.NewtonRaphsonSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class IndexPriceTest extends ISDABaseTest {
  private static final BracketRoot BRACKER = new BracketRoot();
  private static final RealSingleRootFinder ROOTFINDER = new BrentSingleRootFinder();

  protected static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  protected static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1, RANDOM);

  private static final ISDACompliantYieldCurve YIELD_CURVE;
  private static final ISDACompliantCreditCurve CREDIT_CURVE;
  private static final LocalDate TRADE_DATE = LocalDate.of(2013, 11, 29);
  private static final CDSAnalyticFactory FACTORY = new CDSAnalyticFactory();

  static {
    final LocalDate spotDate = addWorkDays(TRADE_DATE.minusDays(1), 3, DEFAULT_CALENDAR);
    final String[] yieldCurvePoints = new String[] {"1M", "2M", "3M", "6M", "9M", "1Y", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y" };
    final String[] yieldCurveInstruments = new String[] {"M", "M", "M", "M", "M", "M", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S", "S" };
    final double[] rates = new double[] {0.00445, 0.009488, 0.012337, 0.017762, 0.01935, 0.020838, 0.01652, 0.02018, 0.023033, 0.02525, 0.02696, 0.02825, 0.02931, 0.03017, 0.03092, 0.0316, 0.03231,
      0.03367, 0.03419, 0.03411, 0.03412 };
    YIELD_CURVE = makeYieldCurve(TRADE_DATE, spotDate, yieldCurvePoints, yieldCurveInstruments, rates, ACT360, D30360, Period.ofYears(1));

    final double[] ccNodes = new double[] {0.5, 1, 3, 5, 7, 10 };
    final double[] ccValues = new double[] {0.01, 0.017, 0.02, 0.018, 0.013, 0.011 };
    CREDIT_CURVE = new ISDACompliantCreditCurve(ccNodes, ccValues);
  }

  @Test
  public void test() {

    final double coupon = 0.01;
    final CDSAnalytic cds = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final double parSpread = PRICER.parSpread(cds, YIELD_CURVE, CREDIT_CURVE);
    final double puf = PRICER.pv(cds, YIELD_CURVE, CREDIT_CURVE, coupon);
    final ISDACompliantCreditCurve flatCC = CREDIT_CURVE_BUILDER.calibrateCreditCurve(cds, coupon, YIELD_CURVE, puf);
    final double quotedSpread = PRICER.parSpread(cds, YIELD_CURVE, flatCC);
    System.out.println("par Spread: " + parSpread * TEN_THOUSAND + ", quoted spread: " + quotedSpread * TEN_THOUSAND + ", flat Hazard rate: " + flatCC.getZeroRateAtIndex(0));

    final double puf2 = PRICER.pv(cds, YIELD_CURVE, flatCC, coupon);
    System.out.println("PUF: " + puf + ", PUF from flat: " + puf2);

    final double prot1 = PRICER.protectionLeg(cds, YIELD_CURVE, CREDIT_CURVE);
    final double prot2 = PRICER.protectionLeg(cds, YIELD_CURVE, flatCC);

    System.out.println(prot1 + "\t" + prot2);

  }

  /**
   * common coupon of 100bps
   */
  @Test
  public void indexTest() {
    final double indexPUF = 0.01;
    final double indexCoupon = 0.01;
    final int nIndex = 125;
    final double sigma = 0.4;

    final CDSAnalytic baseCDS = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final CDSAnalytic[] cds = new CDSAnalytic[nIndex];

    double rrSum = 0;
    for (int i = 0; i < nIndex; i++) {
      final double rr = 0.1 + RANDOM.nextDouble() * 0.8; //recovery between 10 and 90%
      cds[i] = baseCDS.withRecoveryRate(rr);
      rrSum += rr;
    }
    final double indexRR = rrSum / nIndex;
    final CDSAnalytic indexCDS = baseCDS.withRecoveryRate(indexRR);
    final ISDACompliantCreditCurve indexCurve = CREDIT_CURVE_BUILDER.calibrateCreditCurve(indexCDS, indexCoupon, YIELD_CURVE, indexPUF);
    final double indexSpread = PRICER.parSpread(indexCDS, YIELD_CURVE, indexCurve);
    System.out.println("index spread: " + indexSpread);

    final int nKnots = CREDIT_CURVE.getNumberOfKnots();

    final double[] puf = new double[nIndex];
    final double[] parSpread = new double[nIndex];
    final double[] qSpread = new double[nIndex];
    final ISDACompliantCreditCurve[] singleNameCurves = new ISDACompliantCreditCurve[nIndex];
    final ISDACompliantCreditCurve[] singleNameFlatCurves = new ISDACompliantCreditCurve[nIndex];
    double a = 0;
    double b = 0;
    for (int i = 0; i < nIndex; i++) {
      final double[] fwd = new double[nKnots];
      for (int jj = 0; jj < nKnots; jj++) {
        fwd[jj] = 0.02 * Math.exp(sigma * NORMAL.nextRandom() - sigma * sigma / 2);

      }
      singleNameCurves[i] = ISDACompliantCreditCurve.makeFromForwardRates(CREDIT_CURVE.getKnotTimes(), fwd);
      puf[i] = PRICER.pv(cds[i], YIELD_CURVE, singleNameCurves[i], indexCoupon);
      parSpread[i] = PRICER.parSpread(cds[i], YIELD_CURVE, CREDIT_CURVE);
      singleNameFlatCurves[i] = CREDIT_CURVE_BUILDER.calibrateCreditCurve(cds[i], indexCoupon, YIELD_CURVE, puf[i]);
      qSpread[i] = PRICER.parSpread(cds[i], YIELD_CURVE, singleNameFlatCurves[i]);
      final double flatRPV01 = PRICER.annuity(baseCDS, YIELD_CURVE, singleNameFlatCurves[i], PriceType.CLEAN);
      a += qSpread[i] * flatRPV01;
      b += flatRPV01;
      //    System.out.println(parSpread[i] + "\t" + qSpread[i]);
      // final double puf2 = PRICER.pv(cds[i], YIELD_CURVE, singleNameFlatCurves[i], coupon);
      //  System.out.println(puf[i] + "\t" + puf2);
    }
    System.out.println("Flat RPV01 weighted index spread: " + a / b);
    final double indexPrice1 = indexPV(cds, YIELD_CURVE, singleNameCurves, indexCoupon);
    final double indexPrice2 = indexPV(cds, YIELD_CURVE, singleNameFlatCurves, indexCoupon);
    System.out.println("index PV: " + indexPrice1 + "\t" + indexPrice2);

    final ISDACompliantCreditCurve[] adjCurves = adjCurvesForIndex(cds, indexCoupon, YIELD_CURVE, singleNameCurves, indexPUF);
    final double adjPV = indexPV(cds, YIELD_CURVE, adjCurves, indexCoupon);
    System.out.println("adjusted PV: " + adjPV);

    a = 0;
    b = 0;
    for (int i = 0; i < nIndex; i++) {
      final double tSpread = PRICER.parSpread(cds[i], YIELD_CURVE, adjCurves[i]);
      final double rPV01 = PRICER.annuity(baseCDS, YIELD_CURVE, adjCurves[i], PriceType.CLEAN);
      a += tSpread * rPV01;
      b += rPV01;
    }
    System.out.println("Adj RPV01 weighted index spread: " + a / b);

  }

  @Test
  public void fwdCDSTest() {
    final LocalDate fwdDate = LocalDate.of(2014, 6, 19);
    final LocalDate maturity = LocalDate.of(2019, 6, 20);
    final CDSAnalytic fwdCDS = FACTORY.makeCDS(TRADE_DATE, fwdDate, TRADE_DATE, fwdDate, maturity);
    System.out.println(fwdCDS.getAccuredDays());
    final double fwdSpread = PRICER.parSpread(fwdCDS, YIELD_CURVE, CREDIT_CURVE);

    final CDSAnalytic cds1 = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofMonths(6));
    final CDSAnalytic cds2 = FACTORY.makeIMMCDS(TRADE_DATE, Period.of(5, 6, 0));
    final double fwdSpread2 = (PRICER.protectionLeg(cds2, YIELD_CURVE, CREDIT_CURVE) - PRICER.protectionLeg(cds1, YIELD_CURVE, CREDIT_CURVE)) /
        (PRICER.annuity(cds2, YIELD_CURVE, CREDIT_CURVE, PriceType.CLEAN) - PRICER.annuity(cds1, YIELD_CURVE, CREDIT_CURVE, PriceType.CLEAN));
    System.out.println("fwd spread: " + fwdSpread + "\t" + fwdSpread2);
  }

  @Test
  public void payoffTest() {
    final CDSAnalytic cds = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final double coupon = 100 * ONE_BP;
    final double strike = 120 * ONE_BP;
    final Function1D<Double, Double> func = getPayoffFunc(cds, YIELD_CURVE, coupon, strike);
    for (int i = 0; i < 100; i++) {
      final double s = 90. + 1000. * i / 100.;
      final double p = func.evaluate(s * ONE_BP);
      System.out.println(s + "\t" + p);
    }
  }

  @Test
  public void solveForXTest() {
    final CDSAnalytic cds = FACTORY.makeIMMCDS(TRADE_DATE, Period.ofYears(5));
    final double coupon = 100 * ONE_BP;
    final double strike = 120 * ONE_BP;
    final double spread = 130 * ONE_BP;
    final double expH = (spread - coupon) * getRPV01ForSpreadFunction(cds, YIELD_CURVE).evaluate(spread);
    final int warmup = 200;
    final int hotSpot = 200;

    final double x1 = getX(cds, 1.0, expH, YIELD_CURVE, coupon, 0.3);
    final double x2 = getX2(cds, 1.0, expH, YIELD_CURVE, coupon, 0.3);
    assertEquals(x1, x2, 1e-11);
    System.out.println(x1 + "\t" + x2);

    for (int i = 0; i < warmup; i++) {
      final double x = getX2(cds, 1.0, expH, YIELD_CURVE, coupon, 0.3);
    }
    final long t0 = System.nanoTime();
    for (int i = 0; i < hotSpot; i++) {
      final double vol = 0.3 + 0.01 * NORMAL.nextRandom();
      final double x = getX2(cds, 1.0, expH, YIELD_CURVE, coupon, vol);
    }
    final long t1 = System.nanoTime();
    System.out.println((t1 - t0) / hotSpot / 1e6 + "ms");
  }

  public ISDACompliantCreditCurve[] adjCurvesForIndex(final CDSAnalytic[] cds, final double coupon, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve[] creditCurve,
      final double indexPUF) {
    final Function1D<Double, Double> func = getConstHazardRateFunction(cds, coupon, yieldCurve, creditCurve, indexPUF);
    final double[] bracket = BRACKER.getBracketedPoints(func, 0.9, 1.1, 0.0, Double.POSITIVE_INFINITY);
    final double x = ROOTFINDER.getRoot(func, bracket[0], bracket[1]);
    return bumpCurves(creditCurve, x);
  }

  public double indexPV(final CDSAnalytic[] cds, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve[] creditCurves, final double coupon) {
    double protLeg = 0;
    double rpv01 = 0;
    final int n = cds.length;
    for (int i = 0; i < n; i++) {
      protLeg += PRICER.protectionLeg(cds[i], yieldCurve, creditCurves[i]);
      rpv01 += PRICER.annuity(cds[i], yieldCurve, creditCurves[i], PriceType.CLEAN);
    }
    return (protLeg - coupon * rpv01) / n;
  }

  public double getX(final CDSAnalytic cds, final double timeToExpiry, final double expectedH, final ISDACompliantYieldCurve yieldCurve, final double coupon, final double vol) {
    final RungeKuttaIntegrator1D intergrator = new RungeKuttaIntegrator1D();

    final Function1D<Double, Double> func = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        final Function1D<Double, Double> intergrand = getExpIntergrand(cds, timeToExpiry, yieldCurve, coupon, x, vol);
        return intergrator.integrate(intergrand, -6., 10.) - expectedH;
      }
    };

    final double[] bracket = BRACKER.getBracketedPoints(func, 0.8 * coupon, 1.25 * coupon);
    return ROOTFINDER.getRoot(func, bracket[0], bracket[1]);
  }

  public double getX2(final CDSAnalytic cds, final double timeToExpiry, final double expectedH, final ISDACompliantYieldCurve yieldCurve, final double coupon, final double vol) {
    final double rpv01 = getRPV01ForSpreadFunction(cds, yieldCurve).evaluate(coupon);
    final double guess = expectedH / rpv01 + coupon;

    final GaussHermiteQuadratureIntegrator1D intergrator1 = new GaussHermiteQuadratureIntegrator1D(2);
    final GaussHermiteQuadratureIntegrator1D intergrator2 = new GaussHermiteQuadratureIntegrator1D(7);
    final NewtonRaphsonSingleRootFinder nr1 = new NewtonRaphsonSingleRootFinder(1e-8);
    final NewtonRaphsonSingleRootFinder nr2 = new NewtonRaphsonSingleRootFinder(1e-10);

    final DoubleFunction1D func1 = new DoubleFunction1D() {

      @Override
      public Double evaluate(final Double x) {
        final Function1D<Double, Double> intergrand = getGaussHermiteIntergrand(cds, timeToExpiry, yieldCurve, coupon, x, vol);
        return intergrator1.integrateFromPolyFunc(intergrand) - expectedH;
      }
    };

    final DoubleFunction1D func2 = new DoubleFunction1D() {
      @Override
      public final Double evaluate(final Double x) {
        final Function1D<Double, Double> intergrand = getGaussHermiteIntergrand(cds, timeToExpiry, yieldCurve, coupon, x, vol);
        return intergrator2.integrateFromPolyFunc(intergrand) - expectedH;
      }
    };

    //    final double[] bracket = BRACKER.getBracketedPoints(func, 0.8 * coupon, 1.25 * coupon);
    //    return ROOTFINDER.getRoot(func, bracket[0], bracket[1]);
    //   final double g2 = nr1.getRoot(func1, guess);
    return nr2.getRoot(func2, func1.derivative(), guess);
  }

  public Function1D<Double, Double> getExpIntergrand(final CDSAnalytic cds, final double timeToExpiry, final ISDACompliantYieldCurve yieldCurve, final double coupon, final double x, final double vol) {
    final double sigmaRootT = vol * Math.sqrt(timeToExpiry);
    final double sigmaSqrTOver2 = vol * vol * timeToExpiry / 2;
    final Function1D<Double, Double> hFunc = getHFunc(cds, yieldCurve, coupon);
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double z) {
        final double s = x * Math.exp(sigmaRootT * z - sigmaSqrTOver2);
        return hFunc.evaluate(s) * NORMAL.getPDF(z);
      }
    };
  }

  public Function1D<Double, Double> getGaussHermiteIntergrand(final CDSAnalytic cds, final double timeToExpiry, final ISDACompliantYieldCurve yieldCurve, final double coupon, final double x,
      final double vol) {
    final double oneOverRootPi = 1 / Math.sqrt(Math.PI);
    final double sigmaRoot2T = vol * Math.sqrt(2 * timeToExpiry);
    final double sigmaSqrTOver2 = vol * vol * timeToExpiry / 2;
    final Function1D<Double, Double> hFunc = getHFunc(cds, yieldCurve, coupon);
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double z) {
        final double s = x * Math.exp(sigmaRoot2T * z - sigmaSqrTOver2);
        return hFunc.evaluate(s) * oneOverRootPi;
      }
    };
  }

  public Function1D<Double, Double> getConstHazardRateFunction(final CDSAnalytic[] cds, final double coupon, final ISDACompliantYieldCurve yieldCurve, final ISDACompliantCreditCurve[] creditCurve,
      final double indexPUF) {

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        final ISDACompliantCreditCurve[] adjCurves = bumpCurves(creditCurve, x);
        return indexPV(cds, yieldCurve, adjCurves, coupon) - indexPUF;
      }
    };
  }

  private ISDACompliantCreditCurve[] bumpCurves(final ISDACompliantCreditCurve[] creditCurve, final double amount) {
    final int nKnots = creditCurve[0].getNumberOfKnots();
    final int nCurves = creditCurve.length;
    final double[] t = creditCurve[0].getKnotTimes();
    final ISDACompliantCreditCurve[] adjCurves = new ISDACompliantCreditCurve[nCurves];
    for (int jj = 0; jj < nCurves; jj++) {
      final double[] rt = creditCurve[jj].getRt();
      final double[] rtAdj = new double[nKnots];
      for (int i = 0; i < nKnots; i++) {
        rtAdj[i] = rt[i] * amount;
      }
      adjCurves[jj] = ISDACompliantCreditCurve.makeFromRT(t, rtAdj);
    }
    return adjCurves;
  }

  private Function1D<Double, Double> getPayoffFunc(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final double coupon, final double strike) {
    final Function1D<Double, Double> rpv01Func = getRPV01ForSpreadFunction(cds, yieldCurve);
    final double exicisePrice = (strike - coupon) * rpv01Func.evaluate(strike);
    final Function1D<Double, Double> hFunc = getHFunc(cds, yieldCurve, coupon);
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double spread) {
        return Math.max(0, hFunc.evaluate(spread) - exicisePrice);
      }
    };

  }

  private Function1D<Double, Double> getHFunc(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve, final double coupon) {
    final Function1D<Double, Double> rpv01Func = getRPV01ForSpreadFunction(cds, yieldCurve);
    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double spread) {
        return (spread - coupon) * rpv01Func.evaluate(spread);
      }
    };
  }

  private Function1D<Double, Double> getRPV01ForSpreadFunction(final CDSAnalytic cds, final ISDACompliantYieldCurve yieldCurve) {
    final CreditCurveCalibrator calibrator = new CreditCurveCalibrator(new CDSAnalytic[] {cds }, yieldCurve);
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double spread) {
        final ISDACompliantCreditCurve cc = calibrator.calibrate(new double[] {spread });
        return PRICER.annuity(cds, yieldCurve, cc, PriceType.CLEAN);
      }
    };

  }

}
