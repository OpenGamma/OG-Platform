/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import java.util.Arrays;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DStandardCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDE1DDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEResults1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceStrike;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.ArgumentChecker;

/**
 * Sets up a PDE solver to solve the Black-Scholes-Merton PDE for the price of a European or American option on a commodity using a (near) uniform grid.
 * This code should be view as an example of how to setup the PDE solver.
 */
// TODO there is a lot of shared code with BlackScholesMertonPDEPricer
public class LocalVolatilityBackwardsPDEPricer {

  private static final InitialConditionsProvider ICP = new InitialConditionsProvider();
  private static final PDE1DCoefficientsProvider PDE = new PDE1DCoefficientsProvider();
  /*
   * Crank-Nicolson (i.e. theta = 0.5) is known to give poor results around at-the-money. This can be solved by using a short fully implicit (theta = 1.0) burn-in period.
   * Eigenvalues associated with the discontinuity in the first derivative are not damped out when theta = 0.5, but are for theta = 1.0 - the time step for this phase should be
   * such that the Crank-Nicolson (order(dt^2)) accuracy is not destroyed.
   */
  private static final boolean USE_BURNIN = true;
  private static final double BURNIN_FRACTION = 0.20;
  private static final double BURNIN_THETA = 1.0;
  private static final double MAIN_RUN_THETA = 0.5;

  private final boolean _useBurnin;
  private final double _burninFrac;
  private final double _burninTheta;
  private final double _mainRunTheta;

  /**
   * Finite difference PDE solver that uses a 'burn-in' period that consumes 20% of the time nodes (and hence the compute time) and runs with a theta of 1.0.
   * <b>Note</b> These setting are ignored if user supplies own grids and thetas.
   */
  public LocalVolatilityBackwardsPDEPricer() {
    _useBurnin = USE_BURNIN;
    _burninFrac = BURNIN_FRACTION;
    _burninTheta = BURNIN_THETA;
    _mainRunTheta = MAIN_RUN_THETA;
  }

  /**
   * All these setting are ignored if user supplies own grids and thetas
   * @param useBurnin useBurnin if true use a 'burn-in' period that consumes 20% of the time nodes (and hence the compute time) and runs with a theta of 1.0
   */
  public LocalVolatilityBackwardsPDEPricer(final boolean useBurnin) {
    _useBurnin = useBurnin;
    _burninFrac = BURNIN_FRACTION;
    _burninTheta = BURNIN_THETA;
    _mainRunTheta = MAIN_RUN_THETA;
  }

  /**
   * All these setting are ignored if user supplies own grids and thetas
   * @param useBurnin if true use a 'burn-in' period that consumes some fraction of the time nodes (and hence the compute time) and runs with a theta of 1.0
   * @param burninFrac The fraction of burn-in (ignored if useBurnin is false)
   */
  public LocalVolatilityBackwardsPDEPricer(final boolean useBurnin, final double burninFrac) {
    ArgumentChecker.isTrue(burninFrac < 0.5, "burn-in fraction too high");
    _useBurnin = useBurnin;
    _burninFrac = burninFrac;
    _burninTheta = BURNIN_THETA;
    _mainRunTheta = MAIN_RUN_THETA;
  }

  /**
   * All these setting are ignored if user supplies own grids and thetas
   * @param useBurnin if true use a 'burn-in' period that consumes some fraction of the time nodes (and hence the compute time) and runs with a different theta
   * @param burninFrac The fraction of burn-in (ignored if useBurnin is false)
   * @param burninTheta the theta to use for burnin (default is 1.0) (ignored if useBurnin is false)
   * @param mainTheta the theta to use for the main steps (default is 0.5)
   */
  public LocalVolatilityBackwardsPDEPricer(final boolean useBurnin, final double burninFrac, final double burninTheta, final double mainTheta) {
    ArgumentChecker.isTrue(burninFrac < 0.5, "burn-in fraction too high");
    ArgumentChecker.isTrue(0 <= burninTheta && burninTheta <= 1.0, "burn-in theta must be between 0 and 1.0");
    ArgumentChecker.isTrue(0 <= mainTheta && mainTheta <= 1.0, "main theta must be between 0 and 1.0");
    _useBurnin = useBurnin;
    _burninFrac = burninFrac;
    _burninTheta = burninTheta;
    _mainRunTheta = mainTheta;
  }

