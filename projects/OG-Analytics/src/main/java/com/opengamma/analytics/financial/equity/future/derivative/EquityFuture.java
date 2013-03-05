/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.derivative;

import com.opengamma.util.money.Currency;

/**
 * Equity future derivative for pricing.
 * TODO [PLAT-3189] Refactor EquityFuture along lines of CommodityFuture to capture Physical-Settlement.
 */
public class EquityFuture extends CashSettledFuture {

  public EquityFuture(final double timeToFixing, final double timeToDelivery, final double strike, final Currency currency, final double unitValue) {
    super(timeToFixing, timeToDelivery, strike, currency, unitValue);
  }
}
