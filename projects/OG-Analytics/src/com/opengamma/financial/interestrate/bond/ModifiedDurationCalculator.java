/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * 
 */
public class ModifiedDurationCalculator {
  private final BondYieldCalculator _byc = BondYieldCalculator.getInstance();
  private final MacaulayDurationCalculator _mdc = new MacaulayDurationCalculator();

  //TODO compounding frequency does not belong here - should be in bond
  public double calculate(final Bond bond, final double dirtyPrice, final int compoundingFrequency) {
    Validate.notNull(bond, "bond");
    Validate.isTrue(dirtyPrice > 0, "price must be positive");
    Validate.isTrue(compoundingFrequency > 0, "compounding frequency must be positive");
    final double duration = _mdc.calculate(bond, dirtyPrice);
    final double yield = _byc.calculate(bond, dirtyPrice); // NOTE this yield is continuously compounded
    return duration * Math.exp(-yield / compoundingFrequency);
  }
}