  /**
   * Price a European or American option on a commodity under the Black-Scholes-Merton assumptions (i.e. constant risk-free rate, cost-of-carry, and volatility) by using
   * finite difference methods to solve the Black-Scholes-Merton PDE. The grid is close to uniform in space (the strike and spot lie on the grid) and time<p>
   * Since a rather famous analytic formula exists for the price of European options on commodities that should be used in place of this
   * @param fwd the forward curve. This contains the spot and the instantaneous cost-of-carry (drift of spot)
   * @param riskFreeRate curve of instantaneous risk free rate against time
   * @param option the option details. Contains the strike, expiry and whether its a call or put
   * @param localVol the local volatility surface parameterized by strike
   * @param isAmerican true if the option is American (false for European)
   * @param spaceNodes Number of Space nodes
   * @param timeNodes Number of time nodes
   * @return The option price
   */
  public double price(final ForwardCurve fwd, final Curve<Double, Double> riskFreeRate, final EuropeanVanillaOption option, final LocalVolatilitySurfaceStrike localVol, final boolean isAmerican,
      final int spaceNodes, final int timeNodes) {

    final double t = option.getTimeToExpiry();
    final double s0 = fwd.getSpot();
    final double k = option.getStrike();
    final double sigma = Math.max(localVol.getVolatility(t, k), localVol.getVolatility(t, s0));

    final double mult = Math.exp(6.0 * sigma * Math.sqrt(t));
    final double sMin = Math.min(0.8 * k, s0 / mult);
    final double sMax = Math.max(1.25 * k, s0 * mult);

    // set up a near-uniform mesh that includes spot and strike
    final double[] fixedPoints = k == 0.0 ? new double[] {s0} : new double[] {s0, k};
    final MeshingFunction xMesh = new ExponentialMeshing(sMin, sMax, spaceNodes, 0.0, fixedPoints);

    PDEGrid1D[] grid;
    double[] theta;
    if (_useBurnin) {
      final int tBurnNodes = (int) Math.max(2, timeNodes * _burninFrac);
      final double tBurn = _burninFrac * t * t / timeNodes;
      if (tBurn >= t) { // very unlikely to hit this
        final int minNodes = (int) Math.ceil(_burninFrac * t);
        final double minFrac = timeNodes / t;
        throw new IllegalArgumentException("burn in period greater than total time. Either increase timeNodes to above " + minNodes + ", or reduce burninFrac to below " + minFrac);
      }
      final MeshingFunction tBurnMesh = new ExponentialMeshing(0.0, tBurn, tBurnNodes, 0.0);
      final MeshingFunction tMesh = new ExponentialMeshing(tBurn, t, timeNodes - tBurnNodes, 0.0);
      grid = new PDEGrid1D[2];
      grid[0] = new PDEGrid1D(tBurnMesh, xMesh);
      grid[1] = new PDEGrid1D(tMesh, xMesh);
      theta = new double[] {_burninTheta, _mainRunTheta};
    } else {
      grid = new PDEGrid1D[1];
      final MeshingFunction tMesh = new ExponentialMeshing(0, t, timeNodes, 0.0);
      grid[0] = new PDEGrid1D(tMesh, xMesh);
      theta = new double[] {_mainRunTheta};
    }

    return price(fwd, riskFreeRate, option, localVol, isAmerican, grid, theta);
  }

