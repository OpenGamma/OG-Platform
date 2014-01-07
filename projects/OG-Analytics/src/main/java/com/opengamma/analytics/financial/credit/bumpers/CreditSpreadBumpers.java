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
 *@deprecated this will be deleted 
 */
@Deprecated
public class CreditSpreadBumpers {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Add error checkers for the input arguments
  // TODO : Replace the logic in the choice of spread bump to ensure something is bumped i.e. use if then else

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to bump the credit spread term structure at every tenor point by a specified amount

  public double[] getBumpedCreditSpreads(final double[] marketSpreads, final double spreadBump, final SpreadBumpType spreadBumpType) {

    final double[] bumpedCreditSpreads = new double[marketSpreads.length];

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

    final double[] bumpedCreditSpreads = Arrays.copyOf(marketSpreads, marketSpreads.length);

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

  // Method to bump the credit spread term structure at every tenor point by a specified amount simultaneously for every obligor

  public double[][] getBumpedCreditSpreads(final int numberOfObligors, final int numberOfTenors, final double[][] marketSpreads, final double spreadBump, final SpreadBumpType spreadBumpType) {

    final double[][] bumpedMarketSpreads = new double[numberOfObligors][numberOfTenors];

    switch (spreadBumpType) {
      case ADDITIVE_PARALLEL:
        for (int i = 0; i < numberOfObligors; i++) {
          for (int m = 0; m < numberOfTenors; m++) {
            bumpedMarketSpreads[i][m] = marketSpreads[i][m] + spreadBump;
          }
        }

        return bumpedMarketSpreads;

      case MULTIPLICATIVE_PARALLEL:
        for (int i = 0; i < numberOfObligors; i++) {
          for (int m = 0; m < numberOfTenors; m++) {
            bumpedMarketSpreads[i][m] = marketSpreads[i][m] * (1 + spreadBump);
          }
        }

        return bumpedMarketSpreads;

      default:
        throw new IllegalArgumentException("Cannot handle bump type " + spreadBumpType);
    }
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Method to bump the credit spread term structure at every tenor point by a specified amount simultaneously for a single obligor i

  public double[][] getBumpedCreditSpreads(final int numberOfObligors, final int numberOfTenors, final int i, final double[][] marketSpreads, final double spreadBump,
      final SpreadBumpType spreadBumpType) {

    // Assign the bumped spread matrix to be the original input spread matrix
    final double[][] bumpedMarketSpreads = marketSpreads;

    switch (spreadBumpType) {
      case ADDITIVE_PARALLEL:
        for (int m = 0; m < numberOfTenors; m++) {
          bumpedMarketSpreads[i][m] = marketSpreads[i][m] + spreadBump;
        }

        return bumpedMarketSpreads;

      case MULTIPLICATIVE_PARALLEL:
        for (int m = 0; m < numberOfTenors; m++) {
          bumpedMarketSpreads[i][m] = marketSpreads[i][m] * (1 + spreadBump);
        }

        return bumpedMarketSpreads;

      default:
        throw new IllegalArgumentException("Cannot handle bump type " + spreadBumpType);
    }
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
