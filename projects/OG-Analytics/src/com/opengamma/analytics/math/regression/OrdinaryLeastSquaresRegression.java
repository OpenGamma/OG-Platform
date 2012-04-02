/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.regression;

import org.apache.commons.math.distribution.ContinuousDistribution;
import org.apache.commons.math.distribution.TDistributionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

/**
 * 
 */
public class OrdinaryLeastSquaresRegression extends LeastSquaresRegression {
  private static final Logger s_logger = LoggerFactory.getLogger(OrdinaryLeastSquaresRegression.class);
  private final Algebra _algebra = new Algebra();

  @Override
  public LeastSquaresRegressionResult regress(final double[][] x, final double[][] weights, final double[] y, final boolean useIntercept) {
    if (weights != null) {
      s_logger.info("Weights were provided for OLS regression: they will be ignored");
    }
    return regress(x, y, useIntercept);
  }

  public LeastSquaresRegressionResult regress(final double[][] x, final double[] y, final boolean useIntercept) {
    checkData(x, y);
    final double[][] indep = addInterceptVariable(x, useIntercept);
    final double[] dep = new double[y.length];
    for (int i = 0; i < y.length; i++) {
      dep[i] = y[i];
    }
    final DoubleMatrix2D matrix = DoubleFactory2D.dense.make(indep);
    final DoubleMatrix1D vector = DoubleFactory1D.dense.make(dep);
    final DoubleMatrix2D transpose = _algebra.transpose(matrix);
    final DoubleMatrix1D betasVector = _algebra.mult(_algebra.mult(_algebra.inverse(_algebra.mult(transpose, matrix)), transpose), vector);
    final double[] yModel = convertArray(_algebra.mult(matrix, betasVector).toArray());
    final double[] betas = convertArray(betasVector.toArray());
    return getResultWithStatistics(x, y, betas, yModel, transpose, matrix, useIntercept);
  }

  private LeastSquaresRegressionResult getResultWithStatistics(final double[][] x, final double[] y, final double[] betas, final double[] yModel, final DoubleMatrix2D transpose,
      final DoubleMatrix2D matrix, final boolean useIntercept) {
    double yMean = 0.;
    for (final double y1 : y) {
      yMean += y1;
    }
    yMean /= y.length;
    double totalSumOfSquares = 0.;
    double errorSumOfSquares = 0.;
    final int n = x.length;
    final int k = betas.length;
    final double[] residuals = new double[n];
    final double[] stdErrorBetas = new double[k];
    final double[] tStats = new double[k];
    final double[] pValues = new double[k];
    for (int i = 0; i < n; i++) {
      totalSumOfSquares += (y[i] - yMean) * (y[i] - yMean);
      residuals[i] = y[i] - yModel[i];
      errorSumOfSquares += residuals[i] * residuals[i];
    }
    final double regressionSumOfSquares = totalSumOfSquares - errorSumOfSquares;
    final double[][] covarianceBetas = convertArray(_algebra.inverse(_algebra.mult(transpose, matrix)).toArray());
    final double rSquared = regressionSumOfSquares / totalSumOfSquares;
    final double adjustedRSquared = 1. - (1 - rSquared) * (n - 1.) / (n - k);
    final double meanSquareError = errorSumOfSquares / (n - k);
    final ContinuousDistribution studentT = new TDistributionImpl(n - k);
    // final ProbabilityDistribution<Double> studentT = new
    // StudentTDistribution(n - k);
    for (int i = 0; i < k; i++) {
      stdErrorBetas[i] = Math.sqrt(meanSquareError * covarianceBetas[i][i]);
      tStats[i] = betas[i] / stdErrorBetas[i];
      try {
        pValues[i] = 1 - studentT.cumulativeProbability(Math.abs(tStats[i]));
      } catch (final org.apache.commons.math.MathException e) {
        throw new com.opengamma.analytics.math.MathException(e);
      }
    }
    return new LeastSquaresRegressionResult(betas, residuals, meanSquareError, stdErrorBetas, rSquared, adjustedRSquared, tStats, pValues, useIntercept);
  }
}
