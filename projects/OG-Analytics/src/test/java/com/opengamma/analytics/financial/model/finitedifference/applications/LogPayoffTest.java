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
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDE1DDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEResults1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceConverter;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.financial.model.volatility.local.PureLocalVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.smile.function.MultiHorizonMixedLogNormalModelData;
import com.opengamma.analytics.financial.model.volatility.surface.MixedLogNormalVolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;

/**
 * This test computes the value of options that pays the log of the underlying at expiry using a backwards PDE with a flat local volatility,
 * and compares it to the theoretical value. In the second example a 'realistic' local volatility surface is generated from a mixed log-normal model
 * (which again has a know value for the log-contract).
 * TODO Move some of this code to a log-payoff local volatility calculator
 *
 */
@Test(groups = TestGroup.UNIT)
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
    final double theta = 0.5;
    final double ft = FORWARD_CURVE.getForward(EXPIRY);

    final double fL = Math.log(ft / 5.0);
    final double fH = Math.log(5.0 * ft);
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(theta, false);

    final BoundaryCondition lower = new NeumannBoundaryCondition(1.0, fL, true);
    final BoundaryCondition upper = new NeumannBoundaryCondition(1.0, fH, false);

    // MeshingFunction timeMesh = new ExponentialMeshing(0.0, expiry, nTimeNodes, timeMeshLambda);
    final MeshingFunction timeMesh = new ExponentialMeshing(0, EXPIRY, 100, 0.0);
    final MeshingFunction spaceMesh = new ExponentialMeshing(fL, fH, 101, 0.0);

    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<>(PDE, INITIAL_COND, lower, upper, grid);
    final PDEResults1D res = solver.solve(db);

    final int n = res.getNumberSpaceNodes();
    //    for (int i = 0; i < n; i++) {
    //      System.out.println(res.getSpaceValue(i) + "\t" + res.getFunctionValue(i));
    //    }

    //System.out.println("debug " + res.getFunctionValue(n / 2));

    final double kVol = Math.sqrt(-2 * (res.getFunctionValue(n / 2) - Math.log(ft)) / EXPIRY);
    //  System.out.println("expected:" + FLAT_VOL + " actual:" + kVol);
    assertEquals(FLAT_VOL, kVol, 1e-6);

    //test the new backwards local vol method for expected variance
    final YieldAndDiscountCurve yieldCurve = new YieldCurve("test", ConstantDoublesCurve.from(DRIFT));
    final AffineDividends ad = AffineDividends.noDividends();

    final EquityVarianceSwapBackwardsPurePDE backSolver = new EquityVarianceSwapBackwardsPurePDE();
    final PureLocalVolatilitySurface plv = new PureLocalVolatilitySurface(ConstantDoublesSurface.from(FLAT_VOL));

    final double[] res2 = backSolver.expectedVariance(SPOT, yieldCurve, ad, EXPIRY, plv);
    final double kVol2 = Math.sqrt(res2[0] / EXPIRY);
    assertEquals(FLAT_VOL, kVol2, 1e-6);
  }

  @Test
  public void testMixedLogNormalVolSurface() {

    final double[] weights = new double[] {0.9, 0.1 };
    final double[] sigmas = new double[] {0.2, 0.8 };
    final MultiHorizonMixedLogNormalModelData data = new MultiHorizonMixedLogNormalModelData(weights, sigmas);
    final LocalVolatilitySurfaceStrike lv = MixedLogNormalVolatilitySurface.getLocalVolatilitySurface(FORWARD_CURVE, data);
    final LocalVolatilitySurfaceMoneyness lvm = LocalVolatilitySurfaceConverter.toMoneynessSurface(lv, FORWARD_CURVE);
    final double expected = Math.sqrt(weights[0] * sigmas[0] * sigmas[0] + weights[1] * sigmas[1] * sigmas[1]);

    final double ft = FORWARD_CURVE.getForward(EXPIRY);
    final double theta = 0.5;
    //Review the accuracy is very dependent on these numbers
    final double fL = Math.log(ft / 30);
    final double fH = Math.log(30 * ft);

    // PDEUtilityTools.printSurface("lv", lvm.getSurface(), 0.0, 2e-9, 0.9999, 1.0001);

    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(theta, false);

    final BoundaryCondition lower = new NeumannBoundaryCondition(1.0, fL, true);
    final BoundaryCondition upper = new NeumannBoundaryCondition(1.0, fH, false);
    //    BoundaryCondition lower = new FixedSecondDerivativeBoundaryCondition(0.0, xL, true);
    //    BoundaryCondition upper = new FixedSecondDerivativeBoundaryCondition(0.0, xH, false);

    // MeshingFunction timeMesh = new ExponentialMeshing(0.0, expiry, nTimeNodes, timeMeshLambda);
    final MeshingFunction timeMesh = new ExponentialMeshing(0, EXPIRY, 50, 0.0);
    //  final MeshingFunction spaceMesh = new ExponentialMeshing(fL, fH, 101, 0.0);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(fL, fH, (fL + fH) / 2, 101, 0.4);

    // ZZConvectionDiffusionPDEDataBundle pde_data = PDE_DATA_PROVIDER.getBackwardsLocalVolLogPayoff(EXPIRY, lvm);
    final ConvectionDiffusionPDE1DStandardCoefficients pde = PDE_DATA_PROVIDER.getLogBackwardsLocalVol(EXPIRY, lvm);

    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde, INITIAL_COND, lower, upper, grid);
    final PDEResults1D res = solver.solve(db);

    final int n = res.getNumberSpaceNodes();
    final double[] values = new double[n];
    for (int i = 0; i < n; i++) {
      //      System.out.println(res.getSpaceValue(i) + "\t" + res.getFunctionValue(i));
      values[i] = res.getFunctionValue(i);
    }

    final Interpolator1DDataBundle idb = INTERPOLATOR.getDataBundle(grid.getSpaceNodes(), values);
    final double elogS = INTERPOLATOR.interpolate(idb, Math.log(ft));
    final double kVol = Math.sqrt(-2 * (elogS - Math.log(ft)) / EXPIRY);
    //  System.out.println("expected:" + expected + " actual:" + kVol);
    assertEquals(expected, kVol, 1e-3); //TODO Improve on 10bps error - local surface is (by construction) very smooth. NOTE: this has got worse since we improved the T -> 0
    //behaviour of the mixed log-normal local volatility surface
  }

}
