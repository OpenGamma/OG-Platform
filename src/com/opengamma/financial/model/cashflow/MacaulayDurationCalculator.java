/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.cashflow;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * 
 */
public class MacaulayDurationCalculator {
  private final EffectiveDurationCalculator _edc = new EffectiveDurationCalculator();

  public double calculate(final Bond bond, final double dirtyPrice) {
    Validate.notNull(bond, "bond");
    if (dirtyPrice <= 0) {
      throw new IllegalArgumentException("Price must be positive");
    }
    return _edc.calculate(bond.getFixedAnnuity(), dirtyPrice);
  }
}
