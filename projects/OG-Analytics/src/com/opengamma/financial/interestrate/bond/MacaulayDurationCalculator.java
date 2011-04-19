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
 * 
 */
public class MacaulayDurationCalculator extends BondCalculator {
  private static final YieldSensitivityCalculator YIELD_SENSITIVITY_CALCULATOR = YieldSensitivityCalculator.getInstance();
  private static final BondDirtyPriceCalculator DIRTY_PRICE_CALCULATOR = BondDirtyPriceCalculator.getInstance();
  private static final MacaulayDurationCalculator INSTANCE = new MacaulayDurationCalculator();

  public static MacaulayDurationCalculator getInstance() {
    return INSTANCE;
  }

  @Override
  public Double calculate(final Bond bond, final double dirtyPrice) {
    Validate.notNull(bond, "bond");
    Validate.isTrue(dirtyPrice > 0, "dirty price must be positive");
    final double dollarDuration = YIELD_SENSITIVITY_CALCULATOR.calculateNthOrderSensitivity(bond.getAnnuity(), dirtyPrice, 1);
    return dollarDuration / dirtyPrice;
  }

  @Override
  public Double calculate(final Bond bond, final YieldCurveBundle curves) {
    return calculate(bond, DIRTY_PRICE_CALCULATOR.calculate(bond, curves));
  }
}
