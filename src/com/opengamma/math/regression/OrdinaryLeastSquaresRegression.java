/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.regression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.stat.Probability;

public class OrdinaryLeastSquaresRegression extends LeastSquaresRegression {
  private static final Logger s_Log = LoggerFactory.getLogger(OrdinaryLeastSquaresRegression.class);
  private final Algebra _algebra = new Algebra();

  @Override
  public LeastSquaresRegressionResult regress(final Double[][] x, final Double[][] weights, final Double[] y, final boolean useIntercept) {
    if (weights != null) {
      s_Log.info("Weights were provided for OLS regression: they will be ignored");
    }
    return regress(x, y, useIntercept);
  }

  public LeastSquaresRegressionResult regress(final Double[][] x, final Double[] y, final boolean useIntercept) {
    checkData(x, y);
    final double[][] dep = addInterceptVariable(x, useIntercept);
    final double[] indep = new double[y.length];
    for (int i = 0; i < y.length; i++) {
      indep[i] = y[i];
    }
    final DoubleMatrix2D matrix = DoubleFactory2D.dense.make(dep);
    final DoubleMatrix1D vector = DoubleFactory1D.dense.make(indep);
    final DoubleMatrix2D transpose = _algebra.transpose(matrix);
    final DoubleMatrix1D betasVector = _algebra.mult(_algebra.mult(_algebra.inverse(_algebra.mult(transpose, matrix)), transpose), vector);
    final Double[] yModel = convertArray(_algebra.mult(matrix, betasVector).toArray());
    final Double[] betas = convertArray(betasVector.toArray());
    return getResultWithStatistics(x, y, betas, yModel, transpose, matrix);
  }

  private LeastSquaresRegressionResult getResultWithStatistics(final Double[][] x, final Double[] y, final Double[] betas, final Double[] yModel, final DoubleMatrix2D transpose,
      final DoubleMatrix2D matrix) {
    Double yMean = 0.;
    for (final Double y1 : y) {
      yMean += y1;
    }
    yMean /= y.length;
    Double totalSumOfSquares = 0.;
    Double errorSumOfSquares = 0.;
    Double regressionSumOfSquares = 0.;
    final int n = x.length;
    final int k = x[0].length;
    final Double[] residuals = new Double[k];
    final Double[] stdErrorBetas = new Double[k];
    final Double[] tStats = new Double[k];
    final Double[] pValues = new Double[k];
    for (int i = 0; i < k; i++) {
      totalSumOfSquares += (y[i] - yMean) * (y[i] - yMean);
      residuals[i] = y[i] - yModel[i];
      errorSumOfSquares += residuals[i] * residuals[i];
      regressionSumOfSquares += (yModel[i] - y[i]) * (yModel[i] - y[i]);
    }
    final Double[][] covarianceBetas = convertArray(_algebra.inverse(_algebra.mult(transpose, matrix)).toArray());
    final Double rSquared = regressionSumOfSquares / totalSumOfSquares;
    final Double adjustedRSquared = 1 - (n - 1) / (n - k) * (1 - rSquared);
    final Double meanSquareError = 0.;
    final Double[] standardErrorOfBeta = null;
    for (int i = 0; i < k; i++) {
      stdErrorBetas[i] = Math.sqrt(covarianceBetas[i][i]);
      tStats[i] = Math.sqrt(meanSquareError) * betas[i] / stdErrorBetas[i];
      pValues[i] = Probability.studentT(n - k, tStats[i]);// new
      // StudentTDistribution(n
      // -
      // k).getCDF(tStats[i]);
    }
    return new LeastSquaresRegressionResult(betas, residuals, meanSquareError, standardErrorOfBeta, rSquared, adjustedRSquared, tStats, pValues);
  }

}
