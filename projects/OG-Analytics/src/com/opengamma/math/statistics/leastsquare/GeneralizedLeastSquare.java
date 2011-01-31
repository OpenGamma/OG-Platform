/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.leastsquare;

import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.math.UtilFunctions;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.linearalgebra.Decomposition;
import com.opengamma.math.linearalgebra.DecompositionResult;
import com.opengamma.math.linearalgebra.SVDecompositionCommons;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.DoubleMatrixUtils;
import com.opengamma.math.matrix.MatrixAlgebra;
import com.opengamma.math.matrix.OGMatrixAlgebra;

/**
 * 
 */
public class GeneralizedLeastSquare {

  private final Decomposition<?> _decomposition;
  private final MatrixAlgebra _algebra;

  public GeneralizedLeastSquare() {
    _decomposition = new SVDecompositionCommons();
    _algebra = new OGMatrixAlgebra();

  }

  public LeastSquareResults solve(final DoubleMatrix1D x, final DoubleMatrix1D y, final DoubleMatrix1D sigma, final List<Function1D<Double, Double>> basisFunctions) {

    Validate.notNull(x, "x");
    Validate.notNull(y, "y");
    Validate.notNull(sigma, "sigma");
    Validate.notEmpty(basisFunctions, "basisFunctions");

    final int n = x.getNumberOfElements();
    Validate.isTrue(n > 0, "no data");
    Validate.isTrue(y.getNumberOfElements() == n, "y wrong length");
    Validate.isTrue(sigma.getNumberOfElements() == n, "sigma wrong length");

    int m = basisFunctions.size();

    double[] b = new double[m];
    double[][] a = new double[m][m];
    double[] invSigmaSqr = new double[n];
    double[][] f = new double[m][n];
    int i, j, k;

    for (i = 0; i < n; i++) {
      double temp = sigma.getEntry(i);
      Validate.isTrue(temp > 0, "sigma must be great than zero");
      invSigmaSqr[i] = 1.0 / temp / temp;
    }

    for (i = 0; i < m; i++) {
      for (j = 0; j < n; j++) {
        f[i][j] = basisFunctions.get(i).evaluate(x.getEntry(j));
      }
    }

    double sum;
    for (i = 0; i < m; i++) {
      sum = 0;
      for (k = 0; k < n; k++) {
        sum += y.getEntry(k) * f[i][k] * invSigmaSqr[k];
      }
      b[i] = sum;

    }

    for (i = 0; i < m; i++) {
      sum = 0;
      for (k = 0; k < n; k++) {
        sum += UtilFunctions.square(f[i][k]) * invSigmaSqr[k];
      }
      a[i][i] = sum;
      for (j = i + 1; j < m; j++) {
        sum = 0;
        for (k = 0; k < n; k++) {
          sum += f[i][k] * f[j][k] * invSigmaSqr[k];
        }
        a[i][j] = sum;
        a[j][i] = sum;
      }
    }

    DoubleMatrix2D ma = new DoubleMatrix2D(a);
    DoubleMatrix1D mb = new DoubleMatrix1D(b);

    DecompositionResult decmp = _decomposition.evaluate(ma);
    DoubleMatrix1D w = decmp.solve(mb);
    DoubleMatrix2D covar = decmp.solve(DoubleMatrixUtils.getIdentityMatrix2D(m));

    double chiSq = 0;
    for (i = 0; i < n; i++) {
      double temp = 0;
      for (k = 0; k < m; k++) {
        temp += w.getEntry(k) * f[k][i];
      }
      chiSq += UtilFunctions.square(y.getEntry(i) - temp) * invSigmaSqr[i];
    }

    return new LeastSquareResults(chiSq, w, covar);
  }

}
