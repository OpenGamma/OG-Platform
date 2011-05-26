/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.future.derivative;

/**
 * A cash-settled futures contract on the value of a published stock market index on the _fixingDate 
 */
public class EquityIndexFuture extends EquityFuture {

  
  public EquityIndexFuture(final double fixingDate, final double deliveryDate, final double strike, final double pointValue, final String assetName) {
    super( fixingDate, deliveryDate, strike, pointValue, 1, assetName);
  }
    
}
