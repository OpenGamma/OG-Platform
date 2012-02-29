/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.linearalgebra.DecompositionFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.MatrixAlgebra;
import com.opengamma.math.matrix.OGMatrixAlgebra;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.statistics.leastsquare.NonLinearLeastSquareDebug;

/**
 * 
 */
public class SumToOneTest {
  private static final MatrixAlgebra MA = new OGMatrixAlgebra();
  private static final NonLinearLeastSquareDebug SOLVER = new NonLinearLeastSquareDebug(DecompositionFactory.SV_COLT, MA, 1e-9);

  @Test
  public void setTest() {
    int n = 7;
    int[][] sets = SumToOne.getSet(n);
    assertEquals(n, sets.length);
  }

  @Test
  public void setTest2() {
    int n = 13;
    int[][] sets = SumToOne.getSet(n);
    assertEquals(n, sets.length);
  }

  @Test
  public void transformTest() {
    for (int n = 2; n < 13; n++) {
      double[] from = new double[n - 1];
      for (int j = 0; j < n - 1; j++) {
        from[j] = Math.random() * Math.PI / 2;
      }
      SumToOne trans = new SumToOne(n);
      DoubleMatrix1D to = trans.transform(new DoubleMatrix1D(from));
      assertEquals(n, to.getNumberOfElements());
      double sum = 0;
      for (int i = 0; i < n; i++) {
        sum += to.getEntry(i);
      }
      assertEquals("vector length " + n, 1.0, sum, 1e-9);
    }
  }

  @Test
  public void inverseTransformTest() {
    for (int n = 2; n < 13; n++) {
      double[] theta = new double[n - 1];
      for (int j = 0; j < n - 1; j++) {
        theta[j] = Math.random() * Math.PI / 2;
      }
      SumToOne trans = new SumToOne(n);
      DoubleMatrix1D w = trans.transform(new DoubleMatrix1D(theta));

      DoubleMatrix1D theta2 = trans.inverseTransform(w);
      for (int j = 0; j < n - 1; j++) {
        assertEquals("element " + j + ", of vector length " + n, theta[j], theta2.getEntry(j), 1e-9);
      }
    }
  }

  @Test
  public void solverTest() {
    double[] w = new double[] {0.01, 0.5, 0.3, 0.19 };
    final int n = w.length;
    final SumToOne trans = new SumToOne(n);
    Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @Override
      public DoubleMatrix1D evaluate(DoubleMatrix1D theta) {
        return trans.transform(theta);
      }
    };
    double[] temp = new double[n];
    Arrays.fill(temp, 1e-5);
    DoubleMatrix1D start = new DoubleMatrix1D(new double[] {0.02, 0.02, 0.02 });
    DoubleMatrix1D sigma = new DoubleMatrix1D(temp);
    LeastSquareResults res = SOLVER.solve(new DoubleMatrix1D(w), sigma, func, start);
    assertEquals("chi sqr", 0.0, res.getChiSq(), 1e-9);
    double[] fit = res.getFitParameters().getData();
    double[] expected = trans.inverseTransform(w);
    for (int i = 0; i < n - 1; i++) {
      //put the fit result back in the range 0 - pi/2
      double x = fit[i];
      if (x > Math.PI / 2) {
        int p = (int) (x / Math.PI) + 1;
        x -= p * Math.PI;
      }
      if (x < 0.0) {
        if (x > -Math.PI / 2) {
          x = -x;
        } else {
          int p = (int) (-x / Math.PI) + 1;
          x += p * Math.PI;
        }
      }

      assertEquals(expected[i], x, 1e-9);
    }

  }

}
