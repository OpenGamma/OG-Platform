/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;

/**
 * 
 */
public class HyperbolicMeshingTest {

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
}
