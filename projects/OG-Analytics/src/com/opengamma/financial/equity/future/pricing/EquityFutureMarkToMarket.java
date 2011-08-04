/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.future.pricing;

import com.opengamma.financial.equity.future.derivative.EquityFuture;

import org.apache.commons.lang.Validate;

/**
 * Method to compute a future's present value given market price.
 * FIXME !!! This probably needs to be discounted by daysToSpot... curve.getDiscountFactor(future.getDaysToSpot())
 */
public abstract class EquityFutureMarkToMarket {

  public static double presentValue(final EquityFuture future, final double mktPrice) {
    Validate.notNull(future, "Future");
    Validate.notNull(mktPrice, "Market Price");
    return (mktPrice - future.getStrike()) * future.getUnitAmount();
  }

  public static double delta(final EquityFuture future) {
    Validate.notNull(future, "Future");
    return future.getUnitAmount();
  }
}
