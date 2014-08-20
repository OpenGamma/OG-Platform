/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;

/**
 * 
 */
public class PenaltyMatrixGeneratorTest {
  private static final MatrixAlgebra MA = new OGMatrixAlgebra();

  @Test
  public void oneDTest() {

    final int n = 10;
    //constant
    final DoubleMatrix1D x = new DoubleMatrix1D(n, 2.0);
    DoubleMatrix2D p = PenaltyMatrixGenerator.getPenaltyMatrix(n, 2);
    double r = MA.getInnerProduct(x, MA.multiply(p, x));
    assertEquals(0.0, r);

    final double[] data = x.getData();
    for (int i = 0; i < n; i++) {
      data[i] = i;
    }

    r = MA.getInnerProduct(x, MA.multiply(p, x));
    assertEquals(0.0, r);

    for (int i = 0; i < n; i++) {
      data[i] = 0.4 + 0.4 * i + i * i;
    }
    r = MA.getInnerProduct(x, MA.multiply(p, x));
    //The second order diff is 2; for 2nd order difference use 8 values (n-2), so expect 8 * 2^2 = 32
    assertEquals(32.0, r, 1e-11);

    p = PenaltyMatrixGenerator.getPenaltyMatrix(n, 3);
    r = MA.getInnerProduct(x, MA.multiply(p, x));
    assertEquals(0.0, r, 1e-13);
  }

  @Test
  public void twoDTest() {

    final int n1 = 8;
    final int n2 = 13;
    //constant
    DoubleMatrix1D x = new DoubleMatrix1D(n1 * n2, 2.0);
    DoubleMatrix2D p = PenaltyMatrixGenerator.getPenaltyMatrix(new int[] {n1, n2 }, 1, 0);
    double r = MA.getInnerProduct(x, MA.multiply(p, x));
    assertEquals(0.0, r);

    //viewed as an x-y grid, this is flat in the x direction 
    final double[][] data = new double[n1][n2];
    for (int i = 0; i < n1; i++) {
      for (int j = 0; j < n2; j++) {
        data[i][j] = 0.4 + j;
      }
    }
    x = PenaltyMatrixGenerator.flattenMatrix(new DoubleMatrix2D(data));
    r = MA.getInnerProduct(x, MA.multiply(p, x));
    assertEquals(0.0, r);

    p = PenaltyMatrixGenerator.getPenaltyMatrix(new int[] {n1, n2 }, 1, 1);
    r = MA.getInnerProduct(x, MA.multiply(p, x));
    //8*12
    assertEquals(96, r, 1e-12);

    for (int i = 0; i < n1; i++) {
      for (int j = 0; j < n2; j++) {
        x.getData()[i * n2 + j] = 0.4 + j - 0.5 * i * i + 3 * i * j;
      }
    }
    p = PenaltyMatrixGenerator.getPenaltyMatrix(new int[] {n1, n2 }, 2, 0);
    r = MA.getInnerProduct(x, MA.multiply(p, x));
    //6*13
    assertEquals(78, r, 1e-11);
    p = PenaltyMatrixGenerator.getPenaltyMatrix(new int[] {n1, n2 }, 3, 0);
    r = MA.getInnerProduct(x, MA.multiply(p, x));
    assertEquals(0, r, 2e-10);

    p = PenaltyMatrixGenerator.getPenaltyMatrix(new int[] {n1, n2 }, 2, 1);
    r = MA.getInnerProduct(x, MA.multiply(p, x));
    assertEquals(0, r, 2e-10);

    p = PenaltyMatrixGenerator.getPenaltyMatrix(new int[] {n1, n2 }, 1, 1);
    r = MA.getInnerProduct(x, MA.multiply(p, x));
    assertEquals(17232, r, 2e-10);

    p = PenaltyMatrixGenerator.getPenaltyMatrix(new int[] {n1, n2 }, new int[] {2, 1 }, new double[] {1 / 78.0, 1. / 17232. });
    r = MA.getInnerProduct(x, MA.multiply(p, x));
    assertEquals(2.0, r, 2e-10);
  }

  @Test
  public void threeDTest() {

    final int n1 = 5;
    final int n2 = 13;
    final int n3 = 4;

    //constant
    final DoubleMatrix1D x = new DoubleMatrix1D(n1 * n2 * n3, 2.0);
    DoubleMatrix2D p = PenaltyMatrixGenerator.getPenaltyMatrix(new int[] {n1, n2, n3 }, 1, 0);
    double r = MA.getInnerProduct(x, MA.multiply(p, x));
    assertEquals(0.0, r);
    p = PenaltyMatrixGenerator.getPenaltyMatrix(new int[] {n1, n2, n3 }, 1, 1);
    r = MA.getInnerProduct(x, MA.multiply(p, x));
    assertEquals(0.0, r);
    p = PenaltyMatrixGenerator.getPenaltyMatrix(new int[] {n1, n2, n3 }, 1, 2);
    r = MA.getInnerProduct(x, MA.multiply(p, x));
    assertEquals(0.0, r);

    final double[] data = x.getData();
    for (int i = 0; i < n1; i++) {
      for (int j = 0; j < n2; j++) {
        for (int k = 0; k < n3; k++) {
          data[i * n2 * n3 + j * n3 + k] = 0.4 + i - k + j * j - 3.0 * i * k + 4 * i * j;
        }
      }
    }
    p = PenaltyMatrixGenerator.getPenaltyMatrix(new int[] {n1, n2, n3 }, 2, 0);
    r = MA.getInnerProduct(x, MA.multiply(p, x));
    assertEquals(0.0, r, 1e-11);
    p = PenaltyMatrixGenerator.getPenaltyMatrix(new int[] {n1, n2, n3 }, 3, 1);
    r = MA.getInnerProduct(x, MA.multiply(p, x));
    assertEquals(0.0, r, 3e-10);
    p = PenaltyMatrixGenerator.getPenaltyMatrix(new int[] {n1, n2, n3 }, 2, 2);
    r = MA.getInnerProduct(x, MA.multiply(p, x));
    assertEquals(0.0, r, 5e-11);
    p = PenaltyMatrixGenerator.getPenaltyMatrix(new int[] {n1, n2, n3 }, 2, 1);
    r = MA.getInnerProduct(x, MA.multiply(p, x));
    //4*11*5*4
    assertEquals(880, r, 1e-9);
  }

  @Test(enabled = false)
  public void sanityTest() {
    final DoubleMatrix2D a = new DoubleMatrix2D(new double[][] { {10, 20 }, {30, 40 } });
    DoubleMatrix2D p = PenaltyMatrixGenerator.getMatrixForFlattened(new int[] {2, 3 }, a, 0);
    System.out.println(p);
    p = PenaltyMatrixGenerator.getMatrixForFlattened(new int[] {3, 2 }, a, 1);
    System.out.println(p);
  }
}
