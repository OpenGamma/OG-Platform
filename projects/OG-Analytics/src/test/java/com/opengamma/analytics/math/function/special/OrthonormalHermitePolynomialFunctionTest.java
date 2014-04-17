/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function.special;

import static org.testng.AssertJUnit.assertEquals;

import org.apache.commons.math.util.MathUtils;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class OrthonormalHermitePolynomialFunctionTest {
  private static final HermitePolynomialFunction HERMITE = new HermitePolynomialFunction();
  private static final OrthonormalHermitePolynomialFunction ORTHONORMAL = new OrthonormalHermitePolynomialFunction();
  private static final double EPS = 1e-9;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadN() {
    ORTHONORMAL.getPolynomials(-3);
  }

  @Test
  public void test() {
    final int n = 15;
    final DoubleFunction1D[] f1 = HERMITE.getPolynomials(n);
    final DoubleFunction1D[] f2 = ORTHONORMAL.getPolynomials(n);
    final double x = 3.4;
    for (int i = 0; i < f1.length; i++) {
      assertEquals(f1[i].evaluate(x) / Math.sqrt(MathUtils.factorialDouble(i) * Math.pow(2, i) * Math.sqrt(Math.PI)), f2[i].evaluate(x), EPS);
    }
  }

}
