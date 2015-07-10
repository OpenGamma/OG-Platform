/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.leastsquare;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * Hold for results of {@link NonLinearLeastSquareWithPenalty}
 */
public class LeastSquareWithPenaltyResults extends LeastSquareResults {
  private final double _penalty;


  /**
   * Holder for the results of minimising $\sum_{i=1}^N (y_i - f_i(\mathbf{x}))^2 + \mathbf{x}^T\mathbf{P}\mathbf{x}$
   * WRT $\mathbf{x}$  (the vector of model parameters). 
   * @param chiSqr The value of the first term (the chi-squared)- the sum of squares between the 'observed' values $y_i$ and the model values 
   * $f_i(\mathbf{x})$ 
   * @param penalty The value of the second term (the penalty) 
   * @param parameters The value of  $\mathbf{x}$ 
   * @param covariance The covariance matrix for  $\mathbf{x}$ 
   */
  public LeastSquareWithPenaltyResults(double chiSqr, double penalty, DoubleMatrix1D parameters,
      DoubleMatrix2D covariance) {
    super(chiSqr, parameters, covariance);
    //other arguments checked in super class 
    ArgumentChecker.notNegative(penalty, "penalty");
    _penalty = penalty;
  }

  /**
   * Holder for the results of minimising $\sum_{i=1}^N (y_i - f_i(\mathbf{x}))^2 + \mathbf{x}^T\mathbf{P}\mathbf{x}$
   * WRT $\mathbf{x}$  (the vector of model parameters). 
   * @param chiSqr The value of the first term (the chi-squared)- the sum of squares between the 'observed' values $y_i$ and the model values 
   * $f_i(\mathbf{x})$ 
   * @param penalty The value of the second term (the penalty) 
   * @param parameters The value of  $\mathbf{x}$ 
   * @param covariance The covariance matrix for  $\mathbf{x}$ 
   * @param inverseJacobian The inverse Jacobian - this is the sensitivities of the model parameters to the 'observed' values 
   */
  public LeastSquareWithPenaltyResults(double chiSqr, double penalty, DoubleMatrix1D parameters,
      DoubleMatrix2D covariance, DoubleMatrix2D inverseJacobian) {
    super(chiSqr, parameters, covariance, inverseJacobian);
    //other arguments checked in super class 
    ArgumentChecker.notNegative(penalty, "penalty");
    _penalty = penalty;
  }

  /** 
   * get the value of the penalty 
   * @return the penalty 
   */
  public double getPenalty() {
    return _penalty;
  }
}
