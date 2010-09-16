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
 * 
 */
public class MacaulayDurationCalculator {

  public double calculate(final Bond bond, final double dirtyPrice) {
    Validate.notNull(bond, "bond");
    if (dirtyPrice <= 0) {
      throw new IllegalArgumentException("Price must be positive");
    }

    double dollarDuration = YieldSensitivityCalculator.getInstance().calculateNthOrderSensitivity(
        bond.getFixedAnnuity(), dirtyPrice, 1);
    return dollarDuration / dirtyPrice;
  }
}
