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
public class QuasiNewtonTest extends MultidimensionalMinimizerWithGradientTestCase {
  private static final QuasiNewtonInverseHessianUpdate BFGS = new BroydenFletcherGoldfarbShannoInverseHessianUpdate();
  private static final QuasiNewtonInverseHessianUpdate DFP = new DavidonFletcherPowellInverseHessianUpdate();
  private static final double EPS = 1e-8;

  @Test
  public void solvingRosenbrockTest() {
    super.assertSolvingRosenbrock(new QuasiNewtonVectorMinimizer(EPS, EPS, 100, DFP), EPS);
    super.assertSolvingRosenbrock(new QuasiNewtonVectorMinimizer(EPS, EPS, 200, BFGS), EPS);
  }

  //Quasi Newton fails to solve Rosenbrock when finite difference gradients are used - small errors seem to build up in the inverse Hessian estimate 
  // @Test
  // public void solvingRosenbrockTestWithoutGradient() {
  //   super.testSolvingRosenbrockWithoutGradient(new QuasiNewtonVectorMinimizer(EPS, EPS, 100, DFP), EPS);
  //  super.testSolvingRosenbrockWithoutGradient(new QuasiNewtonVectorMinimizer(EPS, EPS, 500, BFGS), EPS);
  // }

  @Test
  public void solvingCoupledRosenbrockTest() {
    super.assertSolvingCoupledRosenbrock(new QuasiNewtonVectorMinimizer(EPS, EPS, 1000, DFP), EPS);
    super.assertSolvingCoupledRosenbrock(new QuasiNewtonVectorMinimizer(EPS, EPS, 1000, BFGS), EPS);
  }

}
