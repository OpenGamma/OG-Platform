/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import com.opengamma.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.financial.interestrate.method.PricingMethod;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Methods for the pricing of interest rate futures option with premium generic to all models.
 */
public abstract class InterestRateFutureOptionMarginTransactionMethod implements PricingMethod {

  /**
   * Compute the present value of a future transaction from a quoted price.
   * @param option The future option.
   * @param price The quoted price.
   * @return The present value.
   */
  public CurrencyAmount presentValueFromPrice(final InterestRateFutureOptionMarginTransaction option, final double price) {
    double pv = (price - option.getReferencePrice()) * option.getUnderlyingOption().getUnderlyingFuture().getPaymentAccrualFactor() * option.getUnderlyingOption().getUnderlyingFuture().getNotional()
        * option.getQuantity();
    return CurrencyAmount.of(option.getUnderlyingOption().getCurrency(), pv);
  }

}
