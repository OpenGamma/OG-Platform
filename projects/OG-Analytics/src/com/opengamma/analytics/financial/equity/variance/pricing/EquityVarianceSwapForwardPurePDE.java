/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DStandardCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDESolver;
import com.opengamma.analytics.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDE1DDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDETerminalResults1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.financial.model.finitedifference.applications.InitialConditionsProvider;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDE1DCoefficientsProvider;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.local.PureLocalVolatilitySurface;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
   * Class to calculate the expected variance (NOT annualised) of an equity variance swap when a discount curve, affine dividends and a PURE local volatility surface is 
   * specified. See White (2012), Equity Variance Swap with Dividends, for details of the model 
 */
public class EquityVarianceSwapForwardPurePDE {

  private static final double DEFAULT_THETA = 0.5;
  private static final PDE1DCoefficientsProvider PDE_PROVIDER = new PDE1DCoefficientsProvider();
  private static final InitialConditionsProvider INITIAL_COND_PROVIDER = new InitialConditionsProvider();

  private final int _nTimeSteps;
  private final int _nSpaceSteps;
  private final int _minStepsBetweenDividends = 5;
  private final ConvectionDiffusionPDESolver _solver;

  /**
   * Class to calculate the expected variance (NOT annualised) of an equity variance swap when a discount curve, affine dividends and a PURE local volatility surface is 
   * specified. See White (2012), Equity Variance Swap with Dividends, for details of the model 
   */
  public EquityVarianceSwapForwardPurePDE() {
    _nTimeSteps = 100;
    _nSpaceSteps = 100;
    _solver = new ThetaMethodFiniteDifference(DEFAULT_THETA, false);
  }

  /**
   * Class to calculate the expected variance (NOT annualised) of an equity variance swap when a discount curve, affine dividends and a PURE local volatility surface is 
   * specified. See White (2012), Equity Variance Swap with Dividends, for details of the model 
   * @param theta The weight used in the finite difference scheme. theta = 0 - fully explicit, theta = 0.5 - Crank-Nicolson, theta = 1.0 - fully implicit
   * @param numTimeSteps The number of time steps used in the finite difference scheme. <b> NOTE</b> more step may be taken, as the is a minimum number of steps between dividends 
   * @param numSpaceSteps The number of time space used in the finite difference scheme.
   */
  public EquityVarianceSwapForwardPurePDE(final double theta, final int numTimeSteps, final int numSpaceSteps) {
    ArgumentChecker.isTrue(numTimeSteps > 0, "numTimeSteps must bem positive");
    ArgumentChecker.isTrue(numSpaceSteps > 0, "numSpaceSteps must bem positive");
    _nSpaceSteps = numSpaceSteps;
    _nTimeSteps = numTimeSteps;
    _solver = new ThetaMethodFiniteDifference(theta, false);

  }

  /**
   * Computes the expected variance by solving the forward PDE for the price of a call on the pure stock price, then using these computed prices at the expiry and all
   * dividend dates, computes the expected variance with and without adjustments for the dividend payments
   * @param spot The current level of the stock or index
   * @param discountCurve The risk free interest rate curve 
   * @param dividends The dividends structure 
   * @param expiry The expiry of the variance swap 
   * @param pureLocalVolSurface A <b>pure</b> local volatility surface
   * @return The expected variance with and without adjustments for the dividend payments (the former is usually the case for single stock and the latter for indices)
   */
  public double[] expectedVariance(final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends, final double expiry,
      final PureLocalVolatilitySurface pureLocalVolSurface) {

    //"convert" to a LocalVolatilitySurfaceMoneyness _ DO NOT interpret this as an actual LocalVolatilitySurfaceMoneyness
    LocalVolatilitySurfaceMoneyness localVolSurface = new LocalVolatilitySurfaceMoneyness(pureLocalVolSurface.getSurface(), new ForwardCurve(1.0));

    ConvectionDiffusionPDE1DStandardCoefficients pde = PDE_PROVIDER.getForwardLocalVol(localVolSurface);
    Function1D<Double, Double> initialCond = INITIAL_COND_PROVIDER.getForwardCallPut(true);

    final EquityDividendsCurvesBundle divCurves = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);
    final double terminalFwd = divCurves.getF(expiry);
    final double logNoDivFwd = Math.log(spot) + discountCurve.getInterestRate(expiry) * expiry;
    final int n = dividends.getNumberOfDividends();

