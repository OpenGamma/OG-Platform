/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import com.opengamma.financial.model.interestrate.InterestRateModel;

/**
 * A DiscountCurve contains discount factors <i>e<sup>-r(t)t</sup></i> (where
 * <i>t</i> is the maturity in years and <i>r(t)</i> is the interest rate at
 * maturity <i>t</i>).
 * 
 * @author emcleod
 */

public abstract class DiscountCurve implements InterestRateModel<Double> {

  /**
   * @return The interest rate for time to maturity <i>t</i>.
   * @throws IllegalArgumentException
   *           If the time to maturity is negative.
   */
  @Override
  public abstract double getInterestRate(final Double t);

  /**
   * @return The discount factor for time to maturity <i>t</i>.
   * @throws IllegalArgumentException
   *           If the time to maturity is negative.
   */
  public abstract double getDiscountFactor(final Double t);
}
