/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.YieldSensitivityCalculator;
import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * Bond convexity calculator
 */
public class ConvexityCalculator {

  /**
   * The convexity of a bond, i.e. the second derivative of its PV with respect to its <b>continuously compounded</b> yield divided by its PV
   * @param bond  A bond of known coupons 
   * @param dirtyPrice The actual sales price of the bond (i.e. clean/quoted price) plus accrued interest. Also know as full price
   * @return The bond convexity
   */
  public double calculate(final Bond bond, final double dirtyPrice) {
    Validate.notNull(bond, "bond");
    if (dirtyPrice <= 0) {
      throw new IllegalArgumentException("Price must be positive");
    }
    double secondOrder = YieldSensitivityCalculator.getInstance().calculateNthOrderSensitivity(bond.getAnnuity(), dirtyPrice, 2);
    return secondOrder / dirtyPrice;
  }

}
