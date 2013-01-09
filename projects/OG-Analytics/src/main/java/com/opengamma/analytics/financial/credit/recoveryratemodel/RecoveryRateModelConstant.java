/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.recoveryratemodel;

import com.opengamma.util.ArgumentChecker;

/**
 * Class to specify a constant recovery rate model to tag to a given obligor/trade
 */
public class RecoveryRateModelConstant {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables for the constant recovery rate model

  private final double _recoveryRate;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public RecoveryRateModelConstant(final double recoveryRate) {

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
