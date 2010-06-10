/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import java.util.Map;
import java.util.Set;

import com.opengamma.financial.model.interestrate.InterestRateModel;

/**
 * A DiscountCurve contains discount factors <i>e<sup>-r(t)t</sup></i> (where
 * <i>t</i> is the maturity in years and <i>r(t)</i> is the interest rate at
 * maturity <i>t</i>).
 * 
 */

public abstract class YieldAndDiscountCurve implements InterestRateModel<Double> {

  /**
   * @param t The time 
   * @return The interest rate for time to maturity <i>t</i>.
   * @throws IllegalArgumentException
   *           If the time to maturity is negative.
   */
  @Override
  public abstract double getInterestRate(final Double t);

  /**
   * @param t The time 
   * @return The discount factor for time to maturity <i>t</i>.
   * @throws IllegalArgumentException
   *           If the time to maturity is negative.
   */
  public abstract double getDiscountFactor(final Double t);

  public abstract Set<Double> getMaturities();

  public abstract YieldAndDiscountCurve withParallelShift(final Double shift);

  public abstract YieldAndDiscountCurve withSingleShift(final Double t, Double shift);

  public abstract YieldAndDiscountCurve withMultipleShifts(final Map<Double, Double> shifts);
}
