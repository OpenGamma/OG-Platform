/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import java.util.Arrays;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DStandardCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDE1DDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEResults1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.ArgumentChecker;

/**
 * @deprecated Use LocalVolatilityBackwardsPDEPricer
 */
@Deprecated
public class LocalVolPDEPricer {

  private static final InitialConditionsProvider ICP = new InitialConditionsProvider();
  private static final PDE1DCoefficientsProvider PDE = new PDE1DCoefficientsProvider();
  private static final ThetaMethodFiniteDifference INITIAL_SOLVER = new ThetaMethodFiniteDifference(1.0, false);
  private static final ThetaMethodFiniteDifference SOLVER = new ThetaMethodFiniteDifference();
  /*
   * Crank-Nicolson (i.e. theta = 0.5) is known to give poor results around at-the-money. This can be solved by using a short fully implicit (theta = 1.0) burn-in period.
   * Eigenvalues associated with the discontinuity in the first derivative are not damped out when theta = 0.5, but are for theta = 1.0 - the time step for this phase should be
   * such that the Crank-Nicolson (order(dt^2)) accuracy is not destroyed.
   */
  private static final boolean USE_BURNIN = true;
  private static final double BURNIN_FRACTION = 0.20;

  public double price(final double s0, final double k, final double r, final double b, final double t, final LocalVolatilitySurfaceStrike locVol, final boolean isCall, final boolean isAmerican,
      final int spaceNodes, final int timeNodes) {

    final double q = r - b;
    // final double s0 = fwdCurve.getSpot();
    final double sigma0 = locVol.getVolatility(t, k);
    final double mult = Math.exp(6.0 * sigma0 * Math.sqrt(t));
    final double sMin = Math.min(0.8 * k, s0 / mult);
    final double sMax = Math.max(1.25 * k, s0 * mult);

    final int tBurnNodes = (int) (USE_BURNIN ? Math.max(2, timeNodes * BURNIN_FRACTION) : 0);
    final double tBurn = USE_BURNIN ? BURNIN_FRACTION * t * t / timeNodes : 0.0;

    // set up a near-uniform mesh that includes spot and strike
    MeshingFunction xMesh = new ExponentialMeshing(sMin, sMax, spaceNodes, 0.0, new double[] {s0, k});
    MeshingFunction tMeshBurn = USE_BURNIN ? new ExponentialMeshing(0.0, tBurn, tBurnNodes, 0.0) : null;
    MeshingFunction tMesh = new ExponentialMeshing(tBurn, t, timeNodes - tBurnNodes, 0.0);
    PDEGrid1D gridBurn = USE_BURNIN ? new PDEGrid1D(tMeshBurn, xMesh) : null;
    PDEGrid1D grid = new PDEGrid1D(tMesh, xMesh);
    final int index = Arrays.binarySearch(grid.getSpaceNodes(), s0);
    ArgumentChecker.isTrue(index >= 0, "cannot find spot on grid");

    ConvectionDiffusionPDE1DStandardCoefficients coef = PDE.getBackwardsLocalVol(r, q, t, locVol);
    Function1D<Double, Double> payoff = ICP.getEuropeanPayoff(k, isCall);

    BoundaryCondition lower;
    BoundaryCondition upper;

    PDEResults1D res;

    if (isAmerican) {
      if (isCall) {
        lower = new NeumannBoundaryCondition(0.0, sMin, true);
        upper = new NeumannBoundaryCondition(1.0, sMax, false);
      } else {
        lower = new NeumannBoundaryCondition(-1.0, sMin, true);
        upper = new NeumannBoundaryCondition(0.0, sMax, false);
      }

      final Function<Double, Double> func = new Function<Double, Double>() {
        @Override
        public Double evaluate(Double... tx) {
          final double x = tx[1];
          return isCall ? Math.max(x - k, 0.0) : Math.max(k - x, 0);
        }
      };

      final FunctionalDoublesSurface free = new FunctionalDoublesSurface(func);
      if (USE_BURNIN) {
        PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> dataBurn = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(coef, payoff, lower, upper, free, gridBurn);
        PDEResults1D resBurn = INITIAL_SOLVER.solve(dataBurn);

        PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> data = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(coef, resBurn.getTerminalResults(), lower, upper, free, grid);
        res = SOLVER.solve(data);
      } else {
        PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> data = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(coef, payoff, lower, upper, free, grid);
        res = SOLVER.solve(data);
      }

    } else {
      if (isCall) {
        lower = new NeumannBoundaryCondition(0.0, sMin, true);
        Function1D<Double, Double> upFunc = new Function1D<Double, Double>() {
          @Override
          public Double evaluate(Double time) {
            return Math.exp(-q * time);
          }
        };
        upper = new NeumannBoundaryCondition(upFunc, sMax, false);
      } else {
        Function1D<Double, Double> downFunc = new Function1D<Double, Double>() {
          @Override
          public Double evaluate(Double time) {
            return -Math.exp(-q * time);
          }
        };
        lower = new NeumannBoundaryCondition(downFunc, sMin, true);
        upper = new NeumannBoundaryCondition(0.0, sMax, false);
      }
      if (USE_BURNIN) {
        PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> dataBurn = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(coef, payoff, lower, upper, gridBurn);
        PDEResults1D resBurn = INITIAL_SOLVER.solve(dataBurn);

        PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> data = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(coef, resBurn.getTerminalResults(), lower, upper, grid);
        res = SOLVER.solve(data);
      } else {
        PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> data = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(coef, payoff, lower, upper, grid);
        res = SOLVER.solve(data);
      }
    }

    return res.getFunctionValue(index);
  }

}
