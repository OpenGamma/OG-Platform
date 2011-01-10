/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
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

  public DistributionFromImpliedVolatility(final double forward, final double maturity, Function1D<Double, Double> impliedVolFunction) {
    Validate.isTrue(maturity > 0.0, "maturity <= 0");
    Validate.isTrue(forward > 0.0, "forward <= 0");
    Validate.notNull(impliedVolFunction, "implied vol null");
    _f = forward;
    _volFunc = impliedVolFunction;
    _rootT = Math.sqrt(maturity);
  }

  @Override
  public double getPDF(Double x) {

    double[] sigmas = getSigmas(x);

    double d1 = getD1(x, sigmas[1] * _rootT);

    double sigmaPrime = sigmaPrime(sigmas, x);
    double sigmaDoublePrime = sigmaDoublePrime(sigmas, x);
    double d1Prime = getD1Prime(x, sigmas[1], sigmaPrime);
    double d2Prime = d1Prime - _rootT * sigmaPrime;
    return -_f * NORMAL.getPDF(d1) * (_rootT * (d1 * d1Prime * sigmaPrime - sigmaDoublePrime) + d2Prime / x);
  }

  @Override
  public double getCDF(Double x) {
    double[] sigmas = getSigmas(x);
    double d1 = getD1(x, sigmas[1] * _rootT);
    double d2 = d1 - sigmas[1] * _rootT;
    double sigmaPrime = sigmaPrime(sigmas, x);
    return NORMAL.getCDF(-d2) + _f * NORMAL.getPDF(d1) * sigmaPrime * _rootT;
  }

  @Override
  public double getInverseCDF(Double p) {
    throw new NotImplementedException();
  }

  @Override
  public double nextRandom() {
    throw new NotImplementedException();
  }

  private double[] getSigmas(final double k) {
    double[] res = new double[3];
    res[1] = _volFunc.evaluate(k);
    double kUp = k * (1 + EPS);
    double kDown = k * (1 - EPS);
    res[2] = _volFunc.evaluate(kUp);
    res[0] = _volFunc.evaluate(kDown);
    return res;
  }

  private double sigmaPrime(final double[] sigmas, final double k) {
    return (sigmas[2] - sigmas[0]) / 2 / k / EPS;
  }

  private double sigmaDoublePrime(final double[] sigmas, final double k) {
    return (sigmas[2] + sigmas[0] - 2 * sigmas[1]) / k / k / EPS / EPS;
  }

  private double getD1(final double k, final double simgaRootT) {
    final double numerator = (Math.log(_f / k) + simgaRootT * simgaRootT / 2);
    if (CompareUtils.closeEquals(numerator, 0, 1e-16)) {
      return 0;
    }
    return numerator / simgaRootT;
  }

  private double getD1Prime(final double k, final double sigma, final double sigmaPrime) {

    double res = -1 / k / sigma / _rootT - (Math.log(_f / k) / sigma / sigma / _rootT - 0.5 * _rootT) * sigmaPrime;
    return res;
  }

}
