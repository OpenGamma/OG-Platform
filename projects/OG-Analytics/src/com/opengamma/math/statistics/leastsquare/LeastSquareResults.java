/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.leastsquare;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class LeastSquareResults {

  private final double _chiSq;
  private final DoubleMatrix1D _parameters;
  private final DoubleMatrix2D _covariance;

  public LeastSquareResults(final double chiSq, final DoubleMatrix1D parameters, final DoubleMatrix2D covariance) {
    ArgumentChecker.notNegative(chiSq, "chi square");
    ArgumentChecker.notNull(parameters, "parameters");
    ArgumentChecker.notNull(covariance, "covariance");
    int n = parameters.getNumberOfElements();
    if (covariance.getNumberOfColumns() != covariance.getNumberOfRows()) {
      throw new IllegalArgumentException("covariance matrix not square");
    }
    if (n != covariance.getNumberOfRows()) {
      throw new IllegalArgumentException("covariance matrix wrong size");
    }
    _chiSq = chiSq;
    _parameters = parameters;
    _covariance = covariance;
  }

  /**
   * Gets the chiSq field.
   * @return the chiSq
   */
  public double getChiSq() {
    return _chiSq;
  }

  /**
   * Gets the parameters field.
   * @return the parameters
   */
  public DoubleMatrix1D getParameters() {
    return _parameters;
  }

  /**
   * Gets the covariance field.
   * @return the covariance
   */
  public DoubleMatrix2D getCovariance() {
    return _covariance;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_chiSq);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_covariance == null) ? 0 : _covariance.hashCode());
    result = prime * result + ((_parameters == null) ? 0 : _parameters.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    LeastSquareResults other = (LeastSquareResults) obj;
    if (Double.doubleToLongBits(_chiSq) != Double.doubleToLongBits(other._chiSq)) {
      return false;
    }
    if (_covariance == null) {
      if (other._covariance != null) {
        return false;
      }
    } else if (!_covariance.equals(other._covariance)) {
      return false;
    }
    if (_parameters == null) {
      if (other._parameters != null) {
        return false;
      }
    } else if (!_parameters.equals(other._parameters)) {
      return false;
    }
    return true;
  }

}
