/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.leastsquare;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class LeastSquareResults {
  private final double _chiSq;
  private final DoubleMatrix1D _parameters;
  private final DoubleMatrix2D _covariance;

  public LeastSquareResults(final double chiSq, final DoubleMatrix1D parameters, final DoubleMatrix2D covariance) {
    Validate.isTrue(chiSq >= 0, "chi square < 0");
    Validate.notNull(parameters, "parameters");
    Validate.notNull(covariance, "covariance");
    final int n = parameters.getNumberOfElements();
    Validate.isTrue(covariance.getNumberOfColumns() == n, "covariance matrix not square");
    Validate.isTrue(covariance.getNumberOfRows() == n, "covariance matrix wrong size");
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
    result = prime * result + _covariance.hashCode();
    result = prime * result + _parameters.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final LeastSquareResults other = (LeastSquareResults) obj;
    if (Double.doubleToLongBits(_chiSq) != Double.doubleToLongBits(other._chiSq)) {
      return false;
    }
    if (!ObjectUtils.equals(_covariance, other._covariance)) {
      return false;
    }
    return ObjectUtils.equals(_parameters, other._parameters);
  }

}
