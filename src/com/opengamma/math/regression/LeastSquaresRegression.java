package com.opengamma.math.regression;

/**
 * 
 * @author emcleod
 * 
 */

public abstract class LeastSquaresRegression implements Regression {
  private double[] _residuals;
  private double[] _betas;
  private double _meanSquareError;
  private double[] _standardErrorOfBeta;
  private double _rSquared;
  private double _rSquaredAdjusted;
  private double[] _tStats;
  private double[] _pValues;

  @Override
  public double[] getBetas() {
    return _betas;
  }

  @Override
  public double[] getResiduals() {
    return _residuals;
  }

  public double getMeanSquareError() {
    return _meanSquareError;
  }

  public double[] getStandardErrorOfBetas() {
    return _standardErrorOfBeta;
  }

  public double getRSquared() {
    return _rSquared;
  }

  public double getAdjustedRSquared() {
    return _rSquaredAdjusted;
  }

  public double[] getTStatistics() {
    return _tStats;
  }

  public double[] getPValues() {
    return _pValues;
  }

  void setBetas(double[] betas) {
    _betas = betas;
  }

  void setResiduals(double[] residuals) {
    _residuals = residuals;
  }

  void setMeanSquareError(double meanSquareError) {
    _meanSquareError = meanSquareError;
  }

  void setStandardErrorOfBeta(double[] standardErrorOfBeta) {
    _standardErrorOfBeta = standardErrorOfBeta;
  }

  void setRSquared(double rSquared) {
    _rSquared = rSquared;
  }

  void setAdjustedRSquared(double rSquaredAdjusted) {
    _rSquaredAdjusted = rSquaredAdjusted;
  }

  void setTStatistics(double[] tStats) {
    _tStats = tStats;
  }

  void setPValues(double[] pValues) {
    _pValues = pValues;
  }
}
