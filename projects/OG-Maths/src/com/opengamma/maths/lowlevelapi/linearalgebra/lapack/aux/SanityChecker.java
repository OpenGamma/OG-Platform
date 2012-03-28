/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.linearalgebra.lapack.aux;

import com.opengamma.maths.commonapi.exceptions.MathsExceptionInvalidMemoryAllocation;
import com.opengamma.maths.commonapi.exceptions.MathsExceptionNullPointer;

/**
 * Checks sanity of mallocs, conformance etc 
 */
public class SanityChecker {

  /**
   * Performs a sanity check on the malloc'd matrix var
   * @param var the matrix
   * @param expectedMallocDimension1 the first dimension of var
   * @param expectedMallocDimension2 the second dimension of var
   * @param prettyName the pretty print name (variable name) of var
   */
  public static void checkMatrixMallocIsAsExpected(final double[] var, final int expectedMallocDimension1, final int expectedMallocDimension2, final String prettyName) {
    final int expectedMallocSize = expectedMallocDimension1 * expectedMallocDimension2;
    if (var == null) {
      throw new MathsExceptionNullPointer(prettyName);
    }
    if (var.length != expectedMallocSize) {
      throw new MathsExceptionInvalidMemoryAllocation(MathsExceptionInvalidMemoryAllocation.varType.matrix, prettyName, var, expectedMallocDimension1, expectedMallocDimension2);
    }
  }
  
  /**
   * Performs a sanity check on the malloc'd vector var
   * @param var the vector
   * @param expectedMallocDimension1 the first dimension of var
   * @param prettyName the pretty print name (variable name) of var
   */
  public static void checkVectorMallocIsAsExpected(final double[] var, final int expectedMallocDimension1, final String prettyName) {
    final int expectedMallocSize = expectedMallocDimension1;
    if (var == null) {
      throw new MathsExceptionNullPointer(prettyName);
    }
    if (var.length != expectedMallocSize) {
      throw new MathsExceptionInvalidMemoryAllocation(MathsExceptionInvalidMemoryAllocation.varType.matrix, prettyName, var, expectedMallocDimension1, 1);
    }
  }  

}
