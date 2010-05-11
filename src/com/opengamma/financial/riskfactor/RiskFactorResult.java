/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

/**
 * 
 */
public class RiskFactorResult {
  private final double _result;

  public RiskFactorResult(final Double result) {
    if (result == null)
      throw new IllegalArgumentException("Risk factor was null");
    _result = result;
  }

  public double getResult() {
    return _result;
  }

  @Override
  public String toString() {
    return "RiskFactorResult[" + getResult() + "]";
  }
}
