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
public class BrentMinimizer1DTest extends Minimizer1DTestCase {
  private static final ScalarMinimizer MINIMIZER = new BrentMinimizer1D();
//  private static final Function1D<Double, Double> NO_MIN = new Function1D<Double, Double>() {
//
//    @Override
//    public Double evaluate(final Double x) {
//      return x;
//    }
//
//  };
//  private static final Function1D<Double, Double> NON_NEGATIVE = new Function1D<Double, Double>() {
//
//    @Override
//    public Double evaluate(final Double x) {
//      return Math.sqrt(x);
//    }
//
//  };

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction1() {
    MINIMIZER.minimize(null, 2., 1., 4.);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction2() {
    MINIMIZER.minimize(null, 2.);
  }

  @Test
  public void test() {
    super.assertInputs(MINIMIZER);
    super.assertMinimizer(MINIMIZER);
  }
}
