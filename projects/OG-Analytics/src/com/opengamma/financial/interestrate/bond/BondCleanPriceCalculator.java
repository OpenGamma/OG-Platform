/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * 
 */
public final class BondCleanPriceCalculator extends BondCalculator {
  private static final BondCalculator DIRTY_PRICE_CALCULATOR = BondDirtyPriceCalculator.getInstance();
  private static final BondCleanPriceCalculator INSTANCE = new BondCleanPriceCalculator();

  private BondCleanPriceCalculator() {
  }

  public static BondCleanPriceCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public Double calculate(final Bond bond, final YieldCurveBundle curves) {
    Validate.notNull(bond, "bond");
    Validate.notNull(curves, "curve");
    return calculate(bond, DIRTY_PRICE_CALCULATOR.calculate(bond, curves));
  }

  @Override
  public Double calculate(final Bond bond, final double dirtyPrice) {
    Validate.notNull(bond, "bond");
    return dirtyPrice - bond.getAccruedInterest();
  }

}
