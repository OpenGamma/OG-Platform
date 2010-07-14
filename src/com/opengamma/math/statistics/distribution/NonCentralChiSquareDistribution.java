/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.math.function.special.GammaFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class NonCentralChiSquareDistribution implements ProbabilityDistribution<Double> {
  //TODO better seed
  private final double _degrees;
  private final double _nonCentrality;
  private final double _tMultiplier;
  private final double _w0;
  private final double _eps = 1e-16;

  public NonCentralChiSquareDistribution(final double degrees, final double nonCentrality) {
    ArgumentChecker.notNegative(degrees, "degrees of freedom");
    ArgumentChecker.notNegative(nonCentrality, "non-centrality");
    _degrees = degrees;
    _nonCentrality = nonCentrality;
    _tMultiplier = 1. / new GammaFunction().evaluate(degrees / 2 + 1);
    _w0 = Math.exp(-nonCentrality / 2);
  }

  @Override
  public double getCDF(final Double x) {
    Validate.notNull(x, "x");
    if (x < 0) {
      return 0;
    }
    final double halfX = x / 2;
    double t = _tMultiplier * Math.pow(halfX, _degrees / 2) * Math.exp(-halfX);
    double w = _w0;
    double u = w;
    int twoI;
    double sum = w * t;
    double temp = Double.NEGATIVE_INFINITY;
    int i = 1;
    while (Math.abs(sum - temp) > _eps) {
      twoI = 2 * i;
      u = u * _nonCentrality / (twoI);
      w = w + u;
      t = t * x / (_degrees + twoI);
      temp = sum;
      sum += w * t;
      i++;
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
    return _degrees;
  }

  public double getNonCentrality() {
    return _nonCentrality;
  }

}
