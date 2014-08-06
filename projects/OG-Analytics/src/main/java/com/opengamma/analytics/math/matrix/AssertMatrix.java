/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.matrix;

import static org.testng.AssertJUnit.assertEquals;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public abstract class AssertMatrix {

  public static void assertEqualsVectors(final DoubleMatrix1D v1, final DoubleMatrix1D v2, final double tol) {
    ArgumentChecker.notNull(v1, "v1");
    ArgumentChecker.notNull(v2, "v2");
    final int size = v1.getNumberOfElements();
    assertEquals("sizes:", size, v2.getNumberOfElements());

    for (int i = 0; i < size; i++) {
      assertEquals("", v1.getEntry(i), v2.getEntry(i), tol);
    }
  }

  public static void assertEqualsMatrix(final DoubleMatrix2D m1, final DoubleMatrix2D m2, final double tol) {
    ArgumentChecker.notNull(m1, "m1");
    ArgumentChecker.notNull(m2, "m2");
    final int rows = m1.getNumberOfRows();
    final int cols = m1.getNumberOfColumns();
    assertEquals("Number of rows:", rows, m2.getNumberOfRows());
    assertEquals("Number of columns:", cols, m2.getNumberOfColumns());
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        assertEquals("", m1.getEntry(i, j), m2.getEntry(i, j), tol);
      }
    }
  }

}
