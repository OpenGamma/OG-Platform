/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.regression;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

/**
 * 
 */
public class GeneralizedLeastSquaresRegression extends LeastSquaresRegression {
  private final Algebra _algebra = new Algebra();

  @Override
  public LeastSquaresRegressionResult regress(final double[][] x, final double[][] weights, final double[] y, final boolean useIntercept) {
    if (weights == null) {
      throw new IllegalArgumentException("Cannot perform GLS regression without an array of weights");
    }
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
    final double[] yModel = convertArray(_algebra.mult(matrix, betasVector).toArray());
    final double[] betas = convertArray(betasVector.toArray());
    return getResultWithStatistics(x, y, betas, yModel, useIntercept);
  }

  private LeastSquaresRegressionResult getResultWithStatistics(final double[][] x, final double[] y, final double[] betas, final double[] yModel, final boolean useIntercept) {
    final int n = x.length;
    final double[] residuals = new double[n];
    for (int i = 0; i < n; i++) {
      residuals[i] = y[i] - yModel[i];
    }
    return new WeightedLeastSquaresRegressionResult(betas, residuals, 0.0, null, 0.0, 0.0, null, null, useIntercept);
  }
}
