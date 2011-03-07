/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.apache.commons.math.MathException;
import org.apache.commons.math.special.Gamma;

import com.opengamma.math.function.special.GammaFunction;

/**
 * 
 */
public class NonCentralChiSquareDistribution implements ProbabilityDistribution<Double> {
  private final double _lambdaOverTwo;
  private final int _k;
  private final double _dofOverTwo;
  private final double _pStart;
  private final double _gammaStart;
  private final double _eps = 1e-16;

  public NonCentralChiSquareDistribution(final double degrees, final double nonCentrality) {
    Validate.isTrue(degrees > 0, "degrees of freedom must be > 0, have " + degrees);
    Validate.isTrue(nonCentrality >= 0, "non-centrality must be >= 0, have " + nonCentrality);
    _dofOverTwo = degrees / 2.0;
    _lambdaOverTwo = nonCentrality / 2.0;
    _k = (int) Math.round(_lambdaOverTwo);
    final double logP = -_lambdaOverTwo + _k * Math.log(_lambdaOverTwo) - Gamma.logGamma(_k + 1);
    _pStart = Math.exp(logP);
    final GammaFunction func = new GammaFunction();
    _gammaStart = func.evaluate(_dofOverTwo + _k);

  }

  //  private double getPenevAppoxCDF(double x) {
  //    double mu = _lambdaOverTwo / _dofOverTwo;
  //    double s = (Math.sqrt(1 + 8 * x * mu / _dofOverTwo) - 1) / 2 / mu;
  //    double h = (s * Math.log(s) + (1 - s) - 0.5 * (1 - s) * (1 - s)) / (1 - s) / (1 - s);
  //    double z = Math.signum(s - 1)
  //        * Math.sqrt(2 * _dofOverTwo * (s - 1) * (s - 1) * (0.5 / s + mu - h / s) - Math.log(1 / s - 2 * h / s / (1 + 2 * mu * s)) + 4 * square(1 + 3 * mu) / 9 / _dofOverTwo / cube(1 + 2 * mu));
  //
  //    return (new NormalDistribution(0, 1)).getCDF(z);
  //  }

  private double getFraserApproxCDF(final double x) {
    final double s = Math.sqrt(_lambdaOverTwo * 2.0);
    final double mu = Math.sqrt(x);
    double z;
    if (mu == s) {
      z = (1 - _dofOverTwo * 2.0) / 2 / s;
    } else {
      z = mu - s - (_dofOverTwo * 2.0 - 1) / 2 * (Math.log(mu) - Math.log(s)) / (mu - s);
    }
    return (new NormalDistribution(0, 1)).getCDF(z);
  }

  @Override
  public double getCDF(final Double x) {
    Validate.notNull(x, "x");
    if (x < 0) {
      return 0.0;
    }

    if ((_dofOverTwo + _lambdaOverTwo) > 10000) {
      return getFraserApproxCDF(x);
    }

    double regGammaStart = 0;
    final double halfX = x / 2.0;
    final double logX = Math.log(halfX);
    try {
      regGammaStart = Gamma.regularizedGammaP(_dofOverTwo + _k, halfX);
    } catch (final MathException ex) {
      // TODO Auto-generated catch block
      ex.printStackTrace();
    }

    double sum = _pStart * regGammaStart;
    double oldSum = Double.NEGATIVE_INFINITY;
    double p = _pStart;
    double gamma = _gammaStart;
    double regGamma = regGammaStart;
    double temp;
    int i = _k;

    // first add terms below _k
    while (i > 0 && Math.abs(sum - oldSum) > _eps) {
      i--;
      p *= (i + 1) / _lambdaOverTwo;
      // temp = (_dofOverTwo + i) * logX - halfX;
      // regGamma += Math.exp(temp) / gamma;
      temp = (_dofOverTwo + i) * logX - halfX - Gamma.logGamma(_dofOverTwo + i + 1);
      regGamma += Math.exp(temp);
      oldSum = sum;
      sum += p * regGamma;
      gamma /= _dofOverTwo + i;
    }

    p = _pStart;
    gamma = _gammaStart;
    regGamma = regGammaStart;
    oldSum = Double.NEGATIVE_INFINITY;
    i = _k;
    while (Math.abs(sum - oldSum) > _eps) {
      i++;
      p *= _lambdaOverTwo / i;
      gamma *= _dofOverTwo + i - 1;
      // temp = (_dofOverTwo + i - 1) * logX - halfX;
      // regGamma -= Math.exp(temp) / gamma;
      temp = (_dofOverTwo + i - 1) * logX - halfX - Gamma.logGamma(_dofOverTwo + i);
      regGamma -= Math.exp(temp);
      oldSum = sum;
      sum += p * regGamma;
    }

    return sum;
  }

  @Override
  public double getInverseCDF(final Double p) {
    throw new NotImplementedException();
  }

  @Override
  public double getPDF(final Double x) {
    throw new NotImplementedException();
  }

  @Override
  public double nextRandom() {
    throw new NotImplementedException();
  }

  public double getDegrees() {
    return _dofOverTwo * 2.0;
  }

  public double getNonCentrality() {
    return _lambdaOverTwo * 2.0;
  }

}
