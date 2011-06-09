/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * Interface used to compute the bond specific figures.
 */
public abstract class BondCalculator {

  public abstract Double calculate(final Bond bond, final YieldCurveBundle curves);

  public abstract Double calculate(final Bond bond, final double price); //TODO not sure that this method is needed
}