  /**
   * Price a European or American option on a commodity under the Black-Scholes-Merton assumptions (i.e. constant risk-free rate, cost-of-carry, and volatility) by using
   * finite difference methods to solve the Black-Scholes-Merton PDE. The spatial (spot) grid concentrates points around the spot level and ensures that
   * strike and spot lie on the grid. The temporal grid concentrates points near time-to-expiry = 0 (i.e. the start). The PDE solver uses theta = 0.5 (Crank-Nicolson)
   * unless a burn-in period is use, in which case theta = 1.0 (fully implicit) in that region.
   * @param fwd the forward curve. This contains the spot and the instantaneous cost-of-carry (drift of spot)
   * @param riskFreeRate curve of instantaneous risk free rate against time
   * @param option the option details. Contains the strike, expiry and whether its a call or put
   * @param localVol the local volatility surface parameterized by strike
   * @param isAmerican true if the option is American (false for European)
   * @param spaceNodes Number of Space nodes
   * @param timeNodes Number of time nodes
   * @param beta Bunching parameter for space (spot) nodes. A value great than zero. Very small values gives a very high density of points around the spot, with the
   * density quickly falling away in both directions
   * @param lambda Bunching parameter for time nodes. $\lambda = 0$ is uniform, $\lambda > 0$ gives a high density of points near $\tau = 0$
   * @param sd The number of standard deviations from s0 to place the boundaries. Values between 3 and 6 are recommended.
   * @return The option price
   */
  public double price(final ForwardCurve fwd, final Curve<Double, Double> riskFreeRate, final EuropeanVanillaOption option, final LocalVolatilitySurfaceStrike localVol, final boolean isAmerican,
      final int spaceNodes, final int timeNodes, final double beta, final double lambda, final double sd) {

    final double t = option.getTimeToExpiry();
    final double s0 = fwd.getSpot();
    final double k = option.getStrike();
    final double sigma = Math.max(localVol.getVolatility(t, k), localVol.getVolatility(t, s0));

    final double sigmaRootT = sigma * Math.sqrt(t);
    final double mult = Math.exp(sd * sigmaRootT);
    final double sMin = Math.min(k, s0 / mult);
    final double sMax = s0 * mult;
    if (sMax <= 1.25 * k) {
      final double minSD = Math.log(1.25 * k / s0) / sigmaRootT;
      throw new IllegalArgumentException("sd does not give boundaries that contain the strike. Use a minimum value of " + minSD);
    }

    // centre the nodes around the spot
    final double[] fixedPoints = k == 0.0 ? new double[] {s0} : new double[] {s0, k};
    final MeshingFunction xMesh = new HyperbolicMeshing(sMin, sMax, s0, spaceNodes, beta, fixedPoints);

    MeshingFunction tMesh = new ExponentialMeshing(0, t, timeNodes, lambda);
    final PDEGrid1D[] grid;
    final double[] theta;

    if (_useBurnin) {
      final int tBurnNodes = (int) Math.max(2, timeNodes * _burninFrac);
      final double dt = tMesh.evaluate(1) - tMesh.evaluate(0);
      final double tBurn = tBurnNodes * dt * dt;
      final MeshingFunction tBurnMesh = new ExponentialMeshing(0, tBurn, tBurnNodes, 0.0);
      tMesh = new ExponentialMeshing(tBurn, t, timeNodes - tBurnNodes, lambda);
      grid = new PDEGrid1D[2];
      grid[0] = new PDEGrid1D(tBurnMesh, xMesh);
      grid[1] = new PDEGrid1D(tMesh, xMesh);
      theta = new double[] {_burninTheta, _mainRunTheta};
    } else {
      grid = new PDEGrid1D[1];
      grid[0] = new PDEGrid1D(tMesh, xMesh);
      theta = new double[] {_mainRunTheta};
    }

    return price(fwd, riskFreeRate, option, localVol, isAmerican, grid, theta);
  }

