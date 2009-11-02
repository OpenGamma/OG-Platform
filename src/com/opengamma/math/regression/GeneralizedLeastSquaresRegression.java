/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.regression;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

/**
 * 
 * @author emcleod
 * 
 */

public class GeneralizedLeastSquaresRegression extends LeastSquaresRegression {
  private final Algebra _algebra = new Algebra();

  @Override
  public LeastSquaresRegressionResult regress(final Double[][] x, final Double[][] weights, final Double[] y, final boolean useIntercept) {
    if (weights == null)
      throw new IllegalArgumentException("Cannot perform GLS regression without an array of weights");
    checkData(x, weights, y);
    final double[][] dep = addInterceptVariable(x, useIntercept);
    final double[] indep = new double[y.length];
    final double[][] wArray = new double[y.length][y.length];
    for (int i = 0; i < y.length; i++) {
      indep[i] = y[i];
      for (int j = 0; j < y.length; j++) {
        wArray[i][j] = weights[i][j];
      }
    }
    final DoubleMatrix2D matrix = DoubleFactory2D.dense.make(dep);
    final DoubleMatrix1D vector = DoubleFactory1D.dense.make(indep);
    final DoubleMatrix2D w = DoubleFactory2D.dense.make(wArray);
    final DoubleMatrix2D transpose = _algebra.transpose(matrix);
    final DoubleMatrix1D betasVector = _algebra.mult(_algebra.mult(_algebra.mult(_algebra.inverse(_algebra.mult(transpose, _algebra.mult(w, matrix))), transpose), w), vector);
    final Double[] yModel = convertArray(_algebra.mult(matrix, betasVector).toArray());
    final Double[] betas = convertArray(betasVector.toArray());
    return getResultWithStatistics(x, y, betas, yModel, useIntercept);
  }

  private LeastSquaresRegressionResult getResultWithStatistics(final Double[][] x, final Double[] y, final Double[] betas, final Double[] yModel, final boolean useIntercept) {
    Double yMean = 0.;
    for (final Double y1 : y) {
      yMean += y1;
    }
    yMean /= y.length;
    final int n = x.length;
    final Double[] residuals = new Double[n];
    for (int i = 0; i < n; i++) {
      residuals[i] = y[i] - yModel[i];
    }
    return new WeightedLeastSquaresRegressionResult(betas, residuals, null, null, null, null, null, null, useIntercept);
  }
}
