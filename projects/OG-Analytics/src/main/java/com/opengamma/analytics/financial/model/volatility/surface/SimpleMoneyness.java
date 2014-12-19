/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;


/**
 * Defined as x = strike - forward
 */
public class SimpleMoneyness implements StrikeType {
  private final double _value;

  /**
   * Constructor with value of simple moneyness 
   * @param value The simple moneyness
   */
  public SimpleMoneyness(double value) {
    _value = value;
  }

  /**
   * Constructor with strike and forward
   * @param strike The strike 
   * @param forward the forward
   */
  public SimpleMoneyness(double strike, double forward) {
    _value = strike - forward;
  }

  @Override
  public double value() {
    return _value;
  }

  @Override
  public StrikeType with(double value) {
    return new SimpleMoneyness(value);
  }

}
