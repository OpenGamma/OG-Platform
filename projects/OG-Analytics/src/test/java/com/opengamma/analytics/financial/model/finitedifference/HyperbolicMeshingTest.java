/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class HyperbolicMeshingTest extends MeshingTest {

  private static final double A = 1.2;
  private static final double B = 5.1;
  private static final double K = 2.0;
  private static final int N = 21;
  private static final double BETA = 0.1;

  private static final MeshingFunction HYP_MESH = new HyperbolicMeshing(A, B, K, N, BETA);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEqualPoints() {
    @SuppressWarnings("unused")
    final MeshingFunction mesh = new HyperbolicMeshing(A, A, K, N, BETA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoPoints() {
    @SuppressWarnings("unused")
    final MeshingFunction mesh = new HyperbolicMeshing(A, B, K, 1, BETA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroBeta() {
    @SuppressWarnings("unused")
    final MeshingFunction mesh = new HyperbolicMeshing(A, B, K, N, 0.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testKLow() {
    @SuppressWarnings("unused")
    final MeshingFunction mesh = new HyperbolicMeshing(A, B, A - 0.1, N, 0.0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testKHight() {
    @SuppressWarnings("unused")
    final MeshingFunction mesh = new HyperbolicMeshing(A, B, B + 0.1, N, 0.0);
  }

  @Test
  public void testEndPoints() {
    assertEquals(A, HYP_MESH.evaluate(0), 1e-10);
    assertEquals(B, HYP_MESH.evaluate(N - 1), 1e-10);
  }

  @Test
  public void testSpacing() {
    double dx, oldDx;
    double x = A;
    oldDx = HYP_MESH.evaluate(1) - HYP_MESH.evaluate(0);
    int i = 2;
    while (x < K) {
      dx = HYP_MESH.evaluate(i) - HYP_MESH.evaluate(i - 1);
      assertTrue(dx < oldDx);
      oldDx = dx;
      i++;
      x = HYP_MESH.evaluate(i);
    }
    x = B;
    oldDx = HYP_MESH.evaluate(N - 1) - HYP_MESH.evaluate(N - 2);
    i = N - 2;
    while (x > K) {
      dx = HYP_MESH.evaluate(i) - HYP_MESH.evaluate(i - 1);
      assertTrue(dx < oldDx);
      oldDx = dx;
      i--;
      x = HYP_MESH.evaluate(i);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void outOfRangeTest() {
    double xMin = -3.4;
    double xMax = 10.0;
    double xCent = 0.0;
    double[] fp = new double[] {5.0, -3.5, -2.0, 7.0 };
    double beta = 0.2;
    int n = 12;

    @SuppressWarnings("unused")
    MeshingFunction mesh = new HyperbolicMeshing(xMin, xMax, xCent, n, beta, fp);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void outOfRangeTest2() {
    double xMin = -3.4;
    double xMax = 10.0;
    double xCent = -2.0;
    double[] fp = new double[] {5.0, -1.4, -2.0, 17.0 };
    double beta = 0.2;
    int n = 12;

    @SuppressWarnings("unused")
    MeshingFunction mesh = new HyperbolicMeshing(xMin, xMax, xCent, n, beta, fp);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void tooFewPointsTest() {
    double xMin = -3.4;
    double xMax = 10.0;
    double xCent = 2.0;
    double[] fp = new double[] {5.0, -1.4, 1.4, 2.0, -2.0, 9.0 };
    double beta = 0.5;
    int n = 7;

    @SuppressWarnings("unused")
    MeshingFunction mesh = new HyperbolicMeshing(xMin, xMax, xCent, n, beta, fp);
  }

  @Test
  public void oneFixedPointTest() {
    double xMin = 3.4;
    double xMax = 10.0;
    double xCent = 3.9;
    double[] fp = new double[] {xCent };
    double beta = 0.2;
    int n = 10;

    MeshingFunction mesh = new HyperbolicMeshing(xMin, xMax, xCent, n, beta, fp);
    testMesh(mesh, fp, xMin, xMax);
  }

  @Test
  public void multiFixedPointTest() {
    double xMin = -3.4;
    double xMax = 10.0;
    double xCent = 5.0;
    double[] fp = new double[] {5.0, -1.0, -2.0, 7.0 };
    double beta = 0.7;
    int n = 12;

    MeshingFunction mesh = new HyperbolicMeshing(xMin, xMax, xCent, n, beta, fp);
    testMesh(mesh, fp, xMin, xMax);
  }

  @Test
  public void multiFixedPointTest2() {
    double xMin = 3.4;
    double xMax = 10.0;
    double xCent = 4.0;
    double[] fp = new double[] {3.5, 9.0, 9.9, 9.5, 9.9, 9.4 };
    double beta = 0.1;
    int n = 12;

    MeshingFunction mesh = new HyperbolicMeshing(xMin, xMax, xCent, n, beta, fp);
    testMesh(mesh, fp, xMin, xMax);
  }
}
