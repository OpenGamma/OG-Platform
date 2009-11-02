/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.regression;

import java.util.Arrays;

/**
 * 
 * Contains the results from a least squares regression.
 * 
 * @author emcleod
 */
public class LeastSquaresRegressionResult {
  private final Double[] _residuals;
  private final Double[] _betas;
  private final Double _meanSquareError;
  private final Double[] _standardErrorOfBeta;
  private final Double _rSquared;
  private final Double _rSquaredAdjusted;
  private final Double[] _tStats;
  private final Double[] _pValues;

  public LeastSquaresRegressionResult(final LeastSquaresRegressionResult result) {
    _betas = result.getBetas();
    _residuals = result.getResiduals();
    _meanSquareError = result.getMeanSquareError();
    _standardErrorOfBeta = result.getStandardErrorOfBetas();
    _rSquared = result.getRSquared();
    _rSquaredAdjusted = result.getAdjustedRSquared();
    _tStats = result.getTStatistics();
    _pValues = result.getPValues();
  }

  public LeastSquaresRegressionResult(final Double[] betas, final Double[] residuals, final Double meanSquareError, final Double[] standardErrorOfBeta, final Double rSquared,
      final Double rSquaredAdjusted, final Double[] tStats, final Double[] pValues) {
    _betas = betas;
    _residuals = residuals;
    _meanSquareError = meanSquareError;
    _standardErrorOfBeta = standardErrorOfBeta;
    _rSquared = rSquared;
    _rSquaredAdjusted = rSquaredAdjusted;
    _tStats = tStats;
    _pValues = pValues;
  }

  public Double[] getBetas() {
    return _betas;
  }

  public Double[] getResiduals() {
    return _residuals;
  }

  public Double getMeanSquareError() {
    return _meanSquareError;
  }

  public Double[] getStandardErrorOfBetas() {
    return _standardErrorOfBeta;
  }

  public Double getRSquared() {
    return _rSquared;
  }

  public Double getAdjustedRSquared() {
    return _rSquaredAdjusted;
  }

  public Double[] getTStatistics() {
    return _tStats;
  }

  public Double[] getPValues() {
    return _pValues;
  }

  public Double getPredictedValue(final Double[] x) {
    if (x == null)
      throw new IllegalArgumentException("x array was null");
    final Double[] betas = getBetas();
    if (x.length != betas.length)
      throw new IllegalArgumentException("Number of variables did not match number used in regression");
    double sum = 0;
    for (int i = 0; i < x.length; i++) {
      sum += x[i] * betas[i];
    }
    return sum;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_betas);
    result = prime * result + (_meanSquareError == null ? 0 : _meanSquareError.hashCode());
    result = prime * result + Arrays.hashCode(_pValues);
    result = prime * result + (_rSquared == null ? 0 : _rSquared.hashCode());
    result = prime * result + (_rSquaredAdjusted == null ? 0 : _rSquaredAdjusted.hashCode());
    result = prime * result + Arrays.hashCode(_residuals);
    result = prime * result + Arrays.hashCode(_standardErrorOfBeta);
    result = prime * result + Arrays.hashCode(_tStats);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final LeastSquaresRegressionResult other = (LeastSquaresRegressionResult) obj;
    if (!Arrays.equals(_betas, other._betas))
      return false;
    if (_meanSquareError == null) {
      if (other._meanSquareError != null)
        return false;
    } else if (!_meanSquareError.equals(other._meanSquareError))
      return false;
    if (!Arrays.equals(_pValues, other._pValues))
      return false;
    if (_rSquared == null) {
      if (other._rSquared != null)
        return false;
    } else if (!_rSquared.equals(other._rSquared))
      return false;
    if (_rSquaredAdjusted == null) {
      if (other._rSquaredAdjusted != null)
        return false;
    } else if (!_rSquaredAdjusted.equals(other._rSquaredAdjusted))
      return false;
    if (!Arrays.equals(_residuals, other._residuals))
      return false;
    if (!Arrays.equals(_standardErrorOfBeta, other._standardErrorOfBeta))
      return false;
    if (!Arrays.equals(_tStats, other._tStats))
      return false;
    return true;
  }
}
