/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.ZSpreadCalculator;
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
