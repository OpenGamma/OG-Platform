/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.Currency;
import com.opengamma.util.time.Expiry;

/**
 * 
 *
 * @author Andrew
 */
public class FXFutureSecurity extends FutureSecurity {
  //TODO there's no reason why this shouldn't be used for FX cross futures, which means it will also need a currency for the trade itself
  private final Currency _numerator;
  private final Currency _denominator;
  private final double _multiplicationFactor;
  
  /**
   * @param expiry
   * @param month
   * @param year
   * @param tradingExchange
   * @param settlementExchange
   */
  public FXFutureSecurity(Expiry expiry, String tradingExchange, String settlementExchange, Currency domesticCurrency, Currency numerator, Currency denominator, double multiplicationFactor) {
    super(expiry, tradingExchange, settlementExchange, domesticCurrency);
    _numerator = numerator;
    _denominator = denominator;
    _multiplicationFactor = multiplicationFactor;
  }
  
  public FXFutureSecurity(Expiry expiry, String tradingExchange, String settlementExchange, Currency domesticCurrency, Currency numerator, Currency denominator) {
    this (expiry, tradingExchange, settlementExchange, domesticCurrency, numerator, denominator, 1.0);
  }
  
  public Currency getNumerator () {
    return _numerator;
  }
  
  public Currency getDenominator () {
    return _denominator;
  }
  
  public double getMultiplicationFactor () {
    return _multiplicationFactor;
  }

  @Override
  public <T> T accept(FutureSecurityVisitor<T> visitor) {
    return visitor.visitFXFutureSecurity (this);
  }

}