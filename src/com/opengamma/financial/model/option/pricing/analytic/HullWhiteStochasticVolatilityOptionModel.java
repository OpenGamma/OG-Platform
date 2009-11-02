/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.HullWhiteStochasticVolatilityModelOptionDataBundle;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionPricingException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.InterpolationException;
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * @author emcleod
 * 
 */

public class HullWhiteStochasticVolatilityOptionModel extends AnalyticOptionModel<EuropeanVanillaOptionDefinition, HullWhiteStochasticVolatilityModelOptionDataBundle> {
  // TODO see if case when rho = 0 gives same results for uncorrelated model -
  // if so, add to the pricing function

  static final double ZERO = 1e-4;
  private final ProbabilityDistribution<Double> _normalProbabilityDistribution = new NormalProbabilityDistribution(0, 1);
  protected BlackScholesMertonModel _bsm = new BlackScholesMertonModel();

  @Override
  public Function1D<HullWhiteStochasticVolatilityModelOptionDataBundle, Double> getPricingFunction(final EuropeanVanillaOptionDefinition definition) {
    final Function1D<HullWhiteStochasticVolatilityModelOptionDataBundle, Double> pricingFunction = new Function1D<HullWhiteStochasticVolatilityModelOptionDataBundle, Double>() {

      @Override
      public Double evaluate(final HullWhiteStochasticVolatilityModelOptionDataBundle data) {
        try {
          final ZonedDateTime date = data.getDate();
          final double s = data.getSpot();
          final double k = definition.getStrike();
          final double t = definition.getTimeToExpiry(date);
          final double sigma = data.getVolatility(t, k);
          final double r = data.getInterestRate(t);
          final double b = data.getCostOfCarry();
          final double lambda = data.getHalfLife();
          final double sigmaLR = data.getLongRunVolatility();
          final double volOfSigma = data.getVolatilityOfVolatility();
          final double rho = data.getCorrelation();
          final StandardOptionDataBundle bsmData = new StandardOptionDataBundle(data.getDiscountCurve(), b, data.getVolatilitySurface(), data.getSpot(), date);
          final double df = getDF(r, b, t);
          final double beta = -Math.log(2) / lambda;
          final double alpha = -beta * sigmaLR * sigmaLR;
          final double delta = beta * t;
          final double eDelta = Math.exp(delta);
          final boolean betaIsZero = Math.abs(beta) < ZERO;
          final double meanVariance = getMeanVariance(betaIsZero, sigma * sigma, t, alpha, beta, eDelta, delta);
          final double meanStd = Math.sqrt(meanVariance);
          // TODO replace vol with Math.sqrt(meanVariance);
          final double d1 = getD1(s, k, t, meanStd, b);
          final double d2 = getD2(d1, meanStd, t);
          final double f0 = _bsm.getPricingFunction(definition).evaluate(bsmData);
          final double f1 = getF1(betaIsZero, s, t, df, d1, d2, meanVariance, alpha, beta, eDelta, delta, rho);
          final double f2 = getF2(betaIsZero, s, t, df, d1, d2, meanVariance, alpha, beta, eDelta, delta, rho);
          final double callPrice = f0 + f1 * volOfSigma + f2 * volOfSigma * volOfSigma;
          if (!definition.isCall())
            return callPrice - s * df + k * Math.exp(-r * t);
          return callPrice;
        } catch (final InterpolationException e) {
          throw new OptionPricingException(e);
        }
      }

    };
    return pricingFunction;
  }

  double getMeanVariance(final boolean betaIsZero, final double variance, final double t, final double alpha, final double beta, final double eDelta, final double delta) {
    if (betaIsZero)
      return variance + alpha * t / 2.;
    final double ratio = alpha / beta;
    return (variance + ratio) * (eDelta - 1) / delta - ratio;
  }

  double getPhi1(final boolean betaIsZero, final double variance, final double t, final double rho, final double alpha, final double beta, final double eDelta, final double delta) {
    if (betaIsZero)
      return rho * rho * t * t * t * (variance + alpha * t / 4.) / 6.;
    return rho * rho * ((alpha + beta * variance) * (eDelta * (delta * delta / 2. - delta + 1) - 1) + alpha * (eDelta * (2 - delta) - (2 + delta))) / (beta * beta * beta * beta);
  }

