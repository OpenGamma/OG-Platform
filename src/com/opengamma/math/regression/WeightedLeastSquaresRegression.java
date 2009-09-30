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

/**
 * 
 * @author emcleod
 * 
 */

public class WeightedLeastSquaresRegression extends LeastSquaresRegression {
  private static final Logger s_Log = LoggerFactory.getLogger(WeightedLeastSquaresRegression.class);
  private final Algebra _algebra = new Algebra();

  @Override
  public LeastSquaresRegressionResult regress(final Double[][] x, final Double[][] weights, final Double[] y, final boolean useIntercept) {
    if (weights == null)
      throw new IllegalArgumentException("Cannot perform WLS regression without an array of weights");
    checkData(x, weights, y);
    s_Log.info("Have a two-dimensional array for what should be a one-dimensional array of weights. The weights used in this regression will be the diagonal elements only");
    final Double[] w = new Double[weights.length];
    for (int i = 0; i < w.length; i++) {
      w[i] = weights[i][i];
    }
    return regress(x, w, y, useIntercept);
  }

  public LeastSquaresRegressionResult regress(final Double[][] x, final Double[] weights, final Double[] y, final boolean useIntercept) {
    if (weights == null)
      throw new IllegalArgumentException("Cannot perform WLS regression without an array of weights");
    checkData(x, weights, y);
    final double[][] dep = addInterceptVariable(x, useIntercept);
    final double[] indep = new double[y.length];
    final double[] w = new double[weights.length];
    for (int i = 0; i < y.length; i++) {
      indep[i] = y[i];
      w[i] = weights[i];
    }
    final DoubleMatrix2D matrix = DoubleFactory2D.dense.make(dep);
    final DoubleMatrix1D vector = DoubleFactory1D.dense.make(indep);
    final DoubleMatrix2D wDiag = DoubleFactory2D.sparse.diagonal(DoubleFactory1D.dense.make(w));
    final DoubleMatrix2D transpose = _algebra.transpose(matrix);
    final DoubleMatrix1D betasVector = _algebra.mult(_algebra.mult(_algebra.mult(_algebra.inverse(_algebra.mult(transpose, _algebra.mult(wDiag, matrix))), transpose), wDiag),
        vector);
    final Double[] yModel = convertArray(_algebra.mult(matrix, betasVector).toArray());
    final Double[] betas = convertArray(betasVector.toArray());
    return getResultWithStatistics(x, convertArray(wDiag.toArray()), y, betas, yModel, transpose, matrix);
  }

  private LeastSquaresRegressionResult getResultWithStatistics(final Double[][] x, final Double[][] w, final Double[] y, final Double[] betas, final Double[] yModel,
      final DoubleMatrix2D transpose, final DoubleMatrix2D matrix) {
    Double yMean = 0.;
    for (final Double y1 : y) {
      yMean += y1;
    }
    yMean /= y.length;
    Double totalSumOfSquares = 0.;
    Double errorSumOfSquares = 0.;
    final Double regressionSumOfSquares = 0.;
    final int n = x.length;
    final int k = x[0].length;
    final Double[] residuals = new Double[k];
    final Double[] stdErrorBetas = new Double[k];
    final Double[] tStats = new Double[k];
    final Double[] pValues = new Double[k];
    for (int i = 0; i < k; i++) {
      totalSumOfSquares += w[i][i] * (y[i] - yMean) * (y[i] - yMean);
      residuals[i] = y[i] - yModel[i];
      errorSumOfSquares += w[i][i] * residuals[i] * residuals[i];
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
