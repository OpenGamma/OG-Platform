/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityVarianceSwapBackwardsPurePDE;
import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DStandardCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDESolver;
import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDE1DDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEResults1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceConverter;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.local.PureLocalVolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;

/**
 * This test computes the value of options that pays the log of the underlying at expiry using a backwards PDE with a flat local volatility,
 * and compares it to the theoretical value. In the second example a 'realistic' local volatility surface is generated from a mixed log-normal model
 * (which again has a know value for the log-contract). 
 * TODO Move some of this code to a log-payoff local volatility calculator 
 */
public class LogPayoffTest {

  private static final Interpolator1D INTERPOLATOR = Interpolator1DFactory.DOUBLE_QUADRATIC_INSTANCE;
  private static final PDE1DCoefficientsProvider PDE_DATA_PROVIDER = new PDE1DCoefficientsProvider();
  private static final InitialConditionsProvider INT_COND_PROVIDER = new InitialConditionsProvider();
  private static final double EXPIRY = 1.5;
  private static final double FLAT_VOL = 0.3;
  private static final double SPOT = 100.0;
  private static final double DRIFT = 0.1;
  private static final LocalVolatilitySurfaceMoneyness LOCAL_VOL;
  private static final ForwardCurve FORWARD_CURVE;
  private static final ConvectionDiffusionPDE1DCoefficients PDE;
  private static final Function1D<Double, Double> INITIAL_COND;
  // private static final ZZConvectionDiffusionPDEDataBundle PDE_DATA;

  static {
    FORWARD_CURVE = new ForwardCurve(SPOT, DRIFT);
    LOCAL_VOL = new LocalVolatilitySurfaceMoneyness(ConstantDoublesSurface.from(FLAT_VOL), FORWARD_CURVE);
    PDE = PDE_DATA_PROVIDER.getLogBackwardsLocalVol(EXPIRY, LOCAL_VOL);
    INITIAL_COND = INT_COND_PROVIDER.getLogContractPayoffInLogCoordinate();
    // PDE_DATA = PDE_DATA_PROVIDER.getBackwardsLocalVolLogPayoff(EXPIRY, LOCAL_VOL);
  }

  @Test
  public void testFlatSurface() {
    double theta = 0.5;
    double ft = FORWARD_CURVE.getForward(EXPIRY);

    double fL = Math.log(ft / 5.0);
    double fH = Math.log(5.0 * ft);
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(theta, false);

    BoundaryCondition lower = new NeumannBoundaryCondition(1.0, fL, true);
    BoundaryCondition upper = new NeumannBoundaryCondition(1.0, fH, false);

    // MeshingFunction timeMesh = new ExponentialMeshing(0.0, expiry, nTimeNodes, timeMeshLambda);
    final MeshingFunction timeMesh = new ExponentialMeshing(0, EXPIRY, 100, 0.0);
    final MeshingFunction spaceMesh = new ExponentialMeshing(fL, fH, 101, 0.0);

    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(PDE, INITIAL_COND, lower, upper, grid);
    final PDEResults1D res = solver.solve(db);

    final int n = res.getNumberSpaceNodes();
    //    for (int i = 0; i < n; i++) {
    //      System.out.println(res.getSpaceValue(i) + "\t" + res.getFunctionValue(i));
    //    }

    System.out.println("debug " + res.getFunctionValue(n / 2));

    double kVol = Math.sqrt(-2 * (res.getFunctionValue(n / 2) - Math.log(ft)) / EXPIRY);
    //  System.out.println("expected:" + FLAT_VOL + " actual:" + kVol);
    assertEquals(FLAT_VOL, kVol, 1e-6);

    //test the new backwards local vol method for expected variance 
    YieldAndDiscountCurve yieldCurve = new YieldCurve("test", ConstantDoublesCurve.from(DRIFT));
    AffineDividends ad = AffineDividends.noDividends();

    final EquityVarianceSwapBackwardsPurePDE backSolver = new EquityVarianceSwapBackwardsPurePDE();
    final PureLocalVolatilitySurface plv = new PureLocalVolatilitySurface(ConstantDoublesSurface.from(FLAT_VOL));

    double[] res2 = backSolver.expectedVariance(SPOT, yieldCurve, ad, EXPIRY, plv);
    double kVol2 = Math.sqrt(res2[0] / EXPIRY);
    assertEquals(FLAT_VOL, kVol2, 1e-6);
  }

