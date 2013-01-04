/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import static com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SurfaceArrayUtils.getLowerBoundIndex;

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
import com.opengamma.analytics.financial.model.finitedifference.applications.PDE1DCoefficientsProvider;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.local.LocalVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.local.PureLocalVolatilitySurface;
import com.opengamma.analytics.math.function.Function1D;

/**
 * Class to calculate the expected variance (<b>not</b> annualised) of an equity variance swap when a discount curve, affine dividends and a <b>pure</b> local volatility surface is 
 * specified. See White (2012), Equity Variance Swap with Dividends, for details of the model.
 */
public class EquityVarianceSwapBackwardsPurePDE {
  /** Bunching parameter for the time mesh */
  private static final double LAMBDA_T = -4.0;
  /** Bunching parameter for the space mesh */
  private static final double LAMBDA_X = 0.1;
  /** Maximum range for volatility */
  private static final double SIGMA = 4.0;  //TODO changed from 6.0

  /** Default weighting the averaging an explicit and implicit scheme */
  private static final double DEFAULT_THETA = 0.5;
  /** */
  private static final PDE1DCoefficientsProvider PDE_PROVIDER = new PDE1DCoefficientsProvider();

  /** */
  private final int _nTimeSteps;
  /** */
  private final int _nSpaceSteps;
  /** */
  private final ConvectionDiffusionPDESolver _solver;

  /**
   * Sets up the PDE
   */
  public EquityVarianceSwapBackwardsPurePDE() {
    _nTimeSteps = 100;
    _nSpaceSteps = 100;
    _solver = new ThetaMethodFiniteDifference(DEFAULT_THETA, false);
  }

  /**
   * Computes the expected variance by solving the backward PDE.
   * @param spot The current level of the stock or index, greater than zero
   * @param discountCurve The risk free interest rate curve, not null
   * @param dividends The dividends structure, not null
   * @param expiry The expiry of the variance swap, greater than zero
   * @param pureLocalVolSurface A <b>pure</b> local volatility surface, not null
   * @return The expected variance with and without adjustments for the dividend payments (the former is usually the case for single stock and the latter for indices)
   */
  public double[] expectedVariance(final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends, final double expiry,
      final PureLocalVolatilitySurface pureLocalVolSurface) {
    final EquityDividendsCurvesBundle divs = new EquityDividendsCurvesBundle(spot, discountCurve, dividends);
    final double logNoDivFwd = Math.log(spot) + discountCurve.getInterestRate(expiry) * expiry;
    //"convert" to a LocalVolatilitySurfaceMoneyness _ DO NOT interpret this as an actual LocalVolatilitySurfaceMoneyness
    final LocalVolatilitySurfaceMoneyness localVolSurface = new LocalVolatilitySurfaceMoneyness(pureLocalVolSurface.getSurface(), new ForwardCurve(1.0));

    final ConvectionDiffusionPDE1DStandardCoefficients pde = PDE_PROVIDER.getLogBackwardsLocalVol(expiry, localVolSurface);
    final Function1D<Double, Double> logPayoff = getLogPayoff(divs, expiry);

    //evaluate the log-payoff on a nominally six sigma range
    final double atmVol = pureLocalVolSurface.getVolatility(expiry, 1.0);
    final double yMin = -Math.sqrt(expiry) * atmVol * SIGMA;
    final double yMax = -yMin;

    final BoundaryCondition lower = new NeumannBoundaryCondition(getLowerBoundaryCondition(divs, expiry), yMin, true);
    final BoundaryCondition upper = new NeumannBoundaryCondition(1.0, yMax, false);

    final MeshingFunction timeMesh = new ExponentialMeshing(0, expiry, _nTimeSteps, LAMBDA_T);
    final MeshingFunction spaceMesh = new ExponentialMeshing(yMin, yMax, _nSpaceSteps, LAMBDA_X);

    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde, logPayoff, lower, upper, grid);
    final PDEResults1D res = _solver.solve(db);

    final int index = getLowerBoundIndex(res.getGrid().getSpaceNodes(), 0.0);
    final double x1 = res.getSpaceValue(index);
    final double x2 = res.getSpaceValue(index + 1);
    final double y1 = res.getFunctionValue(index);
    final double y2 = res.getFunctionValue(index + 1);
    final double dx = x2 - x1;
    final double eLogS = (x2 * y1 - x1 * y2) / dx;
    final double rvNoDivs = -2 * (eLogS - logNoDivFwd);

    final int nDivs = dividends.getNumberOfDividends();

    double corrDivAdj = 0;
    double uncorrDivAdj = 0;
    int i = 0;
    while (nDivs > 0 && i < nDivs && dividends.getTau(i) <= expiry) {
      corrDivAdj += getCorrection(dividends, divs, i, true, pureLocalVolSurface);
      uncorrDivAdj += getCorrection(dividends, divs, i, false, pureLocalVolSurface);
      i++;
    }

