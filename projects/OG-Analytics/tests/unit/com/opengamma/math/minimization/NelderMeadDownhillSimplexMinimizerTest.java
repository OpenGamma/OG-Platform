/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import org.testng.annotations.Test;

public class NelderMeadDownhillSimplexMinimizerTest extends MultidimensionalMinimizerTestCase {

  private static final double EPS = 1e-8;
  final static SimplexMinimizer MINIMIZER = new NelderMeadDownhillSimplexMinimizer();

  @Test
  public void test() {

    super.testInputs(MINIMIZER);
    super.test(MINIMIZER, EPS);
  }

  @Test
  public void testSolvingRosenbrock() {
    super.testSolvingRosenbrock(MINIMIZER, EPS);
  }

  @Test
  public void testSolvingUncoupledRosenbrock() {
    super.testSolvingUncoupledRosenbrock(MINIMIZER, EPS);
  }

  @Test
  public void testSolvingCoupledRosenbrock() {
    super.testSolvingCoupledRosenbrock(MINIMIZER, EPS);
  }
}
