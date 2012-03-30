/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;

/**
 * 
 */
public class ExponentalMeshingTest {

  private static final double A = 1.2;
  private static final double B = 5.1;
  private static final int N = 21;
  private static final double LAMBDA = 0.7;

  private static final MeshingFunction EXP_MESH = new ExponentialMeshing(A, B, N, LAMBDA);
  private static final MeshingFunction LINEAR_MESH = new ExponentialMeshing(A, B, N, 0.0);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEqualPoints() {
    @SuppressWarnings("unused")
    final MeshingFunction mesh = new ExponentialMeshing(A, A, N, LAMBDA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoPoints() {
    @SuppressWarnings("unused")
    final MeshingFunction mesh = new ExponentialMeshing(A, B, 1, LAMBDA);
  }

  @Test
  public void testEndPoints() {
    assertEquals(A, EXP_MESH.evaluate(0), 1e-10);
    assertEquals(B, EXP_MESH.evaluate(N - 1), 1e-10);
    assertEquals(A, LINEAR_MESH.evaluate(0), 1e-10);
    assertEquals(B, LINEAR_MESH.evaluate(N - 1), 1e-10);

  }

  @Test
  public void testSpacing() {
    double dx, oldDx;
    oldDx = EXP_MESH.evaluate(1) - EXP_MESH.evaluate(0);
    for (int i = 2; i < N; i++) {
      dx = EXP_MESH.evaluate(i) - EXP_MESH.evaluate(i - 1);
      assertTrue(dx > oldDx);
      oldDx = dx;
    }
    oldDx = LINEAR_MESH.evaluate(1) - LINEAR_MESH.evaluate(0);
    for (int i = 2; i < N; i++) {
      dx = LINEAR_MESH.evaluate(i) - LINEAR_MESH.evaluate(i - 1);
      assertEquals(oldDx, dx, 1e-10);

    }
  }
}
