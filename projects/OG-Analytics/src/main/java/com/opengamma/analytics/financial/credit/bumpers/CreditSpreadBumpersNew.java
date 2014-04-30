/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.bumpers;

/**
 * Class containing utilities for bumping credit spread term structures by user defined methods and amounts
 *@deprecated this will be deleted 
 */
@Deprecated
public class CreditSpreadBumpersNew {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Add error checkers for the input arguments
  // TODO : Replace the logic in the choice of spread bump to ensure something is bumped i.e. use if then else

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to bump the credit spread term structure at every tenor point by a specified amount

  public double[] getBumpedCreditSpreads(final double[] marketSpreads, final double spreadBump, final SpreadBumpType spreadBumpType) {

    final int n = marketSpreads.length;
    final double[] bumpedCreditSpreads = new double[n];

    switch (spreadBumpType) {
      case ADDITIVE_PARALLEL:
      case ADDITIVE:
      case ADDITIVE_BUCKETED:
        for (int m = 0; m < n; m++) {
          bumpedCreditSpreads[m] = marketSpreads[m] + spreadBump;
        }
        return bumpedCreditSpreads;
      case MULTIPLICATIVE_PARALLEL:
      case MULTIPLICATIVE:
      case MULTIPLICATIVE_BUCKETED:
        for (int m = 0; m < n; m++) {
          bumpedCreditSpreads[m] = marketSpreads[m] * (1 + spreadBump);
        }
        return bumpedCreditSpreads;
      default:
        throw new IllegalArgumentException("Cannot handle bump type " + spreadBumpType);
    }
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to bump the credit spread term structure at a single (specified) tenor point by a specified amount

  public double[] getBumpedCreditSpreads(final double[] marketSpreads, final int spreadTenorToBump, final double spreadBump, final SpreadBumpType spreadBumpType) {
    final int n = marketSpreads.length;
    final double[] bumpedCreditSpreads = new double[n];
    System.arraycopy(marketSpreads, 0, bumpedCreditSpreads, 0, n);

    switch (spreadBumpType) {
      case ADDITIVE_BUCKETED:
      case ADDITIVE:
        bumpedCreditSpreads[spreadTenorToBump] += spreadBump;
        return bumpedCreditSpreads;
      case MULTIPLICATIVE_BUCKETED:
      case MULTIPLICATIVE:
        bumpedCreditSpreads[spreadTenorToBump] *= (1 + spreadBump);
        return bumpedCreditSpreads;
      default:
        throw new IllegalArgumentException("Cannot handle bump type " + spreadBumpType);
    }
  }

  public double getBumpedCreditSpread(final double marketSpread, final double spreadBump, final SpreadBumpType spreadBumpType) {
    switch (spreadBumpType) {
      case ADDITIVE_BUCKETED:
      case ADDITIVE:
        return marketSpread + spreadBump;
      case MULTIPLICATIVE_BUCKETED:
      case MULTIPLICATIVE:
        return marketSpread * (1 + spreadBump);
      default:
        throw new IllegalArgumentException("Cannot handle bump type " + spreadBumpType);
    }
  }
}
