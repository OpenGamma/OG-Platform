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
public class RecoveryRateModelConstant extends RecoveryRateModel {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Member variables for the constant recovery rate model

  private final double _recoveryRate;

  private final RecoveryRateType _recoveryRateType;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public RecoveryRateModelConstant(final double recoveryRate) {

    super(recoveryRate);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    ArgumentChecker.notNegative(recoveryRate, "Recovery Rate");
    ArgumentChecker.isTrue(recoveryRate <= 1.0, "Recovery rate should be less than or equal to 100%");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    _recoveryRate = recoveryRate;

    _recoveryRateType = RecoveryRateType.CONSTANT;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  @Override
  public double getRecoveryRate() {
    return _recoveryRate;
  }

  public RecoveryRateType getRecoveryRateType() {
    return _recoveryRateType;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Builder method to allow the recovery rate to be set at a specified value

  public RecoveryRateModelConstant sampleRecoveryRate(final double recoveryRate) {

    final RecoveryRateModelConstant modifiedRecoveryRateModel = new RecoveryRateModelConstant(recoveryRate);

    return modifiedRecoveryRateModel;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

}
