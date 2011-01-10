/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.junit.Test;

/**
 * 
 */
public class ConjugateDirectionTest extends MultidimensionalMinimizerTestCase {
  private static final double EPS = 1e-8;

  private static ScalarMinimizer LINE_MINIMIZER = new BrentMinimizer1D();
  private static VectorMinimizer MINIMIZER = new ConjugateDirectionVectorMinimizer(LINE_MINIMIZER, EPS, 10000);

  @Test
  public void testSolvingRosenbrock() {
    super.testSolvingRosenbrock(MINIMIZER, EPS);
  }

  /**
   * This needs 100000 iterations to converge 
   */
  //  @Test
  //  public void testSolvingUncoupledRosenbrock() {
  //    super.testSolvingUncoupledRosenbrock(MINIMIZER, 10 * EPS);
  //  }

  @Test
  public void testSolvingCoupledRosenbrock() {
    super.testSolvingCoupledRosenbrock(MINIMIZER, 10 * EPS);
  }

}
