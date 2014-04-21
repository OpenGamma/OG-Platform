/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.options;

/**
 * 
 */
public class SpreadBasedStrike implements IndexOptionStrike {

  private final double _amount;

  public SpreadBasedStrike(final double amount) {
    _amount = amount;
  }

  @Override
  public double amount() {
    return _amount;
  }

}