  /**
   * Price a European or American option on a commodity under the Black-Scholes-Merton assumptions (i.e. constant risk-free rate, cost-of-carry, and volatility) by using
   * finite difference methods to solve the Black-Scholes-Merton PDE. <b>Note</b> This is a specialist method that requires correct grid
   * set up - if unsure use another method that sets up the grid for you.
   * @param fwd the forward curve. This contains the spot and the instantaneous cost-of-carry (drift of spot)
   * @param riskFreeRate curve of instantaneous risk free rate against time
   * @param option the option details. Contains the strike, expiry and whether its a call or put
   * @param localVol the local volatility surface parameterized by strike
   * @param isAmerican true if the option is American (false for European)
   * @param grid the grids. If a single grid is used, the spot must be a grid point and the strike
   * must lie in the range of the xNodes; the time nodes must start at zero and finish at t (time-to-expiry). For multiple grids,
   * the xNodes must be <b>identical</b>, and the last time node of one grid must be the same as the first time node of the next.
   * @param theta the theta to use on different grids
   * @return The option price
   */
  public double price(final ForwardCurve fwd, final Curve<Double, Double> riskFreeRate, final EuropeanVanillaOption option, final LocalVolatilitySurfaceStrike localVol, final boolean isAmerican,
      final PDEGrid1D[] grid, final double[] theta) {

    final int n = grid.length;
    ArgumentChecker.isTrue(n == theta.length, "#theta does not match #grid");
    final double t = option.getTimeToExpiry();
    final double k = option.getStrike();
    final double s0 = fwd.getSpot();
    final Curve<Double, Double> costOfCarry = fwd.getDriftCurve();

    // TODO allow change in grid size and remapping (via spline?) of nodes
    // ensure the grids are consistent
    final double[] xNodes = grid[0].getSpaceNodes();
    ArgumentChecker.isTrue(grid[0].getTimeNode(0) == 0.0, "time nodes not starting from zero");
    ArgumentChecker.isTrue(Double.compare(grid[n - 1].getTimeNode(grid[n - 1].getNumTimeNodes() - 1), t) == 0, "time nodes not ending at t");
    for (int ii = 1; ii < n; ii++) {
      ArgumentChecker.isTrue(Arrays.equals(grid[ii].getSpaceNodes(), xNodes), "different xNodes not supported");
      ArgumentChecker.isTrue(Double.compare(grid[ii - 1].getTimeNode(grid[ii - 1].getNumTimeNodes() - 1), grid[ii].getTimeNode(0)) == 0, "time nodes not consistent");
    }

    final double sMin = xNodes[0];
    final double sMax = xNodes[xNodes.length - 1];
    ArgumentChecker.isTrue(sMin <= k, "strike lower than sMin");
    ArgumentChecker.isTrue(sMax >= k, "strike higher than sMax");

    final int index = Arrays.binarySearch(xNodes, s0);
    ArgumentChecker.isTrue(index >= 0, "cannot find spot on grid");

    final ConvectionDiffusionPDE1DStandardCoefficients coef = PDE.getBackwardsLocalVol(riskFreeRate, costOfCarry, t, localVol);
    final Function1D<Double, Double> payoff = ICP.getEuropeanPayoff(k, option.isCall());

    BoundaryCondition lower;
    BoundaryCondition upper;

    PDEResults1D res;

    if (isAmerican) {
      if (option.isCall()) {
        lower = new NeumannBoundaryCondition(0.0, sMin, true);
        upper = new NeumannBoundaryCondition(1.0, sMax, false);
      } else {
        lower = new NeumannBoundaryCondition(-1.0, sMin, true);
        upper = new NeumannBoundaryCondition(0.0, sMax, false);
      }

      final Function<Double, Double> func = new Function<Double, Double>() {
        @Override
        public Double evaluate(final Double... tx) {
          final double x = tx[1];
          return payoff.evaluate(x);
        }
      };

      final FunctionalDoublesSurface free = new FunctionalDoublesSurface(func);

      PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> data = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(coef, payoff, lower, upper, free, grid[0]);
      ThetaMethodFiniteDifference solver = new ThetaMethodFiniteDifference(theta[0], false);
      res = solver.solve(data);
      for (int ii = 1; ii < n; ii++) {
        data = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(coef, res.getTerminalResults(), lower, upper, free, grid[ii]);
        solver = new ThetaMethodFiniteDifference(theta[ii], false);
        res = solver.solve(data);
      }
      // European
    } else {
      if (option.isCall()) {
        lower = new NeumannBoundaryCondition(0.0, sMin, true);
        final Function1D<Double, Double> upFunc = new Function1D<Double, Double>() {
          @Override
          public Double evaluate(final Double tau) {
            return Math.exp((costOfCarry.getYValue(tau) - riskFreeRate.getYValue(tau)) * tau);
          }
        };
        upper = new NeumannBoundaryCondition(upFunc, sMax, false);
      } else {
        final Function1D<Double, Double> downFunc = new Function1D<Double, Double>() {
          @Override
          public Double evaluate(final Double tau) {
            return -Math.exp((costOfCarry.getYValue(tau) - riskFreeRate.getYValue(tau)) * tau);
          }
        };
        lower = new NeumannBoundaryCondition(downFunc, sMin, true);
        upper = new NeumannBoundaryCondition(0.0, sMax, false);
      }
      PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> data = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(coef, payoff, lower, upper, grid[0]);
      ThetaMethodFiniteDifference solver = new ThetaMethodFiniteDifference(theta[0], false);
      res = solver.solve(data);
      for (int ii = 1; ii < n; ii++) {
        data = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(coef, res.getTerminalResults(), lower, upper, grid[ii]);
        solver = new ThetaMethodFiniteDifference(theta[ii], false);
        res = solver.solve(data);
      }
    }

    return res.getFunctionValue(index);
  }
}
