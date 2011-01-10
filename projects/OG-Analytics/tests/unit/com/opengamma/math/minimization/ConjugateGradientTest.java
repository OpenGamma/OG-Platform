/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
  private static VectorMinimizerWithGradient MINIMISER = new ConjugateGradientVectorMinimizer(LINE_MINIMIZER, EPS, 100);

  @Test
  public void testSolvingRosenbrock() {
    super.testSolvingRosenbrock(MINIMISER, EPS);
  }

  @Test
  public void testSolvingRosenbrockWithoutGradient() {
    super.testSolvingRosenbrockWithoutGradient(MINIMISER, 10 * EPS);
  }

  @Test
  public void testSolvingCoupledRosenbrock() {
    super.testSolvingCoupledRosenbrock(MINIMISER, EPS);
  }

  @Test
  public void testSolvingCoupledRosenbrockWithoutGradient() {
    super.testSolvingCoupledRosenbrockWithoutGradient(MINIMISER, 100 * EPS);
  }
}
