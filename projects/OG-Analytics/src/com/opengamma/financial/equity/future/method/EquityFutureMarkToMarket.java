/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.future.method;

import org.apache.commons.lang.Validate;
import com.opengamma.financial.equity.future.derivative.EquityFuture;

/**
 * Method to compute a future's present value given market price.
 * !!! This probably needs to be discounted by daysToSpot... curve.getDiscountFactor(future.getDaysToSpot())
 */
public abstract class EquityFutureMarkToMarket {

  public static double presentValue(final EquityFuture future, final double mktPrice) {
    Validate.notNull(future, "Future");
    Validate.notNull(mktPrice, "Market Price");
    return (mktPrice - future.getStrike()) * future.getPointValue() * future.getNumContracts();
  }
}
