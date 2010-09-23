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

  public RiskFactorResult(final double result) {
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
