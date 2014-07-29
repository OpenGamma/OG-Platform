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
public class ConjugateGradientTest extends MultidimensionalMinimizerWithGradientTestCase {
  private static double EPS = 1e-8;

  private static ScalarMinimizer LINE_MINIMIZER = new BrentMinimizer1D();
  private static ConjugateGradientVectorMinimizer MINIMISER = new ConjugateGradientVectorMinimizer(LINE_MINIMIZER, EPS, 100);

  @Test
  public void testSolvingRosenbrock() {
    super.assertSolvingRosenbrock(MINIMISER, EPS);
  }

  @Test
  public void testSolvingRosenbrockWithoutGradient() {
    super.assertSolvingRosenbrockWithoutGradient(MINIMISER, 10 * EPS);
  }

  @Test
  public void testSolvingCoupledRosenbrock() {
    super.assertSolvingCoupledRosenbrock(MINIMISER, EPS);
  }

  @Test
  public void testSolvingCoupledRosenbrockWithoutGradient() {
    super.assertSolvingCoupledRosenbrockWithoutGradient(MINIMISER, 100 * EPS);
  }
}
