package com.opengamma.math.statistics.distribution;

import java.util.Date;

import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

/**
 * 
 * @author emcleod
 * 
 */

public class NormalProbabilityDistribution implements ProbabilityDistribution<Double> {
  // TODO need a better seed
  private RandomEngine _randomEngine = new MersenneTwister(new Date());
  private double _mean;
  private double _standardDeviation;
  private Normal _normal;

  public NormalProbabilityDistribution(double mean, double standardDeviation) {
    _mean = mean;
    _standardDeviation = standardDeviation;
    _normal = new Normal(mean, standardDeviation, _randomEngine);
  }

  public NormalProbabilityDistribution(double mean, double standardDeviation, RandomEngine randomEngine) {
    _mean = mean;
    _standardDeviation = standardDeviation;
    _normal = new Normal(mean, standardDeviation, randomEngine);
  }

  @Override
  public double getCDF(Double x) {
    return _normal.cdf(x);
  }

  @Override
  public double getPDF(Double x) {
    return _normal.pdf(x);
  }

  @Override
  public double nextRandom() {
    return _normal.nextDouble();
  }

  public double getMean() {
    return _mean;
  }

  public double getStandardDeviation() {
    return _standardDeviation;
  }
}
