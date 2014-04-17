/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MeshingTest {

  protected void testMesh(final MeshingFunction mesh, final double[] fixedPoints) {
    testMesh(mesh, fixedPoints, 0.0, 1.0);
  }

  protected void testMesh(final MeshingFunction mesh, final double[] fixedPoints, final double l, final double r) {

    final int n = mesh.getNumberOfPoints();
    final double[] y = new double[n];
    for (int ii = 0; ii < n; ii++) {
      y[ii] = mesh.evaluate(ii);
    }

    assertEquals(l, y[0], 1e-18);
    assertEquals(r, y[n - 1], 1e-18);
    for (int ii = 1; ii < n; ii++) {
      assertTrue("points are not assending", y[ii] > y[ii - 1]);
    }

    if (fixedPoints != null) {
      final int m = fixedPoints.length;
      for (int ii = 0; ii < m; ii++) {
        int index = Arrays.binarySearch(y, fixedPoints[ii]);
        assertTrue("fixed point not found", index > 0 && index < n - 1);
      }
    }
  }

}
