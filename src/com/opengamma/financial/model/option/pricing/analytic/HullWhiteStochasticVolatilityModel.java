/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.HullWhiteStochasticVolatilityModelOptionDataBundle;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 * @author emcleod
 * 
 */

public class HullWhiteStochasticVolatilityModel extends AnalyticOptionModel<OptionDefinition, HullWhiteStochasticVolatilityModelOptionDataBundle> {
  protected final ProbabilityDistribution<Double> _normal = new NormalDistribution(0, 1);
  protected BlackScholesMertonModel _bsm = new BlackScholesMertonModel();
  protected final double ZERO = 1e-4;

  @Override
  public Function1D<HullWhiteStochasticVolatilityModelOptionDataBundle, Double> getPricingFunction(final OptionDefinition definition) {
    if (definition == null)
      throw new IllegalArgumentException("Definition was null");
    final Function1D<HullWhiteStochasticVolatilityModelOptionDataBundle, Double> pricingFunction = new Function1D<HullWhiteStochasticVolatilityModelOptionDataBundle, Double>() {

      @Override
      public Double evaluate(final HullWhiteStochasticVolatilityModelOptionDataBundle data) {
        if (data == null)
          throw new IllegalArgumentException("Data bundle was null");
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
        final StandardOptionDataBundle bsmData = new StandardOptionDataBundle(data);
        final OptionDefinition call = definition.isCall() ? definition : new EuropeanVanillaOptionDefinition(k, definition.getExpiry(), true);
        final double beta = -Math.log(2) / lambda;
        final double alpha = -beta * sigmaLR * sigmaLR;
        final double delta = beta * t;
        final double eDelta = Math.exp(delta);
        final boolean betaIsZero = Math.abs(beta) < ZERO;
        final double variance = sigma * sigma;
        final double meanVariance = getMeanVariance(betaIsZero, variance, t, alpha, beta, eDelta, delta);
        final double sDf = s * getDF(r, b, t);
        final double d1 = getD(s, k, b, meanVariance, t);
        final double d2 = d1 - Math.sqrt(meanVariance * t);
        final double nD1 = _normal.getPDF(d1);
        final double f0 = _bsm.getPricingFunction(call).evaluate(bsmData.withVolatilitySurface(new ConstantVolatilitySurface(Math.sqrt(meanVariance))));
        final double f1 = getF1(betaIsZero, variance, rho, alpha, t, beta, delta, eDelta, sDf, nD1, d1, meanVariance);
        final double f2 = getF2(betaIsZero, variance, rho, alpha, t, beta, delta, eDelta, sDf, nD1, d1, d2, meanVariance);
        final double callPrice = f0 + f1 * volOfSigma + f2 * volOfSigma * volOfSigma;
        if (!definition.isCall()) {
          final double df = getDF(r, b, t);
          return callPrice - s * df + k * Math.exp(-r * t);
        }
        return callPrice;
      }

    };
    return pricingFunction;
  }

  double getMeanVariance(final boolean betaIsZero, final double variance, final double alpha, final double t, final double beta, final double eDelta, final double delta) {
    if (betaIsZero)
      return variance + alpha * t / 2.;
    final double ratio = alpha / beta;
    return (variance + ratio) * (eDelta - 1) / delta - ratio;
  }

  private double getPhi1(final boolean betaIsZero, final double variance, final double rho, final double alpha, final double t, final double beta, final double beta4,
      final double eDelta, final double delta) {
    if (betaIsZero)
      return rho * rho * (variance + alpha * t / 4.) * t * t * t / 6.;
    return rho * rho * ((alpha + beta * variance) * (eDelta * (delta * delta / 2. - delta + 1) - 1) + alpha * (eDelta * (2 - delta) - 2 - delta)) / beta4;
  }

