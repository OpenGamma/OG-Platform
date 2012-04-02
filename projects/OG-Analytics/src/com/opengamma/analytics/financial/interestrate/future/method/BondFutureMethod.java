/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;

/**
 * Methods for the pricing of bond futures generic to all models.
 */
public abstract class BondFutureMethod implements PricingMethod {

  /**
   * Compute the present value of a future transaction from a quoted price.
   * @param future The future.
   * @param price The quoted price.
   * @return The present value.
   */
  public double presentValueFromPrice(final BondFuture future, final double price) {
    double pv = (price - future.getReferencePrice()) * future.getNotional();
    return pv;
  }

}
