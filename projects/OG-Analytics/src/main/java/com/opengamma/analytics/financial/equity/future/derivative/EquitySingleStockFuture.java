/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.derivative;

import com.opengamma.util.money.Currency;

/**
 * A cash-settled futures contract on the index of the *dividends* of a given stock market index on the _fixingDate
 */
public class EquitySingleStockFuture extends EquityFuture {

  public EquitySingleStockFuture(final double timeToFixing, final double timeToDelivery, final double strike, final Currency currency, final double unitValue) {
    super(timeToFixing, timeToDelivery, strike, currency, unitValue);
  }

}
