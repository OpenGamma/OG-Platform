/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * 
 */
public class BondCleanPriceCalculator extends BondCalculator {
  private static final BondCalculator DIRTY_PRICE_CALCULATOR = BondCalculatorFactory.getCalculator(BondCalculatorFactory.DIRTY_PRICE);

  @Override
  public Double calculate(final Bond bond, final YieldCurveBundle curves) {
    return calculate(bond, DIRTY_PRICE_CALCULATOR.calculate(bond, curves));
  }

  @Override
  public Double calculate(final Bond bond, final double price) {
    return price - bond.getAccruedInterest();
  }

}
