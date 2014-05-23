/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

/**
 * 
 */
public class ExerciseAmount implements IndexOptionStrike {
  private final double _amount;

  public ExerciseAmount(final double amount) {
    _amount = amount;
  }

  @Override
  public double amount() {
    return _amount;
  }

}
