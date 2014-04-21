/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Map;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityDividendsCurvesBundle;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityVarianceSwapBackwardsPurePDE;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityVarianceSwapStaticReplication;
import com.opengamma.analytics.financial.equity.variance.pricing.VolatilitySurfaceConverter;
import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DStandardCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDESolver;
import com.opengamma.analytics.financial.model.finitedifference.DoubleExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDE1DDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDETerminalResults1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.financial.model.finitedifference.applications.InitialConditionsProvider;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDE1DCoefficientsProvider;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDEUtilityTools;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.local.DupireLocalVolatilityCalculator;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceConverter;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.local.PureLocalVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SurfaceArrayUtils;
import com.opengamma.analytics.financial.model.volatility.smile.function.MultiHorizonMixedLogNormalModelData;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MixedLogNormalVolatilitySurfaceTest {

  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);

  @Test(enabled = false)
  public void printTest() {
    System.out.println("MixedLogNormalVolatilitySurfaceTest. printTest");
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

  /**
   * Test the pricing of a 1D option with the local volatility surface produced by the mixed log-normal model solving both the forward and backwards PDE, and compare 
   * with the result from the implied volatility surface. Neither solving the forward nor the backwards PDE give great accuracy
   */
  @Test
  public void shortDatedOptionTest() {
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.5, false);
    final PDE1DCoefficientsProvider pdeProvider = new PDE1DCoefficientsProvider();
    final InitialConditionsProvider initialConProvider = new InitialConditionsProvider();

    //set up the mixed log-normal model 
    final double[] weights = new double[] {0.1, 0.8, 0.1 };
    final double[] sigmas = new double[] {0.15, 0.5, 0.9 };
    final double[] mus = new double[] {-0.1, 0, 0.1 };

    final MultiHorizonMixedLogNormalModelData mlnData = new MultiHorizonMixedLogNormalModelData(weights, sigmas, mus);
    final double spot = 100.;
    final double r = 0.1;
    final ForwardCurve fwdCurve = new ForwardCurve(spot, r);

    final double t = 1.0 / 365.;
    final double rootT = Math.sqrt(t);
    final double fwd = fwdCurve.getForward(t);

    BlackVolatilitySurfaceStrike ivs = MixedLogNormalVolatilitySurface.getImpliedVolatilitySurface(fwdCurve, mlnData);
    LocalVolatilitySurfaceStrike lvs = MixedLogNormalVolatilitySurface.getLocalVolatilitySurface(fwdCurve, mlnData);
    LocalVolatilitySurfaceMoneyness lvsm = LocalVolatilitySurfaceConverter.toMoneynessSurface(lvs, fwdCurve);

    //    PDEUtilityTools.printSurface("imp vol", ivs.getSurface(), 0, t, 0.9, 1.1);
    //    DupireLocalVolatilityCalculator dupire = new DupireLocalVolatilityCalculator();
    //    LocalVolatilitySurfaceMoneyness dlv = dupire.getLocalVolatility(BlackVolatilitySurfaceConverter.toMoneynessSurface(ivs, fwdCurve));
    //    PDEUtilityTools.printSurface("local vol (dupire)", dlv.getSurface(), 0, t, 0.99, 1.01);
    //    PDEUtilityTools.printSurface("local vol", lvsm.getSurface(), 0, t, 0.99, 1.01);

    //set up for solving the forward PDE 
    //TODO shunt this setup into its own class 
    ConvectionDiffusionPDE1DStandardCoefficients pde = pdeProvider.getForwardLocalVol(lvsm);
    Function1D<Double, Double> initialCond = initialConProvider.getForwardCallPut(true);
    double xL = 0.8;
    double xH = 1.2;
    BoundaryCondition lower = new NeumannBoundaryCondition(-1.0, xL, true);
    BoundaryCondition upper = new NeumannBoundaryCondition(0.0, xH, false);
    final MeshingFunction spaceMeshF = new HyperbolicMeshing(xL, xH, 1.0, 200, 0.001);
    final MeshingFunction timeMeshF = new ExponentialMeshing(0, t, 50, 4.0);
    final MeshingFunction timeMeshB = new DoubleExponentialMeshing(0, t, t / 2, 50, 2.0, -4.0);
    final PDEGrid1D grid = new PDEGrid1D(timeMeshF, spaceMeshF);
    PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> dbF = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde, initialCond, lower, upper, grid);
    PDETerminalResults1D res = (PDETerminalResults1D) solver.solve(dbF);
    final double minK = Math.exp(-6 * rootT);
    final double maxK = Math.exp(6 * rootT);
    Map<Double, Double> vols = PDEUtilityTools.priceToImpliedVol(fwdCurve, t, res, minK, maxK, true);
    DoubleQuadraticInterpolator1D interpolator = Interpolator1DFactory.DOUBLE_QUADRATIC_INSTANCE;
    Interpolator1DDataBundle idb = interpolator.getDataBundle(vols);

    //set up for solving backwards PDE 
    ConvectionDiffusionPDE1DStandardCoefficients pdeB = pdeProvider.getBackwardsLocalVol(t, lvsm);
    double sL = xL * spot;
    double sH = xH * spot;
    final MeshingFunction spaceMeshB = new HyperbolicMeshing(sL, sH, spot, 200, 0.001);
    final PDEGrid1D gridB = new PDEGrid1D(timeMeshB, spaceMeshB);
    int index = SurfaceArrayUtils.getLowerBoundIndex(gridB.getSpaceNodes(), spot);
    double s1 = gridB.getSpaceNode(index);
    double s2 = gridB.getSpaceNode(index + 1);
    final double w = (s2 - spot) / (s2 - s1);

    //solve a separate backwards PDE for each strike
    for (int i = 0; i < 10; i++) {
      double z = -5 + i;
      double k = spot * Math.exp(0.4 * rootT * z);
      double x = k / fwd;
      double vol = ivs.getVolatility(t, k);
      double volFPDE = interpolator.interpolate(idb, x);

      boolean isCall = (k >= fwd);
      BoundaryCondition lowerB = new NeumannBoundaryCondition(isCall ? 0 : -1, sL, true);
      BoundaryCondition upperB = new NeumannBoundaryCondition(isCall ? 1 : 0, sH, false);

      Function1D<Double, Double> bkdIC = initialConProvider.getEuropeanPayoff(k, isCall);
      PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> dbB = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pdeB, bkdIC, lowerB, upperB, gridB);
      PDEResults1D resB = solver.solve(dbB);
      double price1 = resB.getFunctionValue(index);
      double price2 = resB.getFunctionValue(index + 1);
      double vol1 = BlackFormulaRepository.impliedVolatility(price1, s1, k, t, isCall);
      double vol2 = BlackFormulaRepository.impliedVolatility(price2, s2, k, t, isCall);
      double volBPDE = w * vol1 + (1 - w) * vol2;

      //      double price = BlackFormulaRepository.price(fwd, k, t, vol, isCall);
      //  System.out.println(x + "\t" + vol + "\t" + volFPDE + "\t" + volBPDE);
      assertEquals("forward PDE " + x, vol, volFPDE, 4e-3); //40bps error
      assertEquals("backwards PDE " + x, vol, volBPDE, 5e-3); //50bps error TODO - why is this so bad 
    }

  }
}
