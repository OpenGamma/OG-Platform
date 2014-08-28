/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.leastsquare;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Hold for results of {@link NonLinearLeastSquareWithPenalty}
 */
public class LeastSquareWithPenaltyResults extends LeastSquareResults {
  private final double _penalty;

  public LeastSquareWithPenaltyResults(double chiSq, double penalty, DoubleMatrix1D parameters,
      DoubleMatrix2D covariance) {
    super(chiSq, parameters, covariance);
    _penalty = penalty;
  }

  public LeastSquareWithPenaltyResults(double chiSq, double penalty, DoubleMatrix1D parameters,
      DoubleMatrix2D covariance, DoubleMatrix2D inverseJacobian) {
    super(chiSq, parameters, covariance, inverseJacobian);
    _penalty = penalty;
  }

  public double getPenalty() {
    return _penalty;
  }
}