    //TODO add correction terms 
    return new double[] {rvNoDivs + 2 * corrDivAdj, rvNoDivs + 2 * uncorrDivAdj };
  }

  private double getCorrection(final AffineDividends ad, final EquityDividendsCurvesBundle curves, final int index,
      final boolean correctForDividends, final PureLocalVolatilitySurface plv) {
    final double alpha = ad.getAlpha(index);
    if (alpha == 0.0) {
      final double temp = Math.log(1 - ad.getBeta(index));
      return correctForDividends ? temp : temp + 0.5 * temp * temp;
    }
    final double tau = ad.getTau(index);
    final double atmVol = plv.getVolatility(tau, 1.0);
    final double yMin = -Math.sqrt(tau) * atmVol * SIGMA;
    final double yMax = -yMin;

    final LocalVolatilitySurfaceMoneyness localVolSurface = new LocalVolatilitySurfaceMoneyness(plv.getSurface(), new ForwardCurve(1.0));
    final ConvectionDiffusionPDE1DStandardCoefficients pde = PDE_PROVIDER.getLogBackwardsLocalVol(tau, localVolSurface);
    final Function1D<Double, Double> initalCond = getCorrectionInitialCondition(ad, curves, index, correctForDividends);

    final BoundaryCondition lower = new NeumannBoundaryCondition(getCorrectionLowerBoundaryCondition(ad, curves, index, correctForDividends, index), yMin, true);
    final BoundaryCondition upper = new NeumannBoundaryCondition(0.0, yMax, false);

    final MeshingFunction timeMesh = new ExponentialMeshing(0, tau, _nTimeSteps, LAMBDA_T);
    final MeshingFunction spaceMesh = new HyperbolicMeshing(yMin, yMax, 0.0, _nSpaceSteps, LAMBDA_X);

    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients>(pde, initalCond, lower, upper, grid);
    final PDEResults1D res = _solver.solve(db);

    final int gIndex = getLowerBoundIndex(res.getGrid().getSpaceNodes(), 0.0);
    final double x1 = res.getSpaceValue(gIndex);
    final double x2 = res.getSpaceValue(gIndex + 1);
    final double y1 = res.getFunctionValue(gIndex);
    final double y2 = res.getFunctionValue(gIndex + 1);
    final double dx = x2 - x1;
    return (x2 * y1 - x1 * y2) / dx;
  }

  private Function1D<Double, Double> getLogPayoff(final EquityDividendsCurvesBundle divs, final double expiry) {
    final double f = divs.getF(expiry);
    final double d = divs.getD(expiry);
    final double logF = (d == 0 ? Math.log(f) : 0.0);
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double y) {
        if (d == 0) {
          return logF + y;
        }
        final double x = Math.exp(y);
        final double s = (f - d) * x + d;
        return Math.log(s);
      }
    };
  }

  //TODO this needs to be checked
  private Function1D<Double, Double> getLowerBoundaryCondition(final EquityDividendsCurvesBundle divs, final double expiry) {
    final double f = divs.getF(expiry);
    final double d = divs.getD(expiry);
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double y) {
        if (d == 0) {
          return 1.0;
        }
        final double x = Math.exp(y);
        return (f - d) * x / ((f - d) * x + d);
      }
    };
  }

  private Function1D<Double, Double> getCorrectionInitialCondition(final AffineDividends ad, final EquityDividendsCurvesBundle curves, final int index,
      final boolean correctForDividends) {
    final double tau = ad.getTau(index);
    final double alpha = ad.getAlpha(index);
    final double beta = ad.getBeta(index);
    final double f = curves.getF(tau);
    final double d = curves.getD(tau);

    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double y) {

        final double x = Math.exp(y);
        final double s = (f - d) * x + d;
        final double temp = Math.log(s * (1 - beta) / (s + alpha));
        return correctForDividends ? temp : temp + 0.5 * temp * temp;
      }
    };
  }

  private Function1D<Double, Double> getCorrectionLowerBoundaryCondition(final AffineDividends ad, final EquityDividendsCurvesBundle curves, final int index,
      final boolean correctForDividends, final double yMin) {
    final double tau = ad.getTau(index);
    final double alpha = ad.getAlpha(index);
    final double beta = ad.getBeta(index);
    final double f = curves.getF(tau);
    final double d = curves.getD(tau);
    final double x = Math.exp(yMin);
    //the assumption is that for very small x, x is stuck at low values, so the stock value at the dividend is just this projected by the forward 
    final double s = (f - d) * x + d;
    final double temp = Math.log(s * (1 - beta) / (s + alpha));
    final double res = (correctForDividends ? 1.0 : 1.0 + temp) * alpha / s / (s + alpha) * (f - d);
    //not time dependent
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double t) {
        return res;
      }
    };
  }

}
