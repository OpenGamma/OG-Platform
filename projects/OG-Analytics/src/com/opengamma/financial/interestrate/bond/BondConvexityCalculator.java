/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.YieldSensitivityCalculator;
import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * Bond convexity calculator
 */
public class BondConvexityCalculator extends BondCalculator {
  private static final YieldSensitivityCalculator YIELD_SENSITIVITY_CALCULATOR = YieldSensitivityCalculator.getInstance();
  private static final BondDirtyPriceCalculator DIRTY_PRICE_CALCULATOR = BondDirtyPriceCalculator.getInstance();
  private static final BondConvexityCalculator INSTANCE = new BondConvexityCalculator();

  public static BondConvexityCalculator getInstance() {
    return INSTANCE;
  }
  /**
   * The convexity of a bond, i.e. the second derivative of its PV with respect to its <b>continuously compounded</b> yield divided by its PV
   * @param bond  A bond of known coupons 
   * @param dirtyPrice The actual sales price of the bond (i.e. clean/quoted price) plus accrued interest. Also known as full price
   * @return The bond convexity
   */
  @Override
  public Double calculate(final Bond bond, final double dirtyPrice) {
    Validate.notNull(bond, "bond");
    Validate.isTrue(dirtyPrice > 0, "price must be positive");
    final double secondOrder = YIELD_SENSITIVITY_CALCULATOR.calculateNthOrderSensitivity(bond.getAnnuity(), dirtyPrice, 2);
    return secondOrder / dirtyPrice;
  }

  @Override
  public Double calculate(final Bond bond, final YieldCurveBundle curves) {
    return calculate(bond, DIRTY_PRICE_CALCULATOR.calculate(bond, curves));
  }

}
