/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.derivative;

import com.opengamma.util.money.Currency;

/**
 * Generic index future derivative for pricing. An IndexFuture is always cash-settled.
 */
public class IndexFuture extends CashSettledFuture {

  public IndexFuture(final double timeToExpiry, final double timeToSettlement, final double strike, final Currency currency, final double unitAmount) {
    super(timeToExpiry, timeToSettlement, strike, currency, unitAmount);
  }

}
