/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge.inner;

public class ContextPOJO {
  double value = 0;

  public ContextPOJO() {
  }

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }
}
