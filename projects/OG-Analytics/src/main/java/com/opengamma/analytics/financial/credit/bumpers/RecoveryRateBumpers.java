/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.bumpers;

/**
 * Class containing utilities for bumping recovery rates by user defined methods and amounts
 *@deprecated this will be deleted 
 */
@Deprecated
public class RecoveryRateBumpers {

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // TODO :

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Method to bump a single recovery rate by a specified method and amount

  public double getBumpedRecoveryRate(final double recoveryRate, final double recoveryRateBump, final RecoveryRateBumpType recoveryRateBumpType) {

    double bumpedRecoveryRate = 0.0;

    switch (recoveryRateBumpType) {
      case ADDITIVE:
        bumpedRecoveryRate = recoveryRate + recoveryRateBump;
        return bumpedRecoveryRate;

      case MULTIPLICATIVE:
        bumpedRecoveryRate = recoveryRate * (1 + recoveryRateBump);
        return bumpedRecoveryRate;

      default:
        throw new IllegalArgumentException("Cannot handle bump type " + recoveryRateBumpType);
    }
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Method to bump a recovery rate vector by a specified method and amount

  public double[] getBumpedRecoveryRate(final double[] recoveryRate, final double recoveryRateBump, final RecoveryRateBumpType recoveryRateBumpType) {

    final double[] bumpedRecoveryRate = new double[recoveryRate.length];

    switch (recoveryRateBumpType) {
      case ADDITIVE:
        for (int i = 0; i < recoveryRate.length; i++) {
          bumpedRecoveryRate[i] = recoveryRate[i] + recoveryRateBump;
        }
        return bumpedRecoveryRate;

      case MULTIPLICATIVE:
        for (int i = 0; i < recoveryRate.length; i++) {
          bumpedRecoveryRate[i] = recoveryRate[i] * (1 + recoveryRateBump);
        }
        return bumpedRecoveryRate;

      default:
        throw new IllegalArgumentException("Cannot handle bump type " + recoveryRateBumpType);
    }
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------
}
