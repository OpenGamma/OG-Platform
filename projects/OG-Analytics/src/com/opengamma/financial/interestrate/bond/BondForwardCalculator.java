/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.BondForward;

/**
 * 
 */
public abstract class BondForwardCalculator {

  //TODO repo curves are simply compounded, so at the moment we cannot fold the funding rate into the yield curve bundle (i.e. have a repo curve)
  // This needs to change - we need to get everything into a continuously compounded basis and deal with conversions appropriately
  public abstract Double calculate(BondForward bondForward, YieldCurveBundle curves, double fundingRate);

  public abstract Double calculate(BondForward bondForward, double price, double fundingRate); //TODO not sure that this method is needed
}
