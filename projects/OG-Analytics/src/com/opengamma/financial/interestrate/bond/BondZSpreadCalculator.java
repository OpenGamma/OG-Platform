/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.ZSpreadCalculator;
import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * 
 */
public class BondZSpreadCalculator {

  public double calculate(final Bond bond, final YieldCurveBundle curves, final Double dirtyPrice) {
    Validate.notNull(bond, "bond");
    Validate.isTrue(dirtyPrice > 0.0, "need dirtyPrice greater than zero");
    return ZSpreadCalculator.getInstance().calculateZSpread(bond.getAnnuity(), curves, dirtyPrice);
  }
}
