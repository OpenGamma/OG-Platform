/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

/**
 * The two currency amounts in an FX trade.
 */
public class FXAmounts {

  /** The amount in the base currency. */
  private final double _counterAmount;
  /** The amount in the counter currency. */
  private final double _baseAmount;

  /**
   * @param baseAmount The amount in the base currency
   * @param counterAmount The amount in the counter currency
   */
  /* package */ FXAmounts(double baseAmount, double counterAmount) {
    _baseAmount = baseAmount;
    _counterAmount = counterAmount;
  }

  /**
   * @return The amount in the base currency.
   */
  public double getBaseAmount() {
    return _baseAmount;
  }

  /**
   * @return The amount in the counter currency.
   */
  public double getCounterAmount() {
    return _counterAmount;
  }
}
