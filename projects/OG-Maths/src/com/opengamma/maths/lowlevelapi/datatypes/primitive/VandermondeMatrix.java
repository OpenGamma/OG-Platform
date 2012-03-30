/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * VandermondeMatrix is an extension of DenseMatrix that specialises a DenseMatrix as to having Vandermonde
 * structure, thus aiding the selection of optimal algorithms for common linear algebra.
 *
 * A [m x n] Vandermonde Matrix has the form:
 * | 1 a1 (a1)^2 (a1)^3 ... (a1)^(n-1)|
 * | 1 a2 (a2)^2 (a2)^3 ... (a2)^(n-1)|
 * | 1 a3 (a3)^2 (a3)^3 ... (a3)^(n-1)|
 * | 1 a4 (a4)^2 (a4)^3 ... (a4)^(n-1)|
 * | 1 a5 (a5)^2 (a5)^3 ... (a5)^(n-1)|
 * | 1 a6 (a6)^2 (a6)^3 ... (a6)^(n-1)|
 * | 1 a7 (a7)^2 (a7)^3 ... (a7)^(n-1)|
 * though may equally be expressed as it's transpose.
 *
 * Vandermonde matrices arise in applications such as discrete Fourier transforms and in applications
 * involving polynomial evaluation.
 *
 * If it is know a priori that a Full Matrix will have entries of the Vandermonde matrix form and the Vandermonde form
 * is immutable throughout the computation, then the matrix should be expressed using the VandermondeMatrix class. This definition
 * allows a number of special algorithms to be used for linear algebra operations that have considerably lower Landau order
 * than their Full Matrix counterparts.
 *
 */
public class VandermondeMatrix extends DenseMatrix {
  public VandermondeMatrix(double[][] aMatrix) {
    super(aMatrix);
  }

  public VandermondeMatrix(DoubleMatrix2D aMatrix) {
    super(aMatrix);
  }

  public VandermondeMatrix(DenseMatrix aMatrix) {
    super(aMatrix.toArray());
  }
}


