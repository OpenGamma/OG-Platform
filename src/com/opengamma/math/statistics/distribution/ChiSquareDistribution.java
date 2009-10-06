/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.distribution;

import cern.jet.random.ChiSquare;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function2D;
import com.opengamma.math.function.special.InverseIncompleteGammaFunction;

/**
 * 
 * @author emcleod
 */
public class ChiSquareDistribution implements ProbabilityDistribution<Double> {
  // TODO need a better seed
  private final RandomEngine _engine = new MersenneTwister(0);
  private final Function2D<Double, Double> _inverseFunction = new InverseIncompleteGammaFunction();
  private final ChiSquare _chiSquare;
  private final double _degrees;

  public ChiSquareDistribution(final double degrees) {
    _chiSquare = new ChiSquare(degrees, _engine);
    _degrees = degrees;
  }

  @Override
  public double getCDF(final Double x) {
    return _chiSquare.cdf(x);
  }

  @Override
  public double getPDF(final Double x) {
    return _chiSquare.pdf(x);
  }

  @Override
  public double getInverseCDF(final Double p) {
    if (p < 0 || p >= 1)
      throw new IllegalArgumentException("Probability must lie between 0 and 1: have " + p);
    return 2 * _inverseFunction.evaluate(0.5 * _degrees, p);
  }

  @Override
  public double nextRandom() {
    return _chiSquare.nextDouble();
  }
}
