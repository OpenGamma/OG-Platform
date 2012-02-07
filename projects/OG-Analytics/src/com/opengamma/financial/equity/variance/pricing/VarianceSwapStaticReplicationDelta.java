/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.variance.pricing;

import com.opengamma.financial.equity.variance.VarianceSwapDataBundle2;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackImpliedStrikeFromDeltaFunction;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurfaceDelta;
import com.opengamma.financial.model.volatility.surface.Delta;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.Integrator1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class VarianceSwapStaticReplicationDelta extends VarianceSwapStaticReplication2<Delta> {

  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  public VarianceSwapStaticReplicationDelta() {
    super(new Delta(EPS), new Delta(1 - EPS));
  }

  public VarianceSwapStaticReplicationDelta(final Delta lowerBound, final Delta upperBound, final Integrator1D<Double, Double> integrator,
      final Delta cutoffLevel, final Delta cutoffSpread) {
    super(lowerBound, upperBound, integrator, cutoffLevel, cutoffSpread);
  }


  @Override
  protected Function1D<Double, Double> getMainIntegrand(final double expiry, final double fwd, final BlackVolatilitySurface<Delta> volSurf) {

    final BlackVolatilitySurfaceDelta volSurfaceDelta = (BlackVolatilitySurfaceDelta) volSurf;
    final double eps = 1e-5;

    return new Function1D<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double delta) {

        final double vol = volSurfaceDelta.getVolatilityForDelta(expiry, delta);
        final double strike = BlackImpliedStrikeFromDeltaFunction.impliedStrike(delta, true, fwd, expiry, vol);
        boolean isCall = strike >= fwd;

        //TODO if should be the job of the vol surface to provide derivatives
        double dSigmaDDelta;
        if (delta < eps) {
          final double volUp = volSurfaceDelta.getVolatilityForDelta(expiry, delta + eps);
          dSigmaDDelta = (volUp - vol) / eps;
        } else if (delta > 1 - eps) {
          final double volDown = volSurfaceDelta.getVolatilityForDelta(expiry, delta - eps);
          dSigmaDDelta = (vol - volDown) / eps;
        } else {
          final double volUp = volSurfaceDelta.getVolatilityForDelta(expiry, delta + eps);
          final double volDown = volSurfaceDelta.getVolatilityForDelta(expiry, delta - eps);
          dSigmaDDelta = (volUp - volDown) / 2 / eps;
        }

        final double d1 = NORMAL.getInverseCDF(delta);
        final double rootT = Math.sqrt(expiry);
        final double weight = (vol * rootT / NORMAL.getPDF(d1) + dSigmaDDelta * (d1 * rootT - vol * expiry)) / strike;
        final double otmPrice = BlackFormulaRepository.price(fwd, strike, expiry, vol, isCall);

        return weight * otmPrice;
      }
    };
  }

  @Override
  protected Pair<double[], double[]> getTailExtrapolationParameters(double fwd, double expiry, BlackVolatilitySurface<Delta> volSurf) {
    BlackVolatilitySurfaceDelta volsurfDelta = (BlackVolatilitySurfaceDelta) volSurf;
    double[] deltas = new double[2];
    double[] ks = new double[2];
    double[] vols = new double[2];
    deltas[0] = getCutoffLevel().value();
    deltas[1] = deltas[0] - getCutoffSpread().value(); //lower call delta, means higher strike
    for (int i = 0; i < 2; i++) {
      vols[i] = volsurfDelta.getVolatilityForDelta(expiry, deltas[i]);
      ks[i] = BlackImpliedStrikeFromDeltaFunction.impliedStrike(deltas[i], true, fwd, expiry, vols[i]);
    }
    return new ObjectsPair<double[], double[]>(ks, vols);

  }

  @Override
  protected Pair<Delta, Delta> getIntegralLimits(double expiry, VarianceSwapDataBundle2<Delta> market) {
    if (!isCutoffProvided()) {
      return new ObjectsPair<Delta, Delta>(getLowerBound(), getUpperBound());
    }
    double upper = Math.min(getUpperBound().value(), getCutoffLevel().value());
    return new ObjectsPair<Delta, Delta>(getLowerBound(), new Delta(upper));
  }
}


