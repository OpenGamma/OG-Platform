/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NelderMeadDownhillSimplexMinimizerTest extends MultidimensionalMinimizerTestCase {

  private static final double EPS = 1e-8;
  final static NelderMeadDownhillSimplexMinimizer MINIMIZER = new NelderMeadDownhillSimplexMinimizer();

  @Test
  public void test() {

    super.assertInputs(MINIMIZER);
    super.assertMinimizer(MINIMIZER, EPS);
  }

  @Test
  public void testSolvingRosenbrock() {
    super.assertSolvingRosenbrock(MINIMIZER, EPS);
  }

  @Test
  public void testSolvingUncoupledRosenbrock() {
    super.assertSolvingUncoupledRosenbrock(MINIMIZER, EPS);
  }

  @Test
  public void testSolvingCoupledRosenbrock() {
    super.assertSolvingCoupledRosenbrock(MINIMIZER, EPS);
  }
}
