/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.math.MathException;
import org.apache.commons.math.special.Gamma;

import com.opengamma.math.function.special.GammaFunction;
import com.opengamma.util.ArgumentChecker;

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
    ArgumentChecker.notNegative(degrees, "degrees of freedom");
    ArgumentChecker.notNegative(nonCentrality, "non-centrality");
    _dofOverTwo = degrees / 2.0;
    _lambdaOverTwo = nonCentrality / 2.0;
    _k = (int) Math.round(_lambdaOverTwo);
    double logP = -_lambdaOverTwo + _k * Math.log(_lambdaOverTwo) - Gamma.logGamma(_k + 1);
    _pStart = Math.exp(logP);
    GammaFunction func = new GammaFunction();
    _gammaStart = func.evaluate(_dofOverTwo + _k);
  }

  @Override
  public double getCDF(Double x) {
    ArgumentChecker.notNull(x, "x");
    if (x < 0) {
      return 0.0;
    }

    double regGammaStart = 0;
    double halfX = x / 2.0;
    double logX = Math.log(halfX);
    try {
      regGammaStart = Gamma.regularizedGammaP(_dofOverTwo + _k, halfX);
    } catch (MathException ex) {
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
      temp = (_dofOverTwo + i) * logX - halfX;
      regGamma += Math.exp(temp) / gamma;
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
      temp = (_dofOverTwo + i - 1) * logX - halfX;
      regGamma -= Math.exp(temp) / gamma;
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
