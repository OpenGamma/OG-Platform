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
public class ConjugateDirectionTest extends MultidimensionalMinimizerTestCase {
  private static final double EPS = 1e-8;

  private static ScalarMinimizer LINE_MINIMIZER = new BrentMinimizer1D();
  private static ConjugateDirectionVectorMinimizer MINIMIZER = new ConjugateDirectionVectorMinimizer(LINE_MINIMIZER, EPS, 10000);

  @Test
  public void testSolvingRosenbrock() {
    super.assertSolvingRosenbrock(MINIMIZER, EPS);
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
    super.assertSolvingCoupledRosenbrock(MINIMIZER, 10 * EPS);
  }

}
