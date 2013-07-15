/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.recoveryratemodel;

import com.opengamma.util.ArgumentChecker;

/**
 * Class to specify the recovery rate model to apply to an obligor
 */
public abstract class RecoveryRateModel {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Maybe deprecate this and not have the specific types of recovery model derive from it

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables for the recovery rate model

  private final double _recoveryRate;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public RecoveryRateModel(final double recoveryRate) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    ArgumentChecker.notNegative(recoveryRate, "Recovery Rate");
    ArgumentChecker.isTrue(recoveryRate <= 1.0, "Recovery rate should be less than or equal to 100%");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _recoveryRate = recoveryRate;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public double getRecoveryRate() {
    return _recoveryRate;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