  private double getPhi2(final boolean betaIsZero, final double phi1, final double variance, final double rho, final double alpha, final double beta, final double beta4,
      final double eDelta, final double delta) {
    if (betaIsZero)
      return phi1 * (2 + 1. / (rho * rho));
    final double eDeltaSq = eDelta * eDelta;
    return 2 * phi1 + ((alpha + beta * variance) * (eDeltaSq - 2 * delta * eDelta - 1) - alpha * (eDeltaSq - 4 * eDelta + 2 * delta + 3) / 2.) / (2 * beta4);
  }

  private double getPhi3(final boolean betaIsZero, final double variance, final double rho, final double alpha, final double t, final double beta, final double beta4,
      final double eDelta, final double delta) {
    if (betaIsZero)
      return rho * rho * Math.pow(variance + alpha * t / 3., 2) * t * t * t * t / 8.;
    final double x = (alpha + beta * variance) * (eDelta - delta * eDelta - 1) - alpha * (1 + delta - eDelta);
    return rho * rho * x * x / (2 * beta * beta * beta4);
  }

  private double getPhi4(final double phi3) {
    return 2 * phi3;
  }

  double getD(final double s, final double k, final double b, final double meanVariance, final double t) {
    return (Math.log(s / k) + t * (b + meanVariance / 2)) / Math.sqrt(meanVariance * t);
  }

  private double getCSV(final double sDf, final double nD1, final double d2, final double meanVariance) {
    return -sDf * nD1 * d2 / (2 * meanVariance);
  }

  private double getCVV(final double sDf, final double sqrtT, final double nD1, final double d1, final double d2, final double meanVariance) {
    return sDf * sqrtT * nD1 * (d1 * d2 - 1) / (4 * Math.pow(meanVariance, 1.5));
  }

  private double getCSVV(final double sDf, final double nD1, final double d1, final double d2, final double meanVariance) {
    return sDf * nD1 * (-d1 * d2 * d2 + d1 + 2 * d2) / (4 * meanVariance * meanVariance);
  }

  private double getCVVV(final double sDf, final double sqrtT, final double nD1, final double d1, final double d2, final double meanVariance) {
    return sDf * sqrtT * nD1 * ((d1 * d2 - 1) * (d1 * d2 - 3) - d1 * d1 - d2 * d2) / (8 * Math.pow(meanVariance, 2.5));
  }

  double getF1(final boolean betaIsZero, final double variance, final double rho, final double alpha, final double t, final double beta, final double delta, final double eDelta,
      final double sDf, final double nD1, final double d1, final double meanVariance) {
    final double cSV = getCSV(sDf, nD1, d1, meanVariance);
    if (betaIsZero)
      return rho * (variance + alpha * t / 3.) * t * cSV / 2.;
    return rho * cSV * ((alpha + beta * variance) * (1 - eDelta + delta * eDelta) + alpha * (1 + delta - eDelta)) / (beta * beta * beta * t);
  }

  double getF2(final boolean betaIsZero, final double variance, final double rho, final double alpha, final double t, final double beta, final double delta, final double eDelta,
      final double sDf, final double nD1, final double d1, final double d2, final double meanVariance) {
    final double beta4 = beta * beta * beta * beta;
    final double phi1 = getPhi1(betaIsZero, variance, rho, alpha, t, beta, beta4, eDelta, delta);
    final double phi2 = getPhi2(betaIsZero, phi1, variance, rho, alpha, beta, beta4, eDelta, delta);
    final double phi3 = getPhi3(betaIsZero, variance, rho, alpha, t, beta, beta4, eDelta, delta);
    final double phi4 = getPhi4(phi3);
    final double sqrtT = Math.sqrt(t);
    final double cSV = getCSV(sDf, nD1, d2, meanVariance);
    final double cVV = getCVV(sDf, sqrtT, nD1, d1, d2, meanVariance);
    final double cSVV = getCSVV(sDf, nD1, d1, d2, meanVariance);
    final double cVVV = getCVVV(sDf, sqrtT, nD1, d1, d2, meanVariance);
    final double tInv = 1 / t;
    return tInv * (phi1 * cSV + tInv * (phi2 * cVV + phi3 * cSVV + tInv * phi4 * cVVV));
  }
}
