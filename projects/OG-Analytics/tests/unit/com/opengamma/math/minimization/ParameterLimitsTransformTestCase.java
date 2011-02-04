/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import static org.junit.Assert.assertEquals;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

/**
 * 
 */
public class ParameterLimitsTransformTestCase {

  protected static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  protected static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1, RANDOM);

  public void testRoundTrip(ParameterLimitsTransform transform, double modelParam) {
    double fp = transform.transform(modelParam);
    double mp = transform.inverseTransform(fp);
    assertEquals(modelParam, mp, 1e-8);
  }

  // reverse
  public void testReverseRoundTrip(ParameterLimitsTransform transform, double fitParam) {
    double mp = transform.inverseTransform(fitParam);
    double fp = transform.transform(mp);
    assertEquals(fitParam, fp, 1e-8);
  }

  public void testGradientRoundTrip(ParameterLimitsTransform transform, double modelParam) {
    double g = transform.transformGradient(modelParam);
    double fp = transform.transform(modelParam);
    double gInv = transform.inverseTransformGradient(fp);
    assertEquals(g, 1.0 / gInv, 1e-8);
  }

  public void testGradient(ParameterLimitsTransform transform, double modelParam) {
    double eps = 1e-5;
    double g = transform.transformGradient(modelParam);
    double fdg;
    try {
      double down = transform.transform(modelParam - eps);
      double up = transform.transform(modelParam + eps);
      fdg = (up - down) / 2 / eps;
    } catch (IllegalArgumentException e) {
      double fp = transform.transform(modelParam);
      try {
        double up = transform.transform(modelParam + eps);
        fdg = (up - fp) / eps;
      } catch (IllegalArgumentException e2) {
        double down = transform.transform(modelParam - eps);
        fdg = (fp - down) / eps;
      }
    }
    assertEquals(g, fdg, 1e-6);
  }

  public void testInverseGradient(ParameterLimitsTransform transform, double fitParam) {
    double eps = 1e-5;
    double g = transform.inverseTransformGradient(fitParam);
    double fdg;

    double down = transform.inverseTransform(fitParam - eps);
    double up = transform.inverseTransform(fitParam + eps);
    fdg = (up - down) / 2 / eps;

    assertEquals(g, fdg, 1e-6);
  }

}
