/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class HestonModelData implements SmileModelData {
  private static final int NUM_PARAMETERS = 5;

  private double[] _parameters;

  public HestonModelData(final double kappa, final double theta, final double vol0, final double omega, final double rho) {
    this(new double[] {kappa, theta, vol0, omega, rho });
  }

  public HestonModelData(final double[] parameters) {
    Validate.notNull(parameters, "null parameters");
    Validate.isTrue(parameters.length == NUM_PARAMETERS, "number of parameters wrong");
    Validate.isTrue(parameters[0] >= 0.0, "kappa must be >= 0");
    Validate.isTrue(parameters[1] >= 0.0, "theta must be >= 0");
    Validate.isTrue(parameters[2] >= 0.0, "vol0 must be >= 0");
    Validate.isTrue(parameters[3] >= 0.0, "omega must be >= 0");
    Validate.isTrue(parameters[4] >= -1.0 && parameters[4] <= 1.0, "rho must be >= -1 && <= 1");

    _parameters = parameters;
  }

  public double getKappa() {
    return _parameters[0];
  }

  public double getTheta() {
    return _parameters[1];
  }

  public double getVol0() {
    return _parameters[2];
  }

  public double getOmega() {
    return _parameters[3];
  }

  public double getRho() {
    return _parameters[4];
  }

  public HestonModelData withKappa(final double kappa) {
    return this.with(0, kappa);
  }

  public HestonModelData withTheta(final double theta) {
    return this.with(1, theta);
  }

  public HestonModelData withVol0(final double vol0) {
    return this.with(2, vol0);
  }

  public HestonModelData withOmega(final double omega) {
    return this.with(3, omega);
  }

  public HestonModelData withRho(final double rho) {
    return this.with(4, rho);
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
  public HestonModelData with(int index, double value) {
    double[] temp = new double[NUM_PARAMETERS];
    System.arraycopy(_parameters, 0, temp, 0, NUM_PARAMETERS);
    temp[index] = value;
    return new HestonModelData(temp);
  }

  @Override
  public String toString() {
    return "HestonData [kappa=" + getKappa() + ", theta=" + getTheta() + ", vol0=" + getVol0() + ", omega=" + getOmega() + ", rho=" + getRho() + "]";
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
    HestonModelData other = (HestonModelData) obj;
    if (!Arrays.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

}
