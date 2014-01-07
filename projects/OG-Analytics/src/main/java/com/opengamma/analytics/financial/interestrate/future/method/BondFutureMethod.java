/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Methods for the pricing of bond futures generic to all models.
 * @deprecated {@link PricingMethod} is deprecated
 */
@Deprecated
public abstract class BondFutureMethod implements PricingMethod {

  /**
   * Compute the present value of a future transaction from a quoted price.
   * @param future The future.
   * @param price The quoted price.
   * @return The present value.
   */
  public CurrencyAmount presentValueFromPrice(final BondFuture future, final double price) {
    final double pv = (price - future.getReferencePrice()) * future.getNotional();
    return CurrencyAmount.of(future.getCurrency(), pv);
  }

}
