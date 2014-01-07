/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Methods for the pricing of bond futures generic to all models.
 * @deprecated Use the {@link BondFuturesTransactionMethod}.
 */
@Deprecated
public abstract class BondFutureMethod {

  /**
   * Compute the present value of a future transaction from a quoted price.
   * @param future The future.
   * @param price The quoted price.
   * @return The present value.
   */
  public MultipleCurrencyAmount presentValueFromPrice(final BondFuture future, final double price) {
    double pv = (price - future.getReferencePrice()) * future.getNotional();
    return MultipleCurrencyAmount.of(future.getCurrency(), pv);
  }

}
