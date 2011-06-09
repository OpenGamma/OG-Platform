/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.future.pricing;

import com.opengamma.financial.equity.future.derivative.EquityFuture;

import org.apache.commons.lang.Validate;

/**
 * Method to compute a future's present value given the current value of its underlying asset and a cost of carry. 
 * !!! This may include a convexity adjustment for the correlation between these two factors. 
 */
public class EquityFutureCostOfCarry {

  public double presentValue(final EquityFuture future, final double assetValue, final double costOfCarry) {
    Validate.notNull(future, "Future");
    Validate.notNull(costOfCarry, "Cost of Carry");

    double fwdPrice = assetValue * Math.exp(costOfCarry * future.getTimeToSettlement());
    return (fwdPrice - future.getStrike()) * future.getUnitAmount();

  }
}
