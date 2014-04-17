/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding.newton;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.linearalgebra.SVDecompositionColt;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class JacobianDirectionFunctionTest {
  private static final SVDecompositionColt SV = new SVDecompositionColt();
  private static final JacobianDirectionFunction F = new JacobianDirectionFunction(SV);
  private static final double X0 = 2.4;
  private static final double X1 = 7.6;
  private static final double X2 = 4.5;
  private static final DoubleMatrix2D M = new DoubleMatrix2D(new double[][] {new double[] {X0, 0, 0}, new double[] {0, X1, 0}, new double[] {0, 0, X2}});
  private static final DoubleMatrix1D Y = new DoubleMatrix1D(new double[] {1, 1, 1});

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    new JacobianDirectionFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEstimate() {
    F.getDirection(null, Y);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullY() {
    F.getDirection(M, null);
  }

  @Test
  public void test() {
    double eps = 1e-9;
    DoubleMatrix1D direction = F.getDirection(M, Y);
    assertEquals(direction.getEntry(0), 1. / X0, eps);
    assertEquals(direction.getEntry(1), 1. / X1, eps);
    assertEquals(direction.getEntry(2), 1. / X2, eps);
  }
}