    final double stepLength = expiry / _nTimeSteps;
    int count = 0;
    while (n > 0 && count < n && dividends.getTau(count) < expiry) {
      count++;
    }
    final boolean divAtExp = n > 0 && count < n && dividends.getTau(count) == expiry;
    final int nDivsBeforeExpiry = count;
    final int[] steps = new int[nDivsBeforeExpiry + 1];
    int tSteps = 0;
    for (int i = 0; i < nDivsBeforeExpiry; i++) {
      steps[i] = (int) Math.max(_minStepsBetweenDividends, (dividends.getTau(i) - (i == 0 ? 0.0 : dividends.getTau(i - 1))) / stepLength);
      tSteps += steps[i];
    }
    steps[nDivsBeforeExpiry] = Math.max(_minStepsBetweenDividends, _nTimeSteps - tSteps);

    //common boundary conditions 
    //TODO the grid involves some magic numbers that should be possible to externally by expert users 
    double xL = 0.0;
    double xH = 6;
    BoundaryCondition lower = new DirichletBoundaryCondition(1.0, xL);
    BoundaryCondition upper = new NeumannBoundaryCondition(0.0, xH, false);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(xL, xH, 1.0, _nSpaceSteps + 1, 0.05);
    final MeshingFunction[] timeMeshes = new MeshingFunction[nDivsBeforeExpiry + 1];
    final PDEGrid1D[] grids = new PDEGrid1D[nDivsBeforeExpiry + 1];
    if (nDivsBeforeExpiry == 0) {
      timeMeshes[0] = new ExponentialMeshing(0, expiry, _nTimeSteps, 0.0);
    } else {
      timeMeshes[0] = new ExponentialMeshing(0, dividends.getTau(0), steps[0], 0.0);
      for (int i = 1; i < nDivsBeforeExpiry; i++) {
        timeMeshes[i] = new ExponentialMeshing(dividends.getTau(i - 1), dividends.getTau(i), steps[i], 0.0);
      }
      timeMeshes[nDivsBeforeExpiry] = new ExponentialMeshing(dividends.getTau(nDivsBeforeExpiry - 1), expiry, steps[nDivsBeforeExpiry], 0.0);
    }
    PDETerminalResults1D[] res = new PDETerminalResults1D[nDivsBeforeExpiry + 1];
    grids[0] = new PDEGrid1D(timeMeshes[0], spaceMesh);
    PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde, initialCond, lower, upper, grids[0]);
    res[0] = (PDETerminalResults1D) _solver.solve(db);
    for (int i = 1; i <= nDivsBeforeExpiry; i++) {
      grids[i] = new PDEGrid1D(timeMeshes[i], spaceMesh);
      db = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde, res[i - 1].getFinalTimePrices(), lower, upper, grids[i]);
      res[i] = (PDETerminalResults1D) _solver.solve(db);
    }

    double corrDivAdj = 0;
    double uncorrDivAdj = 0;
    for (int i = 0; i < nDivsBeforeExpiry; i++) {
      double f = divCurves.getF(dividends.getTau(i));
      corrDivAdj += integrate(getCorrectedDividendAdjustmentWeight(i, divCurves, dividends), res[i]) + getCorrectedDividendAdjustment(f, i, dividends);
      uncorrDivAdj += integrate(getUncorrectedDividendAdjustmentWeight(i, divCurves, dividends), res[i]) + getUncorrectedDividendAdjustment(f, i, dividends);
    }
    if (divAtExp) {
      corrDivAdj += integrate(getCorrectedDividendAdjustmentWeight(nDivsBeforeExpiry, divCurves, dividends), res[nDivsBeforeExpiry]) +
          getCorrectedDividendAdjustment(terminalFwd, nDivsBeforeExpiry, dividends);
      uncorrDivAdj += integrate(getUncorrectedDividendAdjustmentWeight(nDivsBeforeExpiry, divCurves, dividends), res[nDivsBeforeExpiry]) +
          getUncorrectedDividendAdjustment(terminalFwd, nDivsBeforeExpiry, dividends);
    }

    double logContract = integrate(getLogContractWeight(divCurves, expiry), res[nDivsBeforeExpiry]) + Math.log(terminalFwd);

    final double rvNoDivs = -2 * (logContract - logNoDivFwd);
    final double rvCorrDivs = rvNoDivs + 2 * corrDivAdj;
    final double rvUncorrDivs = rvNoDivs + 2 * uncorrDivAdj;
    return new double[] {rvCorrDivs, rvUncorrDivs };
  }

  private double integrate(final Function1D<Double, Double> weightFunc, final PDETerminalResults1D pdeRes) {
    int n = pdeRes.getNumberSpaceNodes();
    double[] x = pdeRes.getGrid().getSpaceNodes();
    double sum = 0.0;
    for (int i = 1; i < n - 1; i++) {
      double pureCallPrice = pdeRes.getFunctionValue(i);
      double otmPrice = pureCallPrice - (x[i] < 1.0 ? 1 - x[i] : 0);
      double w = weightFunc.evaluate(x[i]);
      sum += w * otmPrice * (x[i + 1] - x[i - 1]);
    }
    sum /= 2.0;
    //don't add on the end points;
    return sum;
  }

  private Function1D<Double, Double> getLogContractWeight(final EquityDividendsCurvesBundle divCurves, final double expiry) {
    final double f = divCurves.getF(expiry);
    final double d = divCurves.getD(expiry);
    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double x) {
        return -FunctionUtils.square((f - d) / ((f - d) * x + d));
      }
    };
  }

  private Function1D<Double, Double> getCorrectedDividendAdjustmentWeight(final int dividendIndex, final EquityDividendsCurvesBundle divCurves, final AffineDividends dividends) {
    final double tau = dividends.getTau(dividendIndex);
    final double f = divCurves.getF(tau);
    final double d = divCurves.getD(tau);
    final double alpha = dividends.getAlpha(dividendIndex);
    final double fMd2 = FunctionUtils.square(f - d);

    return new Function1D<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double x) {
        final double s = (f - d) * x + d;
        final double sPalpha = s + alpha;
        final double ddH = -alpha * (s + sPalpha) / s / s / sPalpha / sPalpha;
        final double weight = fMd2 * ddH;
        return weight;
      }
    };
  }

  private Function1D<Double, Double> getUncorrectedDividendAdjustmentWeight(final int dividendIndex, final EquityDividendsCurvesBundle divCurves, final AffineDividends dividends) {
    final double tau = dividends.getTau(dividendIndex);
    final double f = divCurves.getF(tau);
    final double d = divCurves.getD(tau);
    final double alpha = dividends.getAlpha(dividendIndex);
    final double beta = dividends.getBeta(dividendIndex);
    final double fMd2 = FunctionUtils.square(f - d);

    return new Function1D<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double x) {
        final double s = (f - d) * x + d;
        final double sPalpha = s + alpha;
        final double h = Math.log(s * (1 - beta) / sPalpha);
        final double dH = alpha / s / sPalpha;
        final double ddH = -alpha * (s + sPalpha) / s / s / sPalpha / sPalpha;
        final double weight = fMd2 * ((1 + h) * ddH + dH * dH);
        return weight;
      }
    };
  }

  private double getCorrectedDividendAdjustment(final double s, final int dividendIndex, final AffineDividends dividends) {
    final double alpha = dividends.getAlpha(dividendIndex);
    final double beta = dividends.getBeta(dividendIndex);
    final double h = Math.log(s * (1 - beta) / (s + alpha));
    return h;
  }

  private double getUncorrectedDividendAdjustment(final double s, final int dividendIndex, final AffineDividends dividends) {
    final double alpha = dividends.getAlpha(dividendIndex);
    final double beta = dividends.getBeta(dividendIndex);
    final double h = Math.log(s * (1 - beta) / (s + alpha));
    return h + 0.5 * h * h;
  }
}
