/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.bumpers;

import java.util.Arrays;

import com.opengamma.OpenGammaRuntimeException;

/**
 * 
 *@deprecated this will be deleted 
 */
@Deprecated
public class InterestRateBumpers {
  public double[] getBumpedRates(final double[] marketSpreads, final double spreadBump, final InterestRateBumpType bumpType) {
    final double[] bumpedCreditSpreads = new double[marketSpreads.length];
    // Calculate the bumped spreads
    for (int m = 0; m < marketSpreads.length; m++) {
      getBumpedRates(marketSpreads, spreadBump, bumpType, bumpedCreditSpreads, m);
    }
    return bumpedCreditSpreads;
  }

  public static double[] getBumpedRates(final double[] marketSpreads, final double spreadBump, final InterestRateBumpType bumpType, final int m) {
    final double[] bumped = Arrays.copyOf(marketSpreads, marketSpreads.length);
    getBumpedRates(marketSpreads, spreadBump, bumpType, bumped, m);
    return bumped;
  }

  private static void getBumpedRates(final double[] marketSpreads, final double spreadBump, final InterestRateBumpType bumpType, final double[] bumpedCreditSpreads, final int m) {
    switch (bumpType) {
      case ADDITIVE:
      case ADDITIVE_PARALLEL:
      case ADDITIVE_BUCKETED:
        bumpedCreditSpreads[m] = marketSpreads[m] + spreadBump;
        break;
      case MULTIPLICATIVE:
      case MULTIPLICATIVE_PARALLEL:
      case MULTIPLICATIVE_BUCKETED:
        bumpedCreditSpreads[m] = marketSpreads[m] * (1 + spreadBump);
        break;
      default:
        throw new OpenGammaRuntimeException("Unknown bump type " + bumpType);
    }
  }
}
