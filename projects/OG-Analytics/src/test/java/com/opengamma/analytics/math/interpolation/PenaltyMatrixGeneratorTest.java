/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static com.opengamma.analytics.math.interpolation.PenaltyMatrixGenerator.getDiffMatrix;
import static com.opengamma.analytics.math.interpolation.PenaltyMatrixGenerator.getDifferanceMatrix;
import static com.opengamma.analytics.math.interpolation.PenaltyMatrixGenerator.getMatrixForFlattened;
import static com.opengamma.analytics.math.interpolation.PenaltyMatrixGenerator.getPenaltyMatrix;
import static org.testng.AssertJUnit.assertEquals;

import org.apache.commons.lang.NotImplementedException;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.IdentityMatrix;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.analytics.util.AssertMatrix;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class PenaltyMatrixGeneratorTest {
  private static final MatrixAlgebra MA = new OGMatrixAlgebra();

  @Test
  public void differanceMatrix1DTest() {
    final int n = 7;

    DoubleMatrix2D d0 = getDifferanceMatrix(n, 0); //zeroth order
    AssertMatrix.assertEqualsMatrix(new IdentityMatrix(n), d0, 1e-15);

    DoubleMatrix1D zeroVector = new DoubleMatrix1D(n);
    DoubleMatrix2D d1 = getDifferanceMatrix(n, 1); //first order difference matrix
    assertEquals(n, d1.getNumberOfRows());
    assertEquals(n, d1.getNumberOfColumns());
    AssertMatrix.assertEqualsVectors(zeroVector, d1.getRowVector(0), 1e-15); //first row should be zero

    final DoubleMatrix1D x = new DoubleMatrix1D(n, 1.0);
    DoubleMatrix1D d1x = (DoubleMatrix1D) MA.multiply(d1, x);
    //a constant vector should have zero first order differences 
    AssertMatrix.assertEqualsVectors(zeroVector, d1x, 1e-14);

    DoubleMatrix2D d2 = getDifferanceMatrix(n, 2); //second order difference matrix
    assertEquals(n, d2.getNumberOfRows());
    assertEquals(n, d2.getNumberOfColumns());
    AssertMatrix.assertEqualsVectors(zeroVector, d2.getRowVector(0), 1e-15); //first two rows should be zero
    AssertMatrix.assertEqualsVectors(zeroVector, d2.getRowVector(1), 1e-15);

    for (int i = 0; i < n; i++) {
      x.getData()[i] = i;
    }
    DoubleMatrix1D d2x = (DoubleMatrix1D) MA.multiply(d2, x);
    //a linear vector should have zero second order differences 
    AssertMatrix.assertEqualsVectors(zeroVector, d2x, 1e-14);

    DoubleMatrix2D d3 = getDifferanceMatrix(n, 3); //second order difference matrix
    assertEquals(n, d3.getNumberOfRows());
    assertEquals(n, d3.getNumberOfColumns());
    AssertMatrix.assertEqualsVectors(zeroVector, d3.getRowVector(0), 1e-15); //first three rows should be zero
    AssertMatrix.assertEqualsVectors(zeroVector, d3.getRowVector(1), 1e-15);
    AssertMatrix.assertEqualsVectors(zeroVector, d3.getRowVector(2), 1e-15);

    for (int i = 0; i < n; i++) {
      x.getData()[i] = 0.5 + i + 0.1 * i * i;
    }
    DoubleMatrix1D d3x = (DoubleMatrix1D) MA.multiply(d3, x);
    //a quadratic vector should have zero third order differences 
    AssertMatrix.assertEqualsVectors(zeroVector, d3x, 1e-14);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void diffOrderTooHighTest() {
    @SuppressWarnings("unused")
    DoubleMatrix2D d = getDifferanceMatrix(6, 6);
  }

  @Test
  public void penaltyMatrix1DTest() {
    final int n = 10;
    DoubleMatrix2D p0 = getPenaltyMatrix(n, 0); //zeroth order
    AssertMatrix.assertEqualsMatrix(new IdentityMatrix(n), p0, 1e-15);

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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void penaltyMatrixDiffOrderTooHighTest() {
    @SuppressWarnings("unused")
    DoubleMatrix2D p = getPenaltyMatrix(6, 10);
  }

  @Test
  public void penaltyMatrix2DTest() {

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
  public void penaltyMatrix3DTest() {

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

  @Test
  public void diffMatrix1DTest() {
    double[] x = new double[] {0.0, 0.3, 0.7, 0.8, 1.2, 2.0 };
    int n = x.length;

    DoubleMatrix2D d0 = getDiffMatrix(x, 0, true);
    AssertMatrix.assertEqualsMatrix(new IdentityMatrix(n), d0, 1e-14);

    DoubleMatrix1D y = new DoubleMatrix1D(n);
    DoubleMatrix1D dydx = new DoubleMatrix1D(n);
    DoubleMatrix1D d2ydx2 = new DoubleMatrix1D(n);
    for (int i = 0; i < n; i++) {
      double xi = x[i];
      y.getData()[i] = 0.3 + 0.7 * xi - 0.4 * xi * xi;
      dydx.getData()[i] = 0.7 - 0.8 * xi;
      d2ydx2.getData()[i] = -0.8;
    }

    DoubleMatrix2D d1 = getDiffMatrix(x, 1, true);
    DoubleMatrix2D d2 = getDiffMatrix(x, 2, true);
    DoubleMatrix1D d1y = (DoubleMatrix1D) MA.multiply(d1, y);
    DoubleMatrix1D d2y = (DoubleMatrix1D) MA.multiply(d2, y);
    AssertMatrix.assertEqualsVectors(dydx, d1y, 1e-13);
    AssertMatrix.assertEqualsVectors(d2ydx2, d2y, 1e-13);

    //also check penalty matrix
    DoubleMatrix2D p2 = getPenaltyMatrix(x, 2);
    double r = MA.getInnerProduct(y, MA.multiply(p2, y));
    double scale = Math.pow(2.0, 4);
    //this is the 2nd div squared ((-0.8)^2 = 0.64) times the number of elements (n-2 since we not not use the 
    //end points) times the scale ((2.0-0.0)^2^2)
    assertEquals(0.64 * (n - 2) * scale, r, 1e-11);
  }

  @Test(expectedExceptions = NotImplementedException.class)
  public void orderG2Test() {
    double[] x = new double[] {0.0, 0.3, 0.7, 0.8, 1.2, 2.0 };
    @SuppressWarnings("unused")
    DoubleMatrix2D d3 = getDiffMatrix(x, 3, true);
  }

  /**
   * The penalty matrix is scaled such that the result of x^T*P*x is insensitive to the scale of x 
   */
  @Test
  public void penaltyMatrixScaleTest() {
    double[] x = new double[] {0.0, 0.3, 0.7, 0.8, 1.2, 2.0 };
    double scale = 5.0; //scale the x-axis by a factor of 5
    int n = x.length;
    double[] xScaled = new double[n];

    DoubleMatrix1D y = new DoubleMatrix1D(n);
    for (int i = 0; i < n; i++) {
      double xi = x[i];
      y.getData()[i] = 0.3 + xi + Math.sin(xi);
      xScaled[i] = xi * scale;
    }

    //first order
    DoubleMatrix2D p1 = getPenaltyMatrix(x, 1);
    DoubleMatrix2D p1s = getPenaltyMatrix(xScaled, 1);
    double r = MA.getInnerProduct(y, MA.multiply(p1, y));
    double rs = MA.getInnerProduct(y, MA.multiply(p1s, y));
    assertEquals(r, rs, 1e-10);

    //second order
    DoubleMatrix2D p2 = getPenaltyMatrix(x, 2);
    DoubleMatrix2D p2s = getPenaltyMatrix(xScaled, 2);
    r = MA.getInnerProduct(y, MA.multiply(p2, y));
    rs = MA.getInnerProduct(y, MA.multiply(p2s, y));
    assertEquals(r, rs, 1e-10);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void xNotUniqueTest() {
    double[] x = new double[] {0.0, 0.3, 0.8, 0.8, 1.2, 2.0 };
    @SuppressWarnings("unused")
    DoubleMatrix2D p1 = getPenaltyMatrix(x, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void xNotAscendingTest() {
    double[] x = new double[] {0.0, 0.3, 0.8, 0.7, 1.2, 2.0 };
    @SuppressWarnings("unused")
    DoubleMatrix2D p1 = getPenaltyMatrix(x, 2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void emptyXTest() {
    @SuppressWarnings("unused")
    DoubleMatrix2D p1 = getPenaltyMatrix(new double[0], 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void negRangeTest() {
    double[] x = new double[] {0.0, -2.0 };
    @SuppressWarnings("unused")
    DoubleMatrix2D p1 = getPenaltyMatrix(x, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void singlePointTest() {
    double[] x = new double[] {0.2 };
    @SuppressWarnings("unused")
    DoubleMatrix2D p1 = getPenaltyMatrix(x, 1);
  }

  @Test
  public void zeroOrderSinglePointTest() {
    double[] x = new double[] {0.2 };
    DoubleMatrix2D p1 = getPenaltyMatrix(x, 0);
    AssertMatrix.assertEqualsMatrix(new IdentityMatrix(1), p1, 1e-15);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void twoPointsTest() {
    double[] x = new double[] {0.2, 0.5 };
    @SuppressWarnings("unused")
    DoubleMatrix2D d1 = getDiffMatrix(x, 1, true);
  }

  /**
   * create a quadratic function on a non-uniform 2D grid, then flatten this to a vector and check the first and
   * second order differentiation matrices and penalty matrices work in both dimensions    
   */
  @Test
  public void penalty2DTest() {
    double[] x = new double[] {0.0, 0.3, 0.7, 0.8, 1.2, 2.0 };
    double[] y = new double[] {-20.0, -10.0, 0.0, 5.0, 15.0, 19.0, 20.0 };
    int nx = x.length;
    int ny = y.length;

    DoubleMatrix2D p0 = getPenaltyMatrix(new double[][] {x, y }, 0, 0);
    AssertMatrix.assertEqualsMatrix(new IdentityMatrix(nx * ny), p0, 1e-14);
    p0 = getPenaltyMatrix(new double[][] {x, y }, 0, 1);
    AssertMatrix.assertEqualsMatrix(new IdentityMatrix(nx * ny), p0, 1e-14);

    DoubleMatrix2D diffX1DFirstOrder = getDiffMatrix(x, 1, true);
    DoubleMatrix2D diffY1DFirstOrder = getDiffMatrix(y, 1, true);
    DoubleMatrix2D diffX1DSecOrder = getDiffMatrix(x, 2, true);
    DoubleMatrix2D diffY1DSecOrder = getDiffMatrix(y, 2, true);
    DoubleMatrix2D diffX2DFirstOrder = getMatrixForFlattened(new int[] {nx, ny }, diffX1DFirstOrder, 0);
    DoubleMatrix2D diffY2DFirstOrder = getMatrixForFlattened(new int[] {nx, ny }, diffY1DFirstOrder, 1);
    DoubleMatrix2D diffX2DSecOrder = getMatrixForFlattened(new int[] {nx, ny }, diffX1DSecOrder, 0);
    DoubleMatrix2D diffY2DSecOrder = getMatrixForFlattened(new int[] {nx, ny }, diffY1DSecOrder, 1);

    DoubleMatrix1D z = new DoubleMatrix1D(nx * ny);
    DoubleMatrix1D dzdx = new DoubleMatrix1D(nx * ny);
    DoubleMatrix1D d2zdx2 = new DoubleMatrix1D(nx * ny);
    DoubleMatrix1D dzdy = new DoubleMatrix1D(nx * ny);
    DoubleMatrix1D d2zdy2 = new DoubleMatrix1D(nx * ny);
    double dzdxSum = 0;
    double d2zdx2Sum = 0;
    double dzdySum = 0;
    double d2zdy2Sum = 0;
    for (int i = 0; i < nx; i++) {
      double xi = x[i];
      for (int j = 0; j < ny; j++) {
        double yj = y[j];
        int index = i * ny + j;
        z.getData()[index] = 0.3 + xi + 0.4 * xi * xi + 0.01 * yj - 1e-4 * yj * yj + 0.1 * xi * yj;
        dzdx.getData()[index] = 1.0 + 0.8 * xi + 0.1 * yj;
        d2zdx2.getData()[index] = 0.8;
        dzdy.getData()[index] = 0.01 - 2e-4 * yj + 0.1 * xi;
        d2zdy2.getData()[index] = -2e-4;

        //The penalty matrix does not use end points, so don't include them here 
        if (i != 0 & i != (nx - 1)) {
          dzdxSum += FunctionUtils.square(dzdx.getData()[index]);
          d2zdx2Sum += FunctionUtils.square(d2zdx2.getData()[index]);
        }
        if (j != 0 & j != (ny - 1)) {
          dzdySum += FunctionUtils.square(dzdy.getData()[index]);
          d2zdy2Sum += FunctionUtils.square(d2zdy2.getData()[index]);
        }

      }
    }

    AssertMatrix.assertEqualsVectors(dzdx, (DoubleMatrix1D) MA.multiply(diffX2DFirstOrder, z), 1e-12);
    AssertMatrix.assertEqualsVectors(dzdy, (DoubleMatrix1D) MA.multiply(diffY2DFirstOrder, z), 1e-12);
    AssertMatrix.assertEqualsVectors(d2zdx2, (DoubleMatrix1D) MA.multiply(diffX2DSecOrder, z), 1e-12);
    AssertMatrix.assertEqualsVectors(d2zdy2, (DoubleMatrix1D) MA.multiply(diffY2DSecOrder, z), 1e-12);

    DoubleMatrix2D p1x = getPenaltyMatrix(new double[][] {x, y }, 1, 0);
    DoubleMatrix2D p2x = getPenaltyMatrix(new double[][] {x, y }, 2, 0);
    DoubleMatrix2D p1y = getPenaltyMatrix(new double[][] {x, y }, 1, 1);
    DoubleMatrix2D p2y = getPenaltyMatrix(new double[][] {x, y }, 2, 1);
    double r1x = MA.getInnerProduct(z, MA.multiply(p1x, z));
    double r2x = MA.getInnerProduct(z, MA.multiply(p2x, z));
    double r1y = MA.getInnerProduct(z, MA.multiply(p1y, z));
    double r2y = MA.getInnerProduct(z, MA.multiply(p2y, z));

    double xRange = x[nx - 1] - x[0];
    double yRange = y[ny - 1] - y[0];

    assertEquals("first order x", Math.pow(xRange, 2) * dzdxSum, r1x, 1e-10);
    assertEquals("second order x", Math.pow(xRange, 4) * d2zdx2Sum, r2x, 1e-9);
    assertEquals("first order y", Math.pow(yRange, 2) * dzdySum, r1y, 1e-10);
    assertEquals("second order y", Math.pow(yRange, 4) * d2zdy2Sum, r2y, 1e-8);

    double lambdaX = 0.7;
    double lambdaY = Math.PI;
    //second order in x and first order in y
    DoubleMatrix2D p = getPenaltyMatrix(new double[][] {x, y }, new int[] {2, 1 }, new double[] {lambdaX, lambdaY });
    double r = MA.getInnerProduct(z, MA.multiply(p, z));
    double expR = Math.pow(xRange, 4) * d2zdx2Sum * lambdaX + Math.pow(yRange, 2) * dzdySum * lambdaY;
    assertEquals(expR, r, 1e-9);
  }
}
