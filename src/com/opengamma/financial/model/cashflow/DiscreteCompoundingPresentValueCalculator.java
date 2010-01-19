/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.cashflow;

import com.opengamma.financial.model.interestrate.InterestRateModel;

/**
 * @author emcleod
 * 
 */
public class DiscreteCompoundingPresentValueCalculator extends PresentValueCalculator {

  @Override
  public double calculate(final double t, final double c, final InterestRateModel<Double> rates) {
    if (t < 0)
      throw new IllegalArgumentException("Time must be positive");
    if (rates == null)
      throw new IllegalArgumentException("Rates model was null");
    return c * Math.pow(1 + rates.getInterestRate(t), -t);
  }
}
