/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

/**
 *
 */
public enum OperationType {
  /** Add */
  ADD {

    @Override
    public String getSymbol() {
      return "+";
    }
  },
  /** Subtract */
  SUBTRACT {

    @Override
    public String getSymbol() {
      return "-";
    }
  },
  /** Multiply */
  MULTIPLY {

    @Override
    public String getSymbol() {
      return "x";
    }
  },
  /** Divide */
  DIVIDE {

    @Override
    public String getSymbol() {
      return "/";
    }
  };

  public abstract String getSymbol();
}
