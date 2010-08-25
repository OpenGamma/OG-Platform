/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.junit.Test;

/**
 * 
 */
public class ConjugateGradientTest extends MultidimensionalMinimizerWithGradiantTestCase {
  private static double EPS = 1e-8;

  private static ScalarMinimizer LINE_MINIMIZER = new BrentMinimizer1D();
  private static VectorMinimizerWithGradient MINIMISER = new ConjugateGradientVectorMinimizer(LINE_MINIMIZER, EPS, 500);

  @Test
  public void testSolvingRosenbrock() {
    super.testSolvingRosenbrock(MINIMISER);
  }

  @Test
  public void testSolvingCoupledRosenbrock() {
    super.testSolvingCoupledRosenbrock(MINIMISER);
  }
}
