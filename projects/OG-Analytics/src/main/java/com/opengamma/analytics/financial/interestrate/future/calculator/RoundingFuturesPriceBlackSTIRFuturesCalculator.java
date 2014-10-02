/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

/**
 * Rounds the underlying futures price to a given precision. The precision for
 * this calculator is configured in the constructor. This is useful for option
 * pricing when the quote convention specifies that the underlying future is
 * rounded to a certain precision.
 */
public class RoundingFuturesPriceBlackSTIRFuturesCalculator extends FuturesPriceBlackSTIRFuturesCalculator {
  
  private final double _precision;

  /**
   * A calculator which rounds the price of the underlying future using the
   * configured precision
   * 
   * @param precision the precision to round to, e.g. 0.01 for 2 dps
   */
  public RoundingFuturesPriceBlackSTIRFuturesCalculator(double precision) {
    _precision = precision;
  }
  
  @Override
  protected double roundFuturesPrice(double unrounded) {
    return Math.round(unrounded / _precision) * _precision;
  }
  
}
