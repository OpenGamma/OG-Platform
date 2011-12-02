/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class SABRFormulaData implements SmileModelData {

  private static final int NUM_PARAMETERS = 4;
  private final double[] _parameters;

  /**
   * 
   * @param parameters Must be 4 parameters in the order alpha, beta, rho, nu
   */
  public SABRFormulaData(final double[] parameters) {
    Validate.notNull(parameters, "parameters are null");
    Validate.isTrue(parameters.length == NUM_PARAMETERS, "must be " + NUM_PARAMETERS + " parameters");
    Validate.isTrue(parameters[0] >= 0.0, "alpha must be >= 0.0");
    Validate.isTrue(parameters[1] >= 0.0, "beta must be >= 0.0");
    Validate.isTrue(parameters[2] >= -1 && parameters[2] <= 1, "rho must be between -1 and 1");
    Validate.isTrue(parameters[3] >= 0.0, "nu must be >= 0.0");

    _parameters = parameters;
  }

  /**
   * 
   * @param alpha The initial value of the stochastic volatility 
   * @param beta The CEV parameter 
   * @param rho The correlation between the driver of the underlying and the driver of the stochastic volatility 
   * @param nu The vol-of-vol
   */
  public SABRFormulaData(final double alpha, final double beta, final double rho, final double nu) {
    this(new double[] {alpha, beta, rho, nu });
  }

  public double getNu() {
    return _parameters[3];
  }

  public double getRho() {
    return _parameters[2];
  }

  public double getBeta() {
    return _parameters[1];
  }

  public double getAlpha() {
    return _parameters[0];
  }

  public SABRFormulaData withAlpha(final double alpha) {

    return new SABRFormulaData(alpha, getBeta(), getRho(), getNu());
  }

  public SABRFormulaData withBeta(final double beta) {
    return new SABRFormulaData(getAlpha(), beta, getRho(), getNu());
  }

  public SABRFormulaData withRho(final double rho) {
    return new SABRFormulaData(getAlpha(), getBeta(), rho, getNu());
  }

  public SABRFormulaData withNu(final double nu) {
    return new SABRFormulaData(getAlpha(), getBeta(), getRho(), nu);
  }

  @Override
  public int getNumberOfparameters() {
    return NUM_PARAMETERS;
  }

  @Override
  public double getParameter(int index) {
    return _parameters[index];
  }

  @Override
  public SmileModelData with(int index, double value) {
    double[] temp = new double[NUM_PARAMETERS];
    System.arraycopy(_parameters, 0, temp, 0, NUM_PARAMETERS);
    temp[index] = value;
    return new SABRFormulaData(temp);
  }

  @Override
  public String toString() {
    return "SABRFormulaData [alpha=" + getAlpha() + ", beta=" + getBeta() + ", rho=" + getRho() + ", nu=" + getNu() + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_parameters);
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
    SABRFormulaData other = (SABRFormulaData) obj;
    if (!Arrays.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

}
