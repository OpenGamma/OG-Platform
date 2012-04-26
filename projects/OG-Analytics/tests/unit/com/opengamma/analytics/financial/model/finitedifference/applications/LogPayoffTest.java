/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDEDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDESolver;
import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEResults1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceConverter;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;

/**
 * 
 */
public class LogPayoffTest {

  private static final PDEDataBundleProvider PDE_DATA_PROVIDER = new PDEDataBundleProvider();
  private static final double EXPIRY = 1.5;
  private static final double FLAT_VOL = 0.3;
  private static final double SPOT = 100.0;
  private static final double DRIFT = 0.1;
  private static final LocalVolatilitySurfaceMoneyness LOCAL_VOL;
  private static final ForwardCurve FORWARD_CURVE;
  private static final ConvectionDiffusionPDEDataBundle PDE_DATA;

  static {
    FORWARD_CURVE = new ForwardCurve(SPOT, DRIFT);
    LOCAL_VOL = new LocalVolatilitySurfaceMoneyness(ConstantDoublesSurface.from(FLAT_VOL), FORWARD_CURVE);
    PDE_DATA = PDE_DATA_PROVIDER.getBackwardsLocalVolLogPayoff(EXPIRY, LOCAL_VOL);
  }

  @Test
  public void testFlatSurface() {
    double theta = 0.5;
    double xL = -0.5;
    double xH = 0.5;
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(theta, false);

    BoundaryCondition lower = new NeumannBoundaryCondition(1.0, xL, true);
    BoundaryCondition upper = new NeumannBoundaryCondition(1.0, xH, false);

    // MeshingFunction timeMesh = new ExponentialMeshing(0.0, expiry, nTimeNodes, timeMeshLambda);
    final MeshingFunction timeMesh = new ExponentialMeshing(0, EXPIRY, 100, 0.0);
    final MeshingFunction spaceMesh = new ExponentialMeshing(xL, xH, 101, 0.0);

    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final PDEResults1D res = solver.solve(PDE_DATA, grid, lower, upper);

    final int n = res.getNumberSpaceNodes();
    //    for (int i = 0; i < n; i++) {
    //      System.out.println(res.getSpaceValue(i) + "\t" + res.getFunctionValue(i));
    //    }

    double kVol = Math.sqrt(-2 * res.getFunctionValue(n / 2) / EXPIRY);
    //  System.out.println("expected:" + FLAT_VOL + " actual:" + kVol);
    assertEquals(FLAT_VOL, kVol, 1e-6);
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

    double theta = 0.5;
    double xL = -1.0;
    double xH = 1.0;

    // PDEUtilityTools.printSurface("lv", lvm.getSurface(), 0.0, 0.5, Math.exp(xL), Math.exp(xH));

    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(theta, false);

    BoundaryCondition lower = new NeumannBoundaryCondition(1.0, xL, true);
    BoundaryCondition upper = new NeumannBoundaryCondition(1.0, xH, false);
    //    BoundaryCondition lower = new FixedSecondDerivativeBoundaryCondition(0.0, xL, true);
    //    BoundaryCondition upper = new FixedSecondDerivativeBoundaryCondition(0.0, xH, false);

    // MeshingFunction timeMesh = new ExponentialMeshing(0.0, expiry, nTimeNodes, timeMeshLambda);
    final MeshingFunction timeMesh = new ExponentialMeshing(0, EXPIRY, 100, 0.0);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(xL, xH, 0.0, 101, 0.3);

    ConvectionDiffusionPDEDataBundle pde_data = PDE_DATA_PROVIDER.getBackwardsLocalVolLogPayoff(EXPIRY, lvm);

    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final PDEResults1D res = solver.solve(pde_data, grid, lower, upper);

    final int n = res.getNumberSpaceNodes();
    //    for (int i = 0; i < n; i++) {
    //      System.out.println(res.getSpaceValue(i) + "\t" + res.getFunctionValue(i));
    //    }

    final double expected = Math.sqrt(w * sigma1 * sigma1 + (1 - w) * sigma2 * sigma2);
    double kVol = Math.sqrt(-2 * res.getFunctionValue(n / 2) / EXPIRY);
    //  System.out.println("expected:" + expected + " actual:" + kVol);
    assertEquals(expected, kVol, 5e-4); //TODO Improve on 5bps error - local surface is (by construction) very smooth 
  }
}
