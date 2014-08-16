/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import com.opengamma.analytics.financial.interestrate.capletstripping.CapletStripper;
import com.opengamma.analytics.financial.interestrate.capletstripping.MarketDataType;

/**
 * 
 */
public class SingleStrikeSetup extends CapletStrippingSetup {

  public void testATMStripping(final CapletStripper stripper, final double expChi2, final double[] expModelParms, final double tol, final boolean print) {
    testStripping(stripper, getATMCaps(), getATMCapPrices(), MarketDataType.PRICE, expChi2, expModelParms, tol, print);
  }

  public void testSingleStrikeStripping(final CapletStripper stripper, final int strikeIndex, final double expChi2, final double[] expModelParms, final double tol, final boolean print) {
    testStripping(stripper, getCaps(strikeIndex), getCapPrices(strikeIndex), MarketDataType.PRICE, expChi2, expModelParms, tol, print);
  }
}
