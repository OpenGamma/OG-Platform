/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;


/**
 * Essentially just a wrapper for the many sparse matrix format types
 */
public abstract class SparseMatrixType implements MatrixPrimitive {


  /**
   * Gets the maximum number of non zero entries that occurs across the (row/column) space depending
   * on the most useful direction. The purpose of this variable is to allow the BLAS2/3 level routines
   * to index using 16bit int if possible.
   * @return Returns the maximum number of non zero entries on any (row/column) (takes all rows/columns into account).
   */
  public abstract int getMaxNonZerosInSignificantDirection();

}
