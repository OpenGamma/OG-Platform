/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.future.derivative;

/**
 * A cash-settled futures contract on the index of the *dividends* of a given stock market index on the _fixingDate
 */
public class EquitySingleStockFuture extends EquityFuture {

  
  public EquitySingleStockFuture(final double fixingDate, final double deliveryDate, final double strike, final double pointValue, final String assetName) {
    super( fixingDate, deliveryDate, strike, pointValue, 1, assetName);
  }
    
}