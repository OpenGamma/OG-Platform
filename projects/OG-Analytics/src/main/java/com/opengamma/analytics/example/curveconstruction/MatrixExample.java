/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.example.curveconstruction;

// @export "imports"
import java.io.PrintStream;

import com.opengamma.analytics.math.matrix.ColtMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Example for matrix.
 */
// @export "classDefinition"
public class MatrixExample {
// CSOFF

  // @export "initMatrixDemo"
  static double[] ARRAY_1D = {4.0, 5.0, 10.0 };
  public static double[][] ARRAY_2D = { {1.0, 2.0, 3.0 }, {4.0, 5.0, 6.0 } };

  public static void initMatrixDemo(PrintStream out) {
    DoubleMatrix1D matrix_1d = new DoubleMatrix1D(ARRAY_1D);
    out.println(matrix_1d);

    DoubleMatrix2D matrix_2d = new DoubleMatrix2D(ARRAY_2D);
    out.println(matrix_2d);
  }

  // @export "matrixAlgebraDemo"
  public static void matrixAlgebraDemo(PrintStream out) {
    ColtMatrixAlgebra colt = new ColtMatrixAlgebra();
    DoubleMatrix1D v = new DoubleMatrix1D(ARRAY_1D);
    DoubleMatrix2D m = new DoubleMatrix2D(ARRAY_2D);

    out.println(colt.getTranspose(m));
    out.println(colt.multiply(m, v));
  }

  public static void main(String[] args) {
    initMatrixDemo(System.out);
    matrixAlgebraDemo(System.out);
  }
}