  double getPhi2(final boolean betaIsZero, final double variance, final double rho, final double alpha, final double beta, final double eDelta, final double delta,
      final double phi1) {
    if (betaIsZero)
      return phi1 * (2 + 1. / (rho * rho));
    final double eDelta2 = eDelta * eDelta;
    return 2 * phi1 + ((alpha + beta * variance) * (eDelta2 - 2 * eDelta * delta - 1) - alpha * (eDelta2 - 4 * eDelta + 2 * delta + 3) / 2.) / (2 * beta * beta * beta * beta);
  }

  double getIota(final double variance, final double rho, final double alpha, final double beta, final double eDelta, final double delta) {
    return rho * ((alpha + beta * variance) * (eDelta - delta * eDelta - 1) - alpha * (1 + delta - eDelta)) / (beta * beta * beta);
  }

  double getPhi3(final boolean betaIsZero, final double variance, final double t, final double rho, final double alpha, final double beta, final double eDelta, final double delta) {
    if (betaIsZero) {
      final double a = rho * t * (variance + alpha * t / 3.);
      return a * a / 8.;
    }
    final double a = getIota(variance, rho, alpha, beta, eDelta, delta);
    return a * a / 2.;
  }

  double getdCdSdV(final double s, final double df, final double nD1, final double d2, final double variance) {
    return -s * df * nD1 * d2 / (2 * variance);
  }

  double getdCdVdV(final double s, final double df, final double nD1, final double d1, final double d2, final double variance) {
    return s * df * nD1 * (d1 * d2 - 1) / (4 * Math.pow(variance, 1.5));
  }

  double getdCdSdVdV(final double s, final double df, final double nD1, final double d1, final double d2, final double variance) {
    return s * df * nD1 * (-d1 * d2 * d2 + d1 + 2 * d2) / (4 * variance * variance);
  }

  double getdCdVdVdV(final double s, final double df, final double nD1, final double d1, final double d2, final double variance) {
    // TODO multiply out brackets and simplify
    return s * df * nD1 * ((d1 * d2 - 1) * (d1 * d2 - 3) - (d1 * d1 + d2 * d2)) / (8 * Math.pow(variance, 2.5));
  }

  double getF1(final boolean betaIsZero, final double s, final double t, final double df, final double d1, final double d2, final double variance, final double alpha,
      final double beta, final double eDelta, final double delta, final double rho) {
    final double nD1 = _normalProbabilityDistribution.getPDF(d1);
    final double dCdSdV = getdCdSdV(s, df, nD1, d2, variance);
    if (betaIsZero)
      return rho * t * dCdSdV * (variance + alpha * t / 3.) / 2.;
    return getIota(variance, rho, alpha, beta, eDelta, delta) * dCdSdV / t;
  }

  double getF2(final boolean betaIsZero, final double s, final double t, final double df, final double d1, final double d2, final double variance, final double alpha,
      final double beta, final double eDelta, final double delta, final double rho) {
    final double phi1 = getPhi1(betaIsZero, variance, t, rho, alpha, beta, eDelta, delta);
    final double phi2 = getPhi2(betaIsZero, variance, rho, alpha, beta, eDelta, delta, phi1);
    final double phi3 = getPhi3(betaIsZero, variance, t, rho, alpha, beta, eDelta, delta);
    final double phi4 = 2 * phi3;
    final double nD1 = _normalProbabilityDistribution.getPDF(d1);
    final double dCdSdV = getdCdSdV(s, df, nD1, d2, variance);
    final double dCdVdV = getdCdVdV(s, df, nD1, d1, d2, variance);
    final double dCdSdVdV = getdCdSdVdV(s, df, nD1, d1, d2, variance);
    final double dCdVdVdV = getdCdVdVdV(s, df, nD1, d1, d2, variance);
    return (phi1 * dCdSdV + phi2 * dCdVdV / t + phi3 * dCdSdVdV / t + phi4 * dCdVdVdV / (t * t)) / t;
  }
}