  @Test
  public void testMixedLogNormalVolSurface() {

    final double sigma1 = 0.2;
    final double sigma2 = 0.8;
    final double w = 0.9;

    final Function<Double, Double> surfl = new Function<Double, Double>() {
      ;
      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double k = x[1];
        @SuppressWarnings("synthetic-access")
        final double fwd = FORWARD_CURVE.getForward(t);
        if (t < 1e-9) {
          if (k == fwd) {
            return w * sigma1 + (1 - w) * sigma2;
          } else {
            return sigma2;
          }
        }
        final double dd = w * BlackFormulaRepository.dualGamma(fwd, k, t, sigma1) + (1 - w) * BlackFormulaRepository.dualGamma(fwd, k, t, sigma2);
        if (dd < 1e-100) {
          return sigma2;
        }

        final double theta = w * BlackFormulaRepository.theta(fwd, k, t, sigma1) + (1 - w) * BlackFormulaRepository.theta(fwd, k, t, sigma2);
        return Math.sqrt(-2 * theta / dd / k / k);
      }
    };

    LocalVolatilitySurfaceStrike lv = new LocalVolatilitySurfaceStrike(FunctionalDoublesSurface.from(surfl));
    LocalVolatilitySurfaceMoneyness lvm = LocalVolatilitySurfaceConverter.toMoneynessSurface(lv, FORWARD_CURVE);

    double ft = FORWARD_CURVE.getForward(EXPIRY);
    double theta = 0.5;
    //Review the accuracy is very dependent on these numbers 
    double fL = Math.log(ft / 30);
    double fH = Math.log(30 * ft);

    // PDEUtilityTools.printSurface("lv", lvm.getSurface(), 0.0, 0.5, Math.exp(xL), Math.exp(xH));

    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(theta, false);

    BoundaryCondition lower = new NeumannBoundaryCondition(1.0, fL, true);
    BoundaryCondition upper = new NeumannBoundaryCondition(1.0, fH, false);
    //    BoundaryCondition lower = new FixedSecondDerivativeBoundaryCondition(0.0, xL, true);
    //    BoundaryCondition upper = new FixedSecondDerivativeBoundaryCondition(0.0, xH, false);

    // MeshingFunction timeMesh = new ExponentialMeshing(0.0, expiry, nTimeNodes, timeMeshLambda);
    final MeshingFunction timeMesh = new ExponentialMeshing(0, EXPIRY, 50, 0.0);
    final MeshingFunction spaceMesh = new ExponentialMeshing(fL, fH, 101, 0.0);
    //final MeshingFunction spaceMesh = new HyperbolicMeshing(fL, fH, (fL + fH) / 2, 101, 0.3);

    // ZZConvectionDiffusionPDEDataBundle pde_data = PDE_DATA_PROVIDER.getBackwardsLocalVolLogPayoff(EXPIRY, lvm);
    ConvectionDiffusionPDE1DStandardCoefficients pde = PDE_DATA_PROVIDER.getLogBackwardsLocalVol(EXPIRY, lvm);

    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde, INITIAL_COND, lower, upper, grid);
    final PDEResults1D res = solver.solve(db);

    final int n = res.getNumberSpaceNodes();
    double[] values = new double[n];
    for (int i = 0; i < n; i++) {
      //      System.out.println(res.getSpaceValue(i) + "\t" + res.getFunctionValue(i));
      values[i] = res.getFunctionValue(i);
    }

    final double expected = Math.sqrt(w * sigma1 * sigma1 + (1 - w) * sigma2 * sigma2);

    Interpolator1DDataBundle idb = INTERPOLATOR.getDataBundle(grid.getSpaceNodes(), values);
    double elogS = INTERPOLATOR.interpolate(idb, Math.log(ft));
    double kVol = Math.sqrt(-2 * (elogS - Math.log(ft)) / EXPIRY);
    //  System.out.println("expected:" + expected + " actual:" + kVol);
    assertEquals(expected, kVol, 5e-4); //TODO Improve on 5bps error - local surface is (by construction) very smooth 

  }

}
