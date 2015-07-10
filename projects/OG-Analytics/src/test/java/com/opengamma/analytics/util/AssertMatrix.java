/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.util;

import static org.testng.AssertJUnit.assertEquals;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * Tests for whether vectors and matrices are equal within  some tolerance 
 */
public abstract class AssertMatrix {

  /**
  * Assert that two vectors (as {@link DoubleMatrix1D}) equal concerning a delta. To be equal the vectors
   * must be the same length, and each element must match to within delta. 
   * If they are not equal an AssertionFailedError is thrown.
   * @param v1 expected vector 
   * @param v2 actual vector 
   * @param delta the allowed difference between the elements 
   */
  public static void assertEqualsVectors(final DoubleMatrix1D v1, final DoubleMatrix1D v2, final double delta) {
    ArgumentChecker.notNull(v1, "v1");
    ArgumentChecker.notNull(v2, "v2");
    final int size = v1.getNumberOfElements();
    assertEquals("sizes:", size, v2.getNumberOfElements());

    for (int i = 0; i < size; i++) {
      assertEquals("", v1.getEntry(i), v2.getEntry(i), delta);
    }
  }

  /**
   * Assert that two matrices (as {@link DoubleMatrix2D}) equal concerning a delta. To be equal the matrices
   * must be the same size, and each element must match to within delta. 
   * If they are not equal an AssertionFailedError is thrown.
   * @param m1 expected matrix
   * @param m2 actual matrix 
   * @param delta the allowed difference between the elements 
   */
  public static void assertEqualsMatrix(final DoubleMatrix2D m1, final DoubleMatrix2D m2, final double delta) {
    ArgumentChecker.notNull(m1, "m1");
    ArgumentChecker.notNull(m2, "m2");
    final int rows = m1.getNumberOfRows();
    final int cols = m1.getNumberOfColumns();
    assertEquals("Number of rows:", rows, m2.getNumberOfRows());
    assertEquals("Number of columns:", cols, m2.getNumberOfColumns());
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        assertEquals("", m1.getEntry(i, j), m2.getEntry(i, j), delta);
      }
    }
  }

}
