/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import static com.opengamma.analytics.math.linearalgebra.TridiagonalSolver.solvTriDag;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TridiagonalSolverTest {

  private static MatrixAlgebra MA = new OGMatrixAlgebra();
  private static ProbabilityDistribution<Double> RANDOM = new NormalDistribution(0, 1, new MersenneTwister(123));

  @Test
  public void test() {
    final int n = 97;
    double[] a = new double[n - 1];
    double[] b = new double[n];
    double[] c = new double[n - 1];
    double[] x = new double[n];

    for (int ii = 0; ii < n; ii++) {
      b[ii] = RANDOM.nextRandom();
      x[ii] = RANDOM.nextRandom();
      if (ii < n - 1) {
        a[ii] = RANDOM.nextRandom();
        c[ii] = RANDOM.nextRandom();
      }
    }

    final TridiagonalMatrix m = new TridiagonalMatrix(b, a, c);
    final DoubleMatrix1D xVec = new DoubleMatrix1D(x);
    final DoubleMatrix1D yVec = (DoubleMatrix1D) MA.multiply(m, xVec);

    final double[] xSolv = solvTriDag(m, yVec).getData();

    for (int i = 0; i < n; i++) {
      assertEquals(x[i], xSolv[i], 1e-9);
    }

    DoubleMatrix1D resi = (DoubleMatrix1D) MA.subtract(MA.multiply(m, new DoubleMatrix1D(xSolv)), yVec);
    double err = MA.getNorm2(resi);
    assertEquals(0.0, err, 1e-14);

  }

}
