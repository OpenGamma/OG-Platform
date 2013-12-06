/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

/**
 * Test.
 */
public class UniformMeshingTest extends MeshingTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void outOfRangeTest() {
    int n = 10;
    double[] fixedPoint = new double[] {-0.145, 0.2, 0.25, 0.7 };
    @SuppressWarnings("unused")
    MeshingFunction mesh = new UniformMeshing(n, fixedPoint);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void outOfRangeTest2() {
    int n = 10;
    double[] fixedPoint = new double[] {0.145, 1.2, 0.25, 0.7 };
    @SuppressWarnings("unused")
    MeshingFunction mesh = new UniformMeshing(n, fixedPoint);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void notEnoughPointsTest() {
    int n = 7;
    double[] fixedPoint = new double[] {0.145, 0.2, 0.9, 0.8, 0.25, 0.7 };
    @SuppressWarnings("unused")
    MeshingFunction mesh = new UniformMeshing(n, fixedPoint);
  }

  @Test
  public void noFixedPointsTest() {
    int n = 7;
    MeshingFunction mesh = new UniformMeshing(n);
    for (int ii = 0; ii < n; ii++) {
      assertEquals(((double) ii) / (n - 1), mesh.evaluate(ii), 1e-15);
    }
  }

  @Test
  public void singleFixedPointsTest() {
    int n = 10;
    double[] fixedPoint = new double[] {0.3142 };
    MeshingFunction mesh = new UniformMeshing(n, fixedPoint);
    testMesh(mesh, fixedPoint);
  }

  @Test
  public void multiFixedPointsTest() {
    int n = 10;
    double[] fixedPoint = new double[] {0.145, 0.2, 0.25, 0.7 };
    MeshingFunction mesh = new UniformMeshing(n, fixedPoint);
    testMesh(mesh, fixedPoint);
  }

  @Test
  public void multiFixedPointsTest2() {
    int n = 13;
    double[] fixedPoint = new double[] {0.145, 0.2, 0.25, 0.7, 0.19, 0.2 };
    MeshingFunction mesh = new UniformMeshing(n, fixedPoint);
    testMesh(mesh, fixedPoint);
  }

}
