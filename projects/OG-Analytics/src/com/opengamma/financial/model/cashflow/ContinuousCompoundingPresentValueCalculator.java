/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.cashflow;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.InterestRateModel;

/**
 * 
 */
public class ContinuousCompoundingPresentValueCalculator extends PresentValueCalculator {

  @Override
  public double calculate(final double t, final double c, final InterestRateModel<Double> rates) {
    if (t < 0) {
      throw new IllegalArgumentException("Time must be positive");
    }
    Validate.notNull(rates, "rates");
    return c * Math.exp(-rates.getInterestRate(t) * t);
  }
}
