/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.CompareUtils;

/**
 * Given a model that returns the implied volatility of a European option as a function of strike (e.g. SABR), this find the implied 
 * terminal distribution  
 */
public class DistributionFromImpliedVolatility implements ProbabilityDistribution<Double> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final double EPS = 1e-2;
  private final double _f;
  private final double _rootT;
  private final Function1D<Double, Double> _volFunc;

  public DistributionFromImpliedVolatility(final double forward, final double maturity, final Function1D<Double, Double> impliedVolFunction) {
    Validate.isTrue(maturity > 0.0, "maturity <= 0");
    Validate.isTrue(forward > 0.0, "forward <= 0");
    Validate.notNull(impliedVolFunction, "implied vol function");
    _f = forward;
    _volFunc = impliedVolFunction;
    _rootT = Math.sqrt(maturity);
  }

  @Override
  public double getPDF(final Double x) {
    final double[] sigmas = getSigmas(x);
    final double d1 = getD1(x, sigmas[1] * _rootT);
    final double sigmaPrime = getSigmaPrime(sigmas, x);
    final double sigmaDoublePrime = getSigmaDoublePrime(sigmas, x);
    final double d1Prime = getD1Prime(x, sigmas[1], sigmaPrime);
    final double d2Prime = d1Prime - _rootT * sigmaPrime;
    return -_f * NORMAL.getPDF(d1) * (_rootT * (d1 * d1Prime * sigmaPrime - sigmaDoublePrime) + d2Prime / x);
  }

  @Override
  public double getCDF(final Double x) {
    final double[] sigmas = getSigmas(x);
    final double d1 = getD1(x, sigmas[1] * _rootT);
    final double d2 = d1 - sigmas[1] * _rootT;
    final double sigmaPrime = getSigmaPrime(sigmas, x);
    return NORMAL.getCDF(-d2) + _f * NORMAL.getPDF(d1) * sigmaPrime * _rootT;
  }

  @Override
  public double getInverseCDF(final Double p) {
    throw new NotImplementedException();
  }

  @Override
  public double nextRandom() {
    throw new NotImplementedException();
  }

  private double[] getSigmas(final double k) {
    final double[] res = new double[3];
    res[1] = _volFunc.evaluate(k);
    final double kUp = k * (1 + EPS);
    final double kDown = k * (1 - EPS);
    res[2] = _volFunc.evaluate(kUp);
    res[0] = _volFunc.evaluate(kDown);
    return res;
  }

  private double getSigmaPrime(final double[] sigmas, final double k) {
    return (sigmas[2] - sigmas[0]) / 2 / k / EPS;
  }

  private double getSigmaDoublePrime(final double[] sigmas, final double k) {
    return (sigmas[2] + sigmas[0] - 2 * sigmas[1]) / k / k / EPS / EPS;
  }

  private double getD1(final double k, final double sigmaRootT) {
    final double numerator = (Math.log(_f / k) + sigmaRootT * sigmaRootT / 2);
    if (CompareUtils.closeEquals(numerator, 0, 1e-16)) {
      return 0;
    }
    return numerator / sigmaRootT;
  }

  private double getD1Prime(final double k, final double sigma, final double sigmaPrime) {
    final double res = -1 / k / sigma / _rootT - (Math.log(_f / k) / sigma / sigma / _rootT - 0.5 * _rootT) * sigmaPrime;
    return res;
  }

}
