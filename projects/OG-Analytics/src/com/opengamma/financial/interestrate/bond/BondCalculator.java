/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * 
 */
public abstract class BondCalculator {

  public abstract Double calculate(final Bond bond, final YieldCurveBundle curves);

  public abstract Double calculate(final Bond bond, final double price); //TODO not sure that this method is needed
}
