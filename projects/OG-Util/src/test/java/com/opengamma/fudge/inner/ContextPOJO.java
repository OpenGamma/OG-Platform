/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge.inner;

/**
 * Mock class.
 */
public class ContextPOJO {

  private double _value;

  public ContextPOJO() {
  }

  public double getValue() {
    return _value;
  }

  public void setValue(double value) {
    this._value = value;
  }

}
