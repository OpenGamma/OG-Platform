/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.HullWhiteStochasticVolatilityModelDataBundle;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.CompareUtils;

/**
 * 
 * 
 */

public class HullWhiteStochasticVolatilityModel extends AnalyticOptionModel<OptionDefinition, HullWhiteStochasticVolatilityModelDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final BlackScholesMertonModel BSM = new BlackScholesMertonModel();
  private static final double ZERO = 1e-4;

  @Override
  public Function1D<HullWhiteStochasticVolatilityModelDataBundle, Double> getPricingFunction(final OptionDefinition definition) {
    Validate.notNull(definition);
    final Function1D<HullWhiteStochasticVolatilityModelDataBundle, Double> pricingFunction = new Function1D<HullWhiteStochasticVolatilityModelDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final HullWhiteStochasticVolatilityModelDataBundle data) {
        Validate.notNull(data);
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
        final boolean betaIsZero = CompareUtils.closeEquals(beta, 0, ZERO);
        final double variance = sigma * sigma;
        final double meanVariance = getMeanVariance(betaIsZero, variance, alpha, t, beta, eDelta, delta);
        final double df = getDF(r, b, t);
        final double sDf = s * df;
        final double d1 = getD(s, k, b, meanVariance, t);
        final double d2 = d1 - Math.sqrt(meanVariance * t);
        final double nD1 = NORMAL.getPDF(d1);
        final double f0 = BSM.getPricingFunction(call).evaluate(bsmData.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(Math.sqrt(meanVariance)))));
        final double f1 = getF1(betaIsZero, variance, rho, alpha, t, beta, delta, eDelta, sDf, nD1, d2, meanVariance);
        final double f2 = getF2(betaIsZero, variance, rho, alpha, t, beta, delta, eDelta, sDf, nD1, d1, d2, meanVariance);
        final double callPrice = f0 + f1 * volOfSigma + f2 * volOfSigma * volOfSigma;
        if (!definition.isCall()) {
          return callPrice - s * df + k * Math.exp(-r * t);
        }
        return callPrice;
      }

    };
    return pricingFunction;
  }

  double getMeanVariance(final boolean betaIsZero, final double variance, final double alpha, final double t, final double beta, final double eDelta, final double delta) {
    if (betaIsZero) {
      return variance + alpha * t / 2.;
    }
    final double ratio = alpha / beta;
    return (variance + ratio) * (eDelta - 1) / delta - ratio;
  }

  private double getPhi1(final boolean betaIsZero, final double variance, final double rho, final double alpha, final double t, final double beta, final double beta4, final double eDelta,
      final double delta) {
    if (betaIsZero) {
      return rho * rho * (variance + alpha * t / 4.) * t * t * t / 6.;
    }
    return rho * rho * ((alpha + beta * variance) * (eDelta * (delta * delta / 2. - delta + 1) - 1) + alpha * (eDelta * (2 - delta) - 2 - delta)) / beta4;
  }

  private double getPhi2(final boolean betaIsZero, final double phi1, final double variance, final double rho, final double alpha, final double beta, final double beta4, final double eDelta,
      final double delta) {
    if (betaIsZero) {
      return phi1 * (2 + 1. / (rho * rho));
    }
    final double eDeltaSq = eDelta * eDelta;
    return 2 * phi1 + ((alpha + beta * variance) * (eDeltaSq - 2 * delta * eDelta - 1) - alpha * (eDeltaSq - 4 * eDelta + 2 * delta + 3) / 2.) / (2 * beta4);
  }

  private double getPhi3(final boolean betaIsZero, final double variance, final double rho, final double alpha, final double t, final double beta, final double beta4, final double eDelta,
      final double delta) {
    if (betaIsZero) {
      return rho * rho * Math.pow(variance + alpha * t / 3., 2) * t * t * t * t / 8.;
    }
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

  double getF1(final boolean betaIsZero, final double variance, final double rho, final double alpha, final double t, final double beta, final double delta, final double eDelta, final double sDf,
      final double nD1, final double d2, final double meanVariance) {
    final double cSV = getCSV(sDf, nD1, d2, meanVariance);
    if (betaIsZero) {
      return rho * (variance + alpha * t / 3.) * t * cSV / 2.;
    }
    return rho * cSV * ((alpha + beta * variance) * (1 - eDelta + delta * eDelta) + alpha * (1 + delta - eDelta)) / (beta * beta * beta * t);
  }

  double getF2(final boolean betaIsZero, final double variance, final double rho, final double alpha, final double t, final double beta, final double delta, final double eDelta, final double sDf,
      final double nD1, final double d1, final double d2, final double meanVariance) {
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
