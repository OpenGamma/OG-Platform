/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityDividendsCurvesBundle;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityVarianceSwapBackwardsPurePDE;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityVarianceSwapStaticReplication;
import com.opengamma.analytics.financial.equity.variance.pricing.VolatilitySurfaceConverter;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDEUtilityTools;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.local.DupireLocalVolatilityCalculator;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.local.PureLocalVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.smile.function.MultiHorizonMixedLogNormalModelData;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;

/**
 * 
 */
public class MixedLogNormalVolatilitySurfaceTest {

  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);

  @Test(enabled = false)
  public void printTest() {
    final double t = 1.5;
    final double spot = 100.0;
    final double r = 0.05;
    final ForwardCurve fc = new ForwardCurve(spot, r);
    final YieldAndDiscountCurve discountCurve = new YieldCurve("test", ConstantDoublesCurve.from(r));
    double[] w = new double[] {0.7, 0.25, 0.05 };
    double[] sigma = new double[] {0.3, 0.6, 1.0 };
    double[] mu = new double[] {0.0, 0.3, -0.5 };
    MultiHorizonMixedLogNormalModelData data = new MultiHorizonMixedLogNormalModelData(w, sigma, mu);
    BlackVolatilitySurfaceStrike ivs = MixedLogNormalVolatilitySurface.getImpliedVolatilitySurface(fc, data);
    PDEUtilityTools.printSurface("imp vol", ivs.getSurface(), 0.01, 2.0, spot * 0.1, spot * 3.0);

    LocalVolatilitySurfaceStrike lvs = MixedLogNormalVolatilitySurface.getLocalVolatilitySurface(fc, data);
    PDEUtilityTools.printSurface("local vol", lvs.getSurface(), 0.00, 2.0, spot * 0.1, spot * 3.0);

    DupireLocalVolatilityCalculator cal = new DupireLocalVolatilityCalculator();
    LocalVolatilitySurfaceStrike lv2 = cal.getLocalVolatility(ivs, fc);
    //    PDEUtilityTools.printSurface("local vo2l", lv2.getSurface(), 0.01, 2.0, spot * 0.3, spot * 3.0);

    System.out.println("lv: " + lvs.getVolatility(2.0, 1.7 * spot));
    System.out.println("lv2: " + lv2.getVolatility(2.0, 1.7 * spot));

    EquityVarianceSwapStaticReplication vsPricer = new EquityVarianceSwapStaticReplication();
    double[] res = vsPricer.expectedVariance(spot, discountCurve, AffineDividends.noDividends(), t, ivs);
    System.out.println(Math.sqrt(res[0] / t));

    double sum1 = 0.0;
    double sum2 = 0.0;
    for (int i = 0; i < 3; i++) {
      sum1 += w[i] * Math.exp(mu[i] * t);
      sum2 += w[i] * t * (mu[i] - sigma[i] * sigma[i] / 2);
    }
    double temp = Math.sqrt(2 * (Math.log(sum1) - sum2) / t);
    System.out.println("expected: " + temp);

    PureLocalVolatilitySurface plv = VolatilitySurfaceConverter.convertLocalVolSurface(lv2, new EquityDividendsCurvesBundle(spot, discountCurve, AffineDividends.noDividends()));
    EquityVarianceSwapBackwardsPurePDE backCal = new EquityVarianceSwapBackwardsPurePDE();
    res = backCal.expectedVariance(spot, discountCurve, AffineDividends.noDividends(), t, plv);
    System.out.println(Math.sqrt(res[0] / t));

    double vol = lvs.getVolatility(0.0, 25.0);
    System.out.println(vol);
  }

  @Test
  public void flatTest() {
    final double spot = 123.0;
    final double r = 0.05;
    final ForwardCurve fc = new ForwardCurve(spot, r);
    final double vol = 0.3;
    double[] w = new double[] {0.6, 0.4 };
    double[] sigma = new double[] {vol, vol };
    MultiHorizonMixedLogNormalModelData data = new MultiHorizonMixedLogNormalModelData(w, sigma);
    BlackVolatilitySurfaceStrike ivs = MixedLogNormalVolatilitySurface.getImpliedVolatilitySurface(fc, data);
    LocalVolatilitySurfaceStrike lvs = MixedLogNormalVolatilitySurface.getLocalVolatilitySurface(fc, data);
    for (int i = 0; i < 100; i++) {
      double t = 5.0 * RANDOM.nextDouble();
      //strikes in range +- 8-sigma 
      double a = 16 * (RANDOM.nextDouble() - 0.5);
      double k = spot * Math.exp((r - vol * vol / 2) * t + Math.sqrt(t) * vol * a);
      double iv = ivs.getVolatility(t, k);
      double lv = lvs.getVolatility(t, k);
      assertEquals("implied volatility t=" + t + ", k=" + k, vol, iv, 1e-14);
      assertEquals("local volatilityt=" + t + ", k=" + k, vol, lv, 1e-14);
    }
  }

  @Test
  public void nonflatTest1() {
    DupireLocalVolatilityCalculator dupire = new DupireLocalVolatilityCalculator();
    final double spot = 0.03;
    final double r = 0.05;
    final ForwardCurve fc = new ForwardCurve(spot, r);
    final double vol = 0.3;
    double[] w = new double[] {0.8, 0.2 };
    double[] sigma = new double[] {0.2, 0.7 };
    MultiHorizonMixedLogNormalModelData data = new MultiHorizonMixedLogNormalModelData(w, sigma);
    BlackVolatilitySurfaceStrike ivs = MixedLogNormalVolatilitySurface.getImpliedVolatilitySurface(fc, data);
    LocalVolatilitySurfaceStrike lvs = MixedLogNormalVolatilitySurface.getLocalVolatilitySurface(fc, data);
    LocalVolatilitySurfaceStrike lvs2 = dupire.getLocalVolatility(ivs, fc);

    for (int i = 0; i < 100; i++) {
      double t = 5.0 * RANDOM.nextDouble();
      //strikes in range +- 8-sigma 
      double a = 16 * (RANDOM.nextDouble() - 0.5);
      double k = spot * Math.exp((r - vol * vol / 2) * t + Math.sqrt(t) * vol * a);
      double lv = lvs.getVolatility(t, k);
      double lv2 = lvs2.getVolatility(t, k);
      assertEquals("Local volatility t=" + t + ", k=" + k, lv, lv2, 1e-3); //loss a lot of accuracy going via Dupire formula (since this used finite difference on the implied vol surface)
    }
  }

  @Test
  public void nonflatTest2() {
    DupireLocalVolatilityCalculator dupire = new DupireLocalVolatilityCalculator();
    final double spot = 0.03;
    final double r = 0.10;
    final ForwardCurve fc = new ForwardCurve(spot, r);
    final double vol = 0.3;
    double[] w = new double[] {0.9, 0.1 };
    double[] sigma = new double[] {0.2, 0.7 };
    double[] mu = new double[] {0.1, -0.1 };

    MultiHorizonMixedLogNormalModelData data = new MultiHorizonMixedLogNormalModelData(w, sigma, mu);
    BlackVolatilitySurfaceStrike ivs = MixedLogNormalVolatilitySurface.getImpliedVolatilitySurface(fc, data);
    LocalVolatilitySurfaceStrike lvs = MixedLogNormalVolatilitySurface.getLocalVolatilitySurface(fc, data);
    LocalVolatilitySurfaceStrike lvs2 = dupire.getLocalVolatility(ivs, fc);

    for (int i = 0; i < 100; i++) {
      double t = 5.0 * RANDOM.nextDouble();
      //strikes in range +- 8-sigma 
      double a = 16 * (RANDOM.nextDouble() - 0.5);
      double k = spot * Math.exp((r - vol * vol / 2) * t + Math.sqrt(t) * vol * a);
      double lv = lvs.getVolatility(t, k);
      double lv2 = lvs2.getVolatility(t, k);
      assertEquals("Local volatility t=" + t + ", k=" + k, lv, lv2, 1e-3); //loss a lot of accuracy going via Dupire formula (since this used finite difference on the implied vol surface)
    }

    //lvs.getVolatility(3.86, 0.177);

    //    PDEUtilityTools.printSurface("implied vol", ivs.getSurface(), 0.01, 4.0, 0.1 * spot, 10.0 * spot);
    //  PDEUtilityTools.printSurface("local vol", lvs.getSurface(), 0.01, 5.0, 0.1 * spot, 10.0 * spot);
    //    PDEUtilityTools.printSurface("local vo2l", lvs2.getSurface(), 0.01, 4.0, 0.1 * spot, 10.0 * spot);

  }

}
