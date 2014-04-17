/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.math.function.RealPolynomialFunction1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EigenvaluePolynomialRootFinderTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final Polynomial1DRootFinder<Double> FINDER = new EigenvaluePolynomialRootFinder();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    FINDER.getRoots(null);
  }

  @Test
  public void test() {
    final double[] r = new double[] {-RANDOM.nextDouble(), -RANDOM.nextDouble(), RANDOM.nextDouble(), RANDOM.nextDouble()};
    final double a0 = r[0] * r[1] * r[2] * r[3];
    final double a1 = r[0] * r[1] * r[2] + r[0] * r[1] * r[3] + r[0] * r[2] * r[3] + r[1] * r[2] * r[3];
    final double a2 = r[0] * r[1] + r[0] * r[2] + r[0] * r[3] + r[1] * r[2] + r[1] * r[3] + r[2] * r[3];
    final double a3 = r[0] + r[1] + r[2] + r[3];
    final double a4 = 1;
    final RealPolynomialFunction1D f = new RealPolynomialFunction1D(new double[] {a0, a1, a2, a3, a4});
    final Double[] roots = FINDER.getRoots(f);
    Arrays.sort(roots);
    final double[] expected = new double[r.length];
    for (int i = 0; i < r.length; i++) {
      expected[i] = -r[i];
    }
    Arrays.sort(expected);
    for (int i = 0; i < roots.length; i++) {
      assertEquals(roots[i], expected[i], 1e-12);
    }
  }
}
