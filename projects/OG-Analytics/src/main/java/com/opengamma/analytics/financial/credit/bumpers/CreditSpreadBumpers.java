/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.bumpers;

import java.util.Arrays;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Class containing utilities for bumping credit spread term structures by user defined methods and amounts
 */
public class CreditSpreadBumpers {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Add error checkers for the input arguments
  // TODO : Replace the logic in the choice of spread bump to ensure something is bumped i.e. use if then else

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to bump the credit spread term structure at every tenor point by a specified amount

  public double[] getBumpedCreditSpreads(final double[] marketSpreads, final double spreadBump, final SpreadBumpType spreadBumpType) {

    double[] bumpedCreditSpreads = new double[marketSpreads.length];

    // Calculate the bumped spreads

    for (int m = 0; m < marketSpreads.length; m++) {

      if (spreadBumpType == SpreadBumpType.ADDITIVE_PARALLEL) {
        bumpedCreditSpreads[m] = marketSpreads[m] + spreadBump;
      }

      if (spreadBumpType == SpreadBumpType.MULTIPLICATIVE_PARALLEL) {
        bumpedCreditSpreads[m] = marketSpreads[m] * (1 + spreadBump);
      }
    }

    return bumpedCreditSpreads;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to bump the credit spread term structure at a single (specified) tenor point by a specified amount

  public double[] getBumpedCreditSpreads(final double[] marketSpreads, final int spreadTenorToBump, final double spreadBump, final SpreadBumpType spreadBumpType) {

    double[] bumpedCreditSpreads = Arrays.copyOf(marketSpreads, marketSpreads.length);

    // Calculate the bumped spreads

    if (spreadBumpType == SpreadBumpType.ADDITIVE_BUCKETED || spreadBumpType == SpreadBumpType.ADDITIVE) {
      bumpedCreditSpreads[spreadTenorToBump] += spreadBump;
    } else if (spreadBumpType == SpreadBumpType.MULTIPLICATIVE_BUCKETED || spreadBumpType == SpreadBumpType.MULTIPLICATIVE) {
      bumpedCreditSpreads[spreadTenorToBump] *= (1 + spreadBump);
    } else {
      throw new OpenGammaRuntimeException("Unsupported spread bump type " + spreadBumpType);
    }

    return bumpedCreditSpreads;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
