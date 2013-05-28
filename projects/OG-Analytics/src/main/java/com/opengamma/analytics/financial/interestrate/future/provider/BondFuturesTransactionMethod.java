/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesTransaction;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Methods for the pricing of bond futures generic to all models.
 */
public abstract class BondFuturesTransactionMethod {

  /**
   * Compute the present value of a future transaction from a quoted price.
   * @param futures The futures.
   * @param price The quoted price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromPrice(final BondFuturesTransaction futures, final double price) {
    double pv = (price - futures.getReferencePrice()) * futures.getUnderlyingFuture().getNotional() * futures.getQuantity();
    return MultipleCurrencyAmount.of(futures.getUnderlyingFuture().getCurrency(), pv);
  }

}
