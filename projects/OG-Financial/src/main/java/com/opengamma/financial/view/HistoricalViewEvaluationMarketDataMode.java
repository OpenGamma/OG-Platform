/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import com.opengamma.util.ArgumentChecker;

/**
 * Enumerates the modes in which market data should be obtained in a {@link ViewEvaluationFunction}.
 */
public enum HistoricalViewEvaluationMarketDataMode {
  
  /**
   * The market data inputs required to produce a calculation for a date, d, are the unaltered historical close prices
   * for these inputs on date d.
   */
  HISTORICAL("Historical"),
  /**
   * The market data inputs required to produce a calculation for a date, d, are derived by shocking each input value
   * at the valuation date by the relative difference between its close value on dates d and d-1.
   */
  RELATIVE_SHOCK("RelativeShock");
  
  private String _constraintName;
  
  private HistoricalViewEvaluationMarketDataMode(String constraintName) {
    _constraintName = constraintName;
  }
  
  public String getConstraintName() {
    return _constraintName;
  }
  
  public static HistoricalViewEvaluationMarketDataMode parse(String text) {
    ArgumentChecker.notNull(text, "text");
    for (HistoricalViewEvaluationMarketDataMode mode : values()) {
      if (mode.getConstraintName().equals(text)) {
        return mode;
      }
    }
    throw new IllegalArgumentException("Unknown mode: " + text);
  }
  
}
