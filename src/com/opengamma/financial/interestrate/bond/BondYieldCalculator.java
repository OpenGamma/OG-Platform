/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.ContinouslyCompoundedYieldCalculator;
import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * Continuously compounded 
 */
public class BondYieldCalculator {
  private final ContinouslyCompoundedYieldCalculator _ccyc = new ContinouslyCompoundedYieldCalculator();

  public double calculate(final Bond bond, final Double dirtyPrice) {
    Validate.notNull(bond, "bond");
    return _ccyc.calculate(bond.getFixedAnnuity(), dirtyPrice);
  }
}
