/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * The Tri-Diag matrix class provides support for tri-diagonal matrix storage.
 *
 * A tri-diagonal matrix generally looks like:
 *
 * |* * o o o|
 * |* * * o o|
 * |o * * * o|
 * |o o * * *|
 * |o o o * *|
 *
 * where '*' denotes entry and 'o' denotes zero.
 *
 * Tridiagonal matrices arise from methods such as simple finite differencing schemes, have applications
 * in eigenvalue computation and are generally a good form to which more complicated forms can be reduced.
 * The reason for aiming to convert or store matrices in tri-diagonal form comes from a number of efficient
 * methods available for inversion, eigenvalue computation and other common linear algebra applications.
 *
 */
public class TriDiagMatrix extends PackedMatrix {

  public TriDiagMatrix(double[][] aMatrix) {
    super(MatrixPrimitiveUtils.checkIsTriDiag(aMatrix));
  }

  public TriDiagMatrix(DoubleMatrix2D aMatrix) {
    super(MatrixPrimitiveUtils.checkIsTriDiag(aMatrix.toArray()));
  }
}
