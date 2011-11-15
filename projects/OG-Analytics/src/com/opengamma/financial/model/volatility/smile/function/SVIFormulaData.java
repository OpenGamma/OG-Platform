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
public class SVIFormulaData implements SmileModelData {
  private static final int NUM_PARAMETERS = 5;
  private final double[] _parameters;

  public SVIFormulaData(final double[] parameters) {
    Validate.notNull(parameters, "parameters are null");
    Validate.isTrue(parameters.length == NUM_PARAMETERS, "must be " + NUM_PARAMETERS + " parameters");
    Validate.isTrue(parameters[0] >= 0, "Need a >= 0");
    Validate.isTrue(parameters[1] >= 0, "Need b >= 0");
    Validate.isTrue(parameters[2] >= -1 && parameters[2] <= 1, "Need -1 <= rho <= 1");
    Validate.isTrue(parameters[3] >= 0, "Need nu >= 0");

    _parameters = parameters;
  }

  public SVIFormulaData(final double a, final double b, final double rho, final double nu, final double m) {
    this(new double[] {a, b, rho, nu, m });
  }

  public double getA() {
    return _parameters[0];
  }

  public double getB() {
    return _parameters[1];
  }

  public double getRho() {
    return _parameters[2];
  }

  public double getNu() {
    return _parameters[3];
  }

  public double getM() {
    return _parameters[4];
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
    return new SVIFormulaData(temp);
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
    SVIFormulaData other = (SVIFormulaData) obj;
    if (!Arrays.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "SVIFormulaData [a=" + getA() + ", b=" + getB() + ", rho=" + getRho() + ", nu=" + getNu() + ", m=" + getM() + "]";
  }

}
